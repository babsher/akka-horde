package edu.gmu.horde.actors

import akka.actor.{ActorSystem, _}
import akka.event.LoggingAdapter
import akka.http.Http
import akka.http.marshallers.sprayjson.SprayJsonSupport
import akka.http.model.{HttpResponse, HttpRequest}
import akka.http.server.Directives._
import akka.pattern._
import akka.stream.FlowMaterializer
import akka.stream.scaladsl.{Sink, Source}
import akka.util._
import com.google.common.io.BaseEncoding
import com.typesafe.config.Config
import edu.gmu.horde._
import edu.gmu.horde.storage.{AttributeValue, DoubleValue, StringValue}
import spray.json._

import scala.concurrent.{Future, ExecutionContextExecutor}
import scala.concurrent.duration._

trait Protocols extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val agentInfoFormat = jsonFormat2(AgentInfo.apply)
  implicit val requestAgentsFormat = jsonFormat1(AgentSummary.apply)
  implicit val trainFormat = jsonFormat1(Train.apply)
  implicit val stateFormat = jsonFormat1(State.apply)
  implicit object attributeValueFormat extends RootJsonFormat[AttributeValue] {
    override def read(json: JsValue) = json match {
      case JsNumber(num) =>
        DoubleValue(num.toDouble)
      case JsString(str) =>
        StringValue(str)
      case _ => throw new DeserializationException("Attribute value expected")
    }

    override def write(obj: AttributeValue) = obj match {
      case DoubleValue(num) => JsNumber(num)
      case StringValue(str) => JsString(str)
    }
  }
  implicit val agentDetailFormat = jsonFormat5(AgentDetail.apply)
}

trait ZergHordeService extends Protocols {
  implicit val system: ActorSystem
  implicit val materializer: FlowMaterializer
  implicit val timeout: Timeout = Timeout(15 seconds)
  val logger: LoggingAdapter
  val horde: ActorRef

  lazy val zergConnectionFlow = Http().outgoingConnection(config.getString("services.telizeHost"), config.getInt("services.telizePort")).flow

  def zergRequest(request: HttpRequest): Future[HttpResponse] = Source.single(request).via(zergConnectionFlow).runWith(Sink.head)
  
  val routes = {
    logRequestResult("zerg-microservice") {
      path("") {
        getFromResource("app/dist/index.html")
      } ~ {
        getFromResourceDirectory("app/dist/")
      } ~
      pathPrefix("api") {
        pathPrefix("agents") {
          get {
            complete((horde ? RequestAgentInfo(null)).mapTo[AgentSummary])
          } ~
          pathPrefix("agent") {
            (get & path(Segment)) { id =>
              complete((getActorPath(id) ? RequestAgentDetail).mapTo[AgentDetail])
            }
          } ~
          pathPrefix("train") {
            (post & path(Segment)) { id =>
              entity(as[Train]) { msg =>
                complete((getActorPath(id) ? msg).mapTo[Train])
              }
            }
          }
        } ~
        pathPrefix("system") {
          get {
            complete((horde ? RequestState).mapTo[State])
          }
          post {
            (post & path(Segment)) { id =>
              entity(as[State]) { msg =>
                complete((getActorPath(id) ? msg).mapTo[State])
              }
            }
          }
        }
      }
    }
  }

  implicit def executor: ExecutionContextExecutor

  def config: Config

  def getActorPath(id: String): ActorSelection = {
    val path = new String(BaseEncoding.base64Url().decode(id))
    system.actorSelection(path)
  }
}
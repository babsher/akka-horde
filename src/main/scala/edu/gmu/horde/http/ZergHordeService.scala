package edu.gmu.horde.http

import akka.actor.{ActorRef, ActorSelection, ActorSystem}
import akka.event.LoggingAdapter
import akka.http.Http
import akka.http.model.{HttpRequest, HttpResponse}
import akka.http.server.Directives._
import akka.http.server.PathMatchers.Segment
import akka.stream.FlowMaterializer
import akka.stream.scaladsl.{Sink, Source}
import akka.util.Timeout
import com.google.common.io.BaseEncoding
import com.typesafe.config.Config
import edu.gmu.horde._
import akka.pattern.ask

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.concurrent.duration._

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
            complete((horde ? RequestAgentInfo(null)).mapTo[AgentsSummary])
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
            complete((horde ? RequestState).mapTo[HordeState])
          } ~
          pathPrefix("run") {
            (put & entity(as[Run])) { msg =>
              complete((horde ? msg).mapTo[State])
            }
          } ~
          (pathPrefix("stop") & put) {
            complete((horde ? Stop).mapTo[State])
          } ~
          post {
            (post & path(Segment)) { id =>
              System.out.print("Sending message to agent: " + id)
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
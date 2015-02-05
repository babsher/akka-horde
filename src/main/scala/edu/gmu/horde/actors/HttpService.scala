package edu.gmu.horde.actors

import akka.actor._
import akka.event.{Logging, LoggingAdapter}
import akka.http.Http
import akka.http.client.RequestBuilding
import akka.http.marshallers.sprayjson.SprayJsonSupport._
import akka.http.marshalling.ToResponseMarshallable
import akka.http.model.{HttpResponse, HttpRequest}
import akka.http.model.StatusCodes._
import akka.pattern._
import akka.util._
import akka.http.server.Directives._
import akka.http.server.directives._
import com.typesafe.config.{ConfigFactory, Config}
import akka.actor.ActorSystem
import akka.event.{LoggingAdapter, Logging}
import akka.http.Http
import akka.http.client.RequestBuilding
import akka.http.marshallers.sprayjson.SprayJsonSupport._
import akka.http.marshalling.ToResponseMarshallable
import akka.http.model.{HttpResponse, HttpRequest}
import akka.http.model.StatusCodes._
import akka.http.server.Directives._
import akka.http.unmarshalling.Unmarshal
import akka.stream.FlowMaterializer
import akka.stream.scaladsl.{Sink, Source}
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import java.io.IOException
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.math._
import scala.concurrent.duration._
import spray.json.DefaultJsonProtocol

trait Protocols extends DefaultJsonProtocol {
  implicit val agentInfoFormat = jsonFormat2(AgentInfo.apply)
  implicit val requestAgentsFormat = jsonFormat1(AgentSummary.apply)
}

trait ZergHordeService extends Protocols {
  implicit val system: ActorSystem
  implicit def executor: ExecutionContextExecutor
  implicit val materializer: FlowMaterializer
  implicit val timeout: Timeout = Timeout(15 seconds)

  def config: Config
  val logger: LoggingAdapter
  val horde: ActorRef

  val routes = {
    get {
      getFromDirectory("app")
    } ~
    pathPrefix("api") {
      pathPrefix("agents") {
        get {
          complete((horde ? RequestAgentInfo()).mapTo[ToResponseMarshallable])
        }
      }
    }
  }
}

object ZergHordeService extends App with ZergHordeService {
  override implicit val system = ActorSystem()
  override implicit val executor = system.dispatcher
  override implicit val materializer = FlowMaterializer()

  override val config = ConfigFactory.load()
  override val logger = Logging(system, getClass)
  override val horde: ActorRef = createHorde()

  Http().bind(interface = config.getString("http.interface"), port = config.getInt("http.port")).startHandlingWith(routes)

  def createHorde(): ActorRef = {
    system.actorOf(Props[HordeFSM])
  }
}

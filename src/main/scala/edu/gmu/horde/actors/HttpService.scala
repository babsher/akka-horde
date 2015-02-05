package edu.gmu.horde.actors

import akka.actor.{ActorSystem, _}
import akka.event.{Logging, LoggingAdapter}
import akka.http.Http
import akka.http.marshalling.ToResponseMarshallable
import akka.http.server.Directives._
import akka.pattern._
import akka.stream.FlowMaterializer
import akka.util._
import com.typesafe.config.{Config, ConfigFactory}
import spray.json.DefaultJsonProtocol

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._

trait Protocols extends DefaultJsonProtocol {
  implicit val agentInfoFormat = jsonFormat2(AgentInfo.apply)
  implicit val requestAgentsFormat = jsonFormat1(AgentSummary.apply)
}

trait ZergHordeService extends Protocols {
  implicit val system: ActorSystem
  implicit val materializer: FlowMaterializer
  implicit val timeout: Timeout = Timeout(15 seconds)
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

  implicit def executor: ExecutionContextExecutor

  def config: Config
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

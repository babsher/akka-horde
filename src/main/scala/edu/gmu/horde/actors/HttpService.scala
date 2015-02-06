package edu.gmu.horde.actors

import akka.actor.{ActorSystem, _}
import akka.event.{Logging, LoggingAdapter}
import akka.http.Http
import akka.http.marshallers.sprayjson.SprayJsonSupport
import akka.http.marshalling.{Marshal, ToResponseMarshallable}
import akka.http.server.Directives._
import akka.pattern._
import akka.stream.FlowMaterializer
import akka.util._
import com.typesafe.config.{Config, ConfigFactory}
import spray.json.DefaultJsonProtocol

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._

trait Protocols extends DefaultJsonProtocol with SprayJsonSupport {
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
    path("") {
      getFromResource("app/dist/index.html")
    } ~ {
      getFromResourceDirectory("app/dist/")
    } ~
      pathPrefix("api") {
        pathPrefix("agents") {
          get {
            complete((horde ? RequestAgentInfo(null)).mapTo[AgentSummary])
          }
        }
      }
  }

  implicit def executor: ExecutionContextExecutor

  def config: Config
}
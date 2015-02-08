package edu.gmu.horde.actors

import akka.actor
import akka.actor.{ActorSystem, _}
import akka.event.{Logging, LoggingAdapter}
import akka.http.Http
import akka.http.marshallers.sprayjson.SprayJsonSupport
import akka.http.marshalling.{Marshal, ToResponseMarshallable}
import akka.http.server.Directives._
import akka.pattern._
import akka.stream.FlowMaterializer
import akka.util._
import com.google.common.io.BaseEncoding
import com.typesafe.config.{Config, ConfigFactory}
import spray.json.DefaultJsonProtocol

import scala.concurrent.{Future, ExecutionContextExecutor}
import scala.concurrent.duration._

trait Protocols extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val agentInfoFormat = jsonFormat2(AgentInfo.apply)
  implicit val requestAgentsFormat = jsonFormat1(AgentSummary.apply)
  implicit val trainFormat = jsonFormat1(Train.apply)
  implicit val agentDetailFormat = jsonFormat3(AgentDetail.apply)
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
        pathPrefix("agent") {
          get {
            complete((horde ? RequestAgentInfo(null)).mapTo[AgentSummary])
          } ~
          pathPrefix("" / Rest) { id =>
            complete((getActorPath(id) ? RequestAgentDetail()).mapTo[AgentDetail])
          }
          pathPrefix("train" / Rest) { id =>
            put {
              entity(as[Train]) { msg =>
                complete((getActorPath(id) ? msg).mapTo[Train])
              }
            }
          }
        } 
      }
  }
  
  def getActorPath(id: String): ActorSelection = {
    val path = new String(BaseEncoding.base64Url().decode(id))
    system.actorSelection(path)
  }

  implicit def executor: ExecutionContextExecutor

  def config: Config
}
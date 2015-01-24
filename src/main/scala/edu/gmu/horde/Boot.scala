package edu.gmu.horde

import akka.actor.{ActorSystem, Props}
import akka.util.Timeout
import akka.io.IO
import edu.gmu.horde.actors.MyServiceActor
import spray.can.Http
import akka.pattern.ask
import scala.concurrent.duration._

object Boot extends App {

  implicit val system = ActorSystem()

  // the handler actor replies to incoming HttpRequests
  val handler = system.actorOf(Props[MyServiceActor], name = "handler")

  IO(Http) ! Http.Bind(handler, interface = "localhost", port = 8080)
}

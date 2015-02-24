package edu.gmu.horde

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.event.Logging
import akka.http.Http
import akka.stream.ActorFlowMaterializer
import com.typesafe.config.ConfigFactory
import edu.gmu.horde.actors.HordeFSM
import edu.gmu.horde.http.ZergHordeService

object Boot extends App with ZergHordeService {
  override implicit val system = ActorSystem()
  override implicit val executor = system.dispatcher
  override implicit val materializer = ActorFlowMaterializer()

  override val config = ConfigFactory.load()
  override val logger = Logging(system, getClass)
  override val horde: ActorRef = createHorde()
  horde ! Scenario("zerg-" + new java.util.Date)

  Http().bind(interface = config.getString("http.interface"), port = config.getInt("http.port")).startHandlingWith(routes)

  def createHorde(): ActorRef = {
    system.actorOf(Props[HordeFSM], "horde")
  }
}
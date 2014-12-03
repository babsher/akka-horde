package edu.gmu.horde.zerg.agents

import akka.actor.{Props, ActorRef, Actor}
import edu.gmu.horde.SetEnvironment
import edu.gmu.horde.zerg.NewUnit

object MilitaryAgent {
  def props(env :ActorRef) = Props(new MilitaryAgent(env))
}

class MilitaryAgent(var env :ActorRef) extends Actor {
  var platoon :ActorRef = context.actorOf(Props(classOf[Platoon], env))

  override def receive: Receive = {
    case n @ NewUnit(id, unit) =>
      platoon ! n
  }
}

package edu.gmu.horde.zerg.agents

import akka.actor.{Props, ActorRef, Actor}
import edu.gmu.horde.NewUnit

object MilitaryAgent {
  def props(env :ActorRef, store: ActorRef) = Props(new MilitaryAgent(env, store))
}

class MilitaryAgent(val env :ActorRef, val store: ActorRef) extends Actor {
  var platoon :ActorRef = context.actorOf(Props(classOf[Platoon], env))

  override def receive: Receive = {
    case n @ NewUnit(id, unit) =>
      platoon ! n
  }
}

package edu.gmu.horde.zerg.agents

import akka.actor.{Props, ActorRef, Actor}
import edu.gmu.horde.zerg.NewUnit

class MilitaryAgent extends Actor {

  var platoon :ActorRef = context.actorOf(Props[Platoon])

  override def receive: Receive = {
    case n @ NewUnit(id, unit) =>
      platoon ! n
  }
}

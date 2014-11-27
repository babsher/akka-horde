package edu.gmu.horde.zerg.agents

import akka.actor.{ActorRef, Actor}
import edu.gmu.horde.zerg.NewUnit

class MilitaryAgent extends Actor {

  var platoon :ActorRef = context.actorOf(Platoon.props)

  override def receive: Receive = {
    case n @ NewUnit(id, unit) =>
      platoon ! n
  }
}

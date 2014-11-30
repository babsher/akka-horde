package edu.gmu.horde

import akka.actor.{Props, ActorRef, Actor}
import akka.actor.Actor.Receive
import edu.gmu.horde.zerg.agents.{MilitaryAgent, ProductionAgent}

case class SetManagers(production :ActorRef, military :ActorRef)

class Root extends Actor {

  var miliatry :ActorRef = null
  var production :ActorRef = null
  var research :ActorRef = null

  override def receive: Receive = {
    case Run =>
      miliatry = context.actorOf(Props[MilitaryAgent])
      production = context.actorOf(Props[ProductionAgent])
    case msg @ _ =>
      miliatry ! msg
      production ! msg
  }
}

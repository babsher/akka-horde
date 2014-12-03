package edu.gmu.horde

import akka.actor.{Props, ActorRef, Actor}
import akka.actor.Actor.Receive
import edu.gmu.horde.zerg.NewUnit
import edu.gmu.horde.zerg.agents.{MilitaryAgent, ProductionAgent}

case class SetManagers(production :ActorRef, military :ActorRef)

class Root extends Actor {

  var miliatry :ActorRef = null
  var production :ActorRef = null
  var research :ActorRef = null
  var env :ActorRef = null

  override def receive: Receive = {
    case Run(connect, train) =>
      createManagers(connect, train)
    case SetEnvironment(e) =>
      env = e
    case msg @ NewUnit(id, u) =>
      production ! msg
    case msg @ _ =>
      production ! msg
      miliatry ! msg
  }

  def createManagers(connect :Boolean, train :Boolean) :Unit = {
    miliatry = context.actorOf(Props[MilitaryAgent])
    production = context.actorOf(Props[ProductionAgent])
    val set = SetManagers(production, miliatry)
    production ! set
    miliatry ! set
    context.children.map(child => child ! Train(train))
  }
}

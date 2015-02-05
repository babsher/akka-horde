package edu.gmu.horde.actors

import akka.actor.{Actor, ActorRef}
import edu.gmu.horde.zerg.NewUnit
import edu.gmu.horde.zerg.agents.{MilitaryAgent, ProductionAgent}

case class SetManagers(production :ActorRef, military :ActorRef)
case class NewAgent(id: String, agent: ActorRef, typeName: String)
case class SendAgents(sender: ActorRef)
case class Agents(agents: Seq[(String, String)])

class Root extends Actor {
  
  var agents = collection.mutable.Map[String, (ActorRef, String)]()
  
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
    case msg @ NewAgent(id, agent, typeName) =>
      agents += id -> (agent, typeName)
      context.parent ! msg
    case SendAgents() =>
      val a = for((id: String, (agent: ActorRef, typeName: String)) <- agents) yield List((id, typeName))
      context.sender() ! Agents(a flatMap identity toSeq)
    case msg @ _ =>
      production ! msg
      miliatry ! msg
  }

  def createManagers(connect :Boolean, train :Boolean) :Unit = {
    miliatry = context.actorOf(MilitaryAgent.props(env))
    production = context.actorOf(ProductionAgent.props(env))
    val set = SetManagers(production, miliatry)
    production ! set
    miliatry ! set
    context.children.map(child => child ! Train(train))
  }
}

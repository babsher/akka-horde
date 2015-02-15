package edu.gmu.horde.actors

import akka.actor.{Actor, ActorRef}
import edu.gmu.horde._
import edu.gmu.horde.zerg.agents.{MilitaryAgent, ProductionAgent}

class Root extends Actor {
  
  var agents = collection.mutable.Map[String, (ActorRef, String)]()
  
  var miliatry :ActorRef = null
  var production :ActorRef = null
  var research :ActorRef = null
  var env :ActorRef = null

  override def receive: Receive = {
    case Run(connect) =>
      createManagers(connect)
    case SetEnvironment(e) =>
      env = e
    case msg @ NewUnit(id, u) =>
      production ! msg
    case msg @ NewAgent(agent, typeName) =>
      agents += agent.path.toSerializationFormat -> (agent, typeName)
      context.parent ! msg
    case RequestAgentInfo(sender: ActorRef) =>
      val a = for((id: String, (agent: ActorRef, typeName: String)) <- agents) yield AgentInfo(id, typeName)
      sender ! AgentSummary(a toSeq)
    case msg @ _ =>
      production ! msg
      miliatry ! msg
  }

  def createManagers(connect :Boolean) :Unit = {
    miliatry = context.actorOf(MilitaryAgent.props(env))
    production = context.actorOf(ProductionAgent.props(env))
    val set = SetManagers(production, miliatry)
    production ! set
    miliatry ! set
  }
}

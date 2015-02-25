package edu.gmu.horde.actors

import akka.actor.{ActorLogging, Actor, ActorRef}
import edu.gmu.horde._
import edu.gmu.horde.zerg.agents.{MilitaryAgent, ProductionAgent}

class Root extends Actor with ActorLogging {
  
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
      log.debug("Got new agent {}", msg)
      agents += agent.path.toSerializationFormat -> (agent, typeName)
      context.parent ! msg
    case RequestAgentInfo(sender: ActorRef) =>
      val a = for((id: String, (agent: ActorRef, typeName: String)) <- agents) yield AgentInfo(id, typeName)
      sender ! AgentsSummary(a toSeq)
    case msg @ _ =>
      production ! msg
      miliatry ! msg
  }

  def createManagers(connect :Boolean) :Unit = {
    miliatry = context.actorOf(MilitaryAgent.props(env), "military")
    production = context.actorOf(ProductionAgent.props(env, context.self), "production")
    val set = SetManagers(production, miliatry)
    production ! set
    miliatry ! set
  }
}

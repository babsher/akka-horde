package edu.gmu.horde.actors

import akka.actor.{Props, ActorLogging, Actor, ActorRef}
import edu.gmu.horde._
import edu.gmu.horde.zerg.agents.{MilitaryAgent, ProductionAgent}

object Root {
  def props(store: ActorRef): Props = Props(new Root(store))
}

class Root(val store: ActorRef) extends Actor with ActorLogging with Messages {

  case class Agent(ref: ActorRef, parent: ActorRef, typeName: String, unitId: Option[Int])
  
  var agents = collection.mutable.Map[String, Agent]()
  
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
    case msg @ NewAgent(agent, parent, typeName, unitId) =>
      log.debug("Got new agent {}", msg)
      agents += agent.path.toSerializationFormat -> Agent(agent, parent, typeName, unitId)
      context.parent ! msg
    case RequestAgentInfo(sender: ActorRef) =>
      val a = 
        for((id: String, agent: Agent) <- agents)
          yield AgentInfo(agent.ref, agent.parent, agent.typeName, agent.unitId)
      sender ! AgentsSummary(a toSeq)
    case msg @ _ =>
      production ! msg
      miliatry ! msg
  }

  def createManagers(connect :Boolean) :Unit = {
    miliatry = context.actorOf(MilitaryAgent.props(env, store), "military")
    production = context.actorOf(ProductionAgent.props(env, context.self, store), "production")
    val set = SetManagers(production, miliatry)
    production ! set
    miliatry ! set
  }
}

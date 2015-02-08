package edu.gmu.horde

import akka.actor.ActorRef
import edu.gmu.horde.storage.AttributeValue
import jnibwapi.UnitCommand

sealed trait Messages
case class Subscribe(id: Int, ref: ActorRef)
case class Unsubscribe(ref: ActorRef, id: Option[Int] = None)
case class Publish(u: UnitUpdate)

case class UnitUpdate(id: Int, unit: jnibwapi.Unit)
case class UnitCmd(id: Int, cmd: UnitCommand)
case class OnFrame()
case class NewUnit(id: Int, unit: jnibwapi.Unit)
case class SetManagers(production :ActorRef, military :ActorRef)
case class NewAgent(agent: ActorRef, typeName: String)

case class Scenario(name: String)
case class SetEnvironment(env: ActorRef)
case class SetRoot(env: ActorRef)
case class SetAttributeStore(store: ActorRef)
case class Run(connect :Boolean, train :Boolean)
case object Stop

case class Train(train :Boolean)

case object RequestAgentDetail
case class AgentDetail(agent: ActorRef, agentType: String, features: Seq[AttributeValue])

case class RequestAgentInfo(sender: ActorRef)
case class AgentInfo(name: String, agentType: String)
case class AgentSummary(agents: Seq[AgentInfo])

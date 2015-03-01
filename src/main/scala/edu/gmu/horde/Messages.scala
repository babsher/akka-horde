package edu.gmu.horde

import akka.actor.ActorRef
import com.google.common.io.BaseEncoding
import edu.gmu.horde.storage.AttributeValue
import jnibwapi.UnitCommand

trait Messages {
  implicit def actorRefWrapper(a: ActorRef): String = {
    val path = a.path.toSerializationFormat
    new String(BaseEncoding.base64Url().encode(path.getBytes))
  }
  
  def serializePath(path: String): String = {
    new String(BaseEncoding.base64Url().encode(path.getBytes))
  }
}

case class Subscribe(id: Int, ref: ActorRef)
case class Unsubscribe(ref: ActorRef, id: Option[Int] = None)

case class Publish(u: UnitUpdate)
case class UnitUpdate(id: Int, unit: jnibwapi.Unit)
case class UnitCmd(id: Int, cmd: UnitCommand)
case class OnFrame()

case class NewUnit(id: Int, unit: jnibwapi.Unit)
case class NewAgent(agent: ActorRef, parent: ActorRef, typeName: String, unitId: Option[Int])

case class SetManagers(production: ActorRef, military: ActorRef)

case class Scenario(name: String)
case class SetEnvironment(env: ActorRef)

case class SetRoot(env: ActorRef)
case class SetAttributeStore(store: ActorRef)
case class Run(connect: Boolean)
case object Stop

case class Train(train: Boolean)

case object RequestAgentDetail
case class AgentDetail(name: String, agentType: String, currentState: State, states: Seq[AgentPossibleStates], features: Map[String, AttributeValue])
case class RequestAgentInfo(sender: ActorRef)

case class AgentInfo(name: String, parent: String, agentType: String, unitId: Option[Int])
case class AgentsSummary(agents: Seq[AgentInfo])

case object RequestState
case class State(state: String)
case class AgentPossibleStates(state: String, nextState: Boolean, isCurrent: Boolean)
case class HordeState(horde: String, state: State, root: String)
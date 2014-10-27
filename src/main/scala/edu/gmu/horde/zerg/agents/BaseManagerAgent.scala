package edu.gmu.horde.zerg.agents

import akka.actor.{Props, ActorRef, Actor}
import edu.gmu.horde.zerg.NewUnit
import jnibwapi.types.UnitType
import jnibwapi.Position

class BaseManagerAgent extends Actor {
  var military :ActorRef = ???

  override def receive: Receive = {
    case SetManagers(production: ActorRef, military: ActorRef) =>
      this.military = military
    case NewUnit(id: Int, unit: jnibwapi.Unit) =>
      if(unit.getType == UnitType.UnitTypes.Zerg_Drone) {
        AgentRef a = getBaseAgent(unit.getPosition)
      } else {

      }
  }

  def getBaseAgent(pos: Position) :ActorRef = {
    context.actorOf(Props[BaseManagerAgent])
  }
}

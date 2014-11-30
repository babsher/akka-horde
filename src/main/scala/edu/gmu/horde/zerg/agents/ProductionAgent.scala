package edu.gmu.horde.zerg.agents

import akka.actor.{Props, ActorRef, Actor}
import edu.gmu.horde.SetManagers
import edu.gmu.horde.zerg.NewUnit
import jnibwapi.Position
import jnibwapi.types.UnitType

class ProductionAgent extends Actor {

  var military :ActorRef = null

  override def receive: Receive = {
    case SetManagers(production: ActorRef, military: ActorRef) =>
      this.military = military
    case NewUnit(id: Int, unit: jnibwapi.Unit) =>

      if(unit.getType == UnitType.UnitTypes.Zerg_Drone) {
        val a = getBaseAgent(unit.getPosition)
        a ! NewUnit(id, unit)
      } else {
        military ! NewUnit(id, unit)
      }
  }

  def getBaseAgent(pos: Position) :ActorRef = {
    context.actorOf(Props[BaseManagerAgent])
  }
}

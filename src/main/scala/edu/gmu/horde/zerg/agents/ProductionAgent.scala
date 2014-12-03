package edu.gmu.horde.zerg.agents

import akka.actor.{Props, ActorRef, Actor}
import edu.gmu.horde.{Train, SetManagers}
import edu.gmu.horde.zerg.NewUnit
import jnibwapi.Position
import jnibwapi.types.UnitType
import jnibwapi.types.UnitType.UnitTypes

class ProductionAgent extends Actor {

  var military :ActorRef = null
  var larva :Set[Int] = Set[Int]()

  override def receive: Receive = {
    case SetManagers(production: ActorRef, military: ActorRef) =>
      this.military = military
    case NewUnit(id: Int, unit: jnibwapi.Unit) =>
      if(unit.getPlayer.isSelf) {
        unit.getType match {
          case UnitTypes.Zerg_Larva =>
            larva = larva + id
          case UnitType.UnitTypes.Zerg_Drone =>
            val a = getBaseAgent(unit.getPosition)
            a ! NewUnit(id, unit)
          case _ =>
            military ! NewUnit(id, unit)
        }
      }
    case Train(train) =>
      context.children.map(child => child ! Train(train))
  }

  def getBaseAgent(pos: Position) :ActorRef = {
    context.actorOf(Props[BaseManagerAgent])
  }
}

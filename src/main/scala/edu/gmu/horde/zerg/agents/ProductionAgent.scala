package edu.gmu.horde.zerg.agents

import akka.actor.{ActorLogging, Props, ActorRef, Actor}
import edu.gmu.horde.actors.{NewAgent, SetManagers, Train}
import edu.gmu.horde.zerg.env.{MorphLarva, Supply}
import edu.gmu.horde.zerg.{OnFrame, NewUnit}
import jnibwapi.Position
import jnibwapi.types.UnitType
import jnibwapi.types.UnitType.UnitTypes

object ProductionAgent {
  def props(env :ActorRef) = Props(new ProductionAgent(env))
}

class ProductionAgent(env :ActorRef) extends Actor with ActorLogging {
  var military :ActorRef = null
  var larva :Seq[Int] = Seq[Int]()
  var supply = 0
  var totalSupply = 0

  override def receive: Receive = {
    case SetManagers(production: ActorRef, military: ActorRef) =>
      this.military = military
    case msg @ Supply(used, total) =>
      supply = used
      totalSupply = total
      log.debug("Got supply " + msg)
    case NewUnit(id: Int, unit: jnibwapi.Unit) =>
      if(unit.getPlayer.isSelf) {
        unit.getType match {
          case UnitTypes.Zerg_Larva =>
            larva = larva :+ id
          case UnitType.UnitTypes.Zerg_Drone =>
            val a = getBaseAgent(unit.getPosition)
            a ! NewUnit(id, unit)
          case _ =>
            military ! NewUnit(id, unit)
        }
      }
    case OnFrame =>
      checkSupply()
      build()
    case Train(train) =>
      context.children.map(child => child ! Train(train))
    case msg @ NewAgent(_,_,_) =>
      context.parent ! msg
  }

  def build() = {
    if(!larva.isEmpty) {
      val id = larva(0)
      larva = larva.filter(el => el != id)
      env ! MorphLarva(id, UnitTypes.Zerg_Drone)
      log.debug("Larva {}", larva)
    }
  }

  def checkSupply() = {
    val gap = totalSupply - supply
    log.debug("Supply gap is {}", gap)
    if(gap < 4) {
      if(!larva.isEmpty) {
        val id = larva(0)
        larva = larva.filter(el => el != id)
        env ! MorphLarva(id, UnitTypes.Zerg_Overlord)
        log.debug("Larva {}", larva)
      }
    }
  }

  def getBaseAgent(pos: Position) :ActorRef = {
    context.actorOf(BaseManagerAgent.props(env))
  }
}

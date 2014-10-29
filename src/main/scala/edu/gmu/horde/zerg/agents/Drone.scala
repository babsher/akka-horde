package edu.gmu.horde.zerg.agents

import akka.actor.{ActorRef, FSM, Props}
import edu.gmu.horde.zerg.env.MoveToNearestMineral
import jnibwapi.{Unit => BUnit}

trait DroneStates
case object Harvest extends DroneStates
case object Start extends DroneStates
case object Moving extends DroneStates
case object Attacking extends DroneStates
case object Retreat extends DroneStates
case object Idle extends DroneStates

trait DroneFeatures
case object Uninitialized extends DroneFeatures
case object MoveTarget extends DroneFeatures

object Drone {
  def props(id: Int, unit: BUnit, env: ActorRef): Props = Props(new Drone(id, unit, env))
}

class Drone(id: Int, unit: BUnit, env: ActorRef) extends UnitAgent(id, unit, env) with FSM[DroneStates, DroneFeatures] {

  startWith(Start, Uninitialized)

  onTransition {
    case x -> y => log.debug("Entering " + y + " from " + x)
  }

  when(Idle) {
    case Event(Harvest, _) =>
      env ! MoveToNearestMineral(id)
      goto(Harvest)
  }

  initialize()
}

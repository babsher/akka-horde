package edu.gmu.horde.zerg.agents

import akka.actor.FSM

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

class Drone extends UnitAgent with FSM[DroneStates, DroneFeatures] {

  startWith(Start, Uninitialized)

  onTransition {
    case x -> y => log.debug("Entering " + y + " from " + x)
  }

  initialize()
}

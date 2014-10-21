package edu.gmu.horde.zerg.agents

import akka.actor.{FSM, _}
import edu.gmu.horde.zerg.{Activate, UnitUpdate}
import edu.gmu.horde.zerg.agents.UnitAgent
import jnibwapi.Unit

import scala.concurrent.duration._

object UnitAgent {
  trait States
  case object Start extends States
  case object Moving extends States
  case object Attacking extends States
  case object Retreat extends States
  case object Idle extends States

  trait Features
  case object Uninitialized extends Features
  case object MoveTarget extends Features
}

class UnitAgent extends Actor with FSM[UnitAgent.States, UnitAgent.Features] {
  import UnitAgent._

  var unit: jnibwapi.Unit = null

  startWith(Start, Uninitialized)

  when(Start, stateTimeout = 1 second) {
    case Event(Activate, _) =>
      goto(Moving)
  }

  when(Moving) {
    case Event(UnitUpdate(id: Integer, u: jnibwapi.Unit), _) =>
      unit = u
      if (unit.isMoving) {
        stay
      } else {
        goto(Idle)
      }
  }

  when(Attacking) {
    case Event(UnitUpdate(id: Integer, u: jnibwapi.Unit), _) =>
      unit = u
      if(unit.isAttacking) {
        stay
      } else {
        goto(Idle)
      }
  }

  onTransition {
    case x -> y => log.debug("Entering " + y + " from " + x)
  }
  initialize()
}
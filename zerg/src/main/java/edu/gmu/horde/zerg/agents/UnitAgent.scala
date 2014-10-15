package edu.gmu.horde.zerg.agents

import akka.actor.{FSM, _}

import scala.concurrent.duration._

object UnitAgent {
  trait States
  case object Start extends States
  case object Moving extends States
  case object Attacking extends States

  trait Features
  case object Uninitialized extends Features

  trait Messages
  case object Activate extends Messages
}

class UnitAgent extends Actor with FSM[UnitAgent.States, UnitAgent.Features] {
  import edu.gmu.horde.zerg.agents.UnitAgent._

  startWith(Start, Uninitialized)

  when(Start, stateTimeout = 1 second) {
    case Event(Activate, _) =>
      goto(Moving)
  }

  onTransition {
    case x -> y => log.debug("Entering " + y + " from " + x)
  }
  initialize()
}
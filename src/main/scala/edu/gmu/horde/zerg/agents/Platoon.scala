package edu.gmu.horde.zerg.agents

import akka.actor.{LoggingFSM, FSM, Actor}
import edu.gmu.horde.HordeAgentFSM


object Platoon {
  trait States
  case object Start extends States
  case object Moving extends States
  case object Attacking extends States
  case object Idle extends States

  trait Features
  case object Uninitialized extends Features
  case object MoveTarget extends Features
}

class Platoon extends Actor with LoggingFSM[Platoon.States, Platoon.Features] with HordeAgentFSM[Platoon.States, Platoon.Features] {

  from(Platoon.Start) {
    Seq(
      To(Platoon.Moving, action(Platoon.Start, Platoon.Moving)),
      To(Platoon.Attacking, action(Platoon.Start, Platoon.Attacking))
    )
  }

  initialize

  private def action(fromState: Platoon.States, toState: Platoon.States): Function1[Event, Unit] = {
    case Event(nextState: Platoon.States, _) =>
    case _ =>
  }

  private def onState(currentState: Platoon.States, message: Platoon.States) = {
    stay
  }
}

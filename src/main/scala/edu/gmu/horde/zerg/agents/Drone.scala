package edu.gmu.horde.zerg.agents

import akka.actor.{LoggingFSM, ActorRef, FSM, Props}
import edu.gmu.horde.HordeAgentFSM
import edu.gmu.horde.zerg.env.MoveToNearestMineral
import jnibwapi.{Unit => BUnit}

object Drone {

  trait States
  case object Harvest extends States
  case object Start extends States
  case object Moving extends States
  case object Attacking extends States
  case object Retreat extends States
  case object Idle extends States

  trait Features
  case object Uninitialized extends Features
  case object MoveTarget extends Features
  def props(id: Int, unit: BUnit, env: ActorRef): Props = Props(new Drone(id, unit, env))
}

class Drone(id: Int, unit: BUnit, env: ActorRef) extends UnitAgent(id, unit, env) with LoggingFSM[Drone.States, Drone.Features] with HordeAgentFSM[Drone.States, Drone.Features] {

  import Drone._


  startWith(Start, Uninitialized)

  onTransition {
    case x -> y => log.debug("Entering " + y + " from " + x)
  }

  when(Idle) {
    case Event(Harvest, _) =>
      env ! MoveToNearestMineral(id)
      goto(Harvest)
  }

  initialize

  from(Start) {
    Seq(
      To(Moving, action(Start, Moving)),
      To(Attacking, action(Start, Attacking))
    )
  }

  private def action(fromState: States, toState: States): Function1[Event, Unit] = {
    case Event(nextState: States, _) =>
    case _ =>
  }

  private def onState(currentState: States, message: States) = {
    stay
  }
}

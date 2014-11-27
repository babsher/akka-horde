package edu.gmu.horde.zerg.agents

import akka.actor.{LoggingFSM, ActorRef, Props}
import akka.actor.FSM.Event
import edu.gmu.horde.zerg.UnitFeatures
import edu.gmu.horde.{AgentState, DoubleValue, AttributeValue, HordeAgentFSM}
import edu.gmu.horde.zerg.env.MoveToNearestMineral
import jnibwapi.{Unit => BUnit}
import weka.core.Attribute


object Drone {

  trait States extends AgentState[Drone] with UnitFeatures
  case object Harvest extends States {
    override def attributes(): Seq[Attribute] = Seq(new Attribute(""))
    override def name() : String = "Harvest"
    override def features(d : Drone) : Map[String, AttributeValue] = {
        implicit val unit = d.asInstanceOf[Drone].unit
        Map(UnitPosition)
    }
  }
  case object Start extends States {
    override def attributes(): Seq[Attribute] = Seq(new Attribute(TrueFeatureName))

    override def features(d: Platoon): Map[String, AttributeValue] = {
      TrueFeature()
    }

    override def name(): String = "Start"
  }
  case object Moving extends States
  case object Attacking extends States
  case object Retreat extends States
  case object Idle extends States

  trait Features
  case object Uninitialized extends Features
  case object MoveTarget extends Features
  def props(id: Int, unit : BUnit, env: ActorRef): Props = Props(new Drone(id, unit, env))
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

  private def action(fromState: States, toState: States): (Event) => Unit = {
    case Event(nextState: States, _) =>
    case _ =>
  }

  private def onState(currentState: States, message: States) = {
    stay
  }
}

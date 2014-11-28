package edu.gmu.horde.zerg.agents

import akka.actor.{Props, LoggingFSM, FSM, Actor}
import edu.gmu.horde._
import weka.core.Attribute

object Platoon {

  def props : Props = {
    Props[Platoon]
  }

  trait States extends AgentState with SimpleFeatures {
    override def features(d : AnyRef) = {
      d match {
        case Platoon => features(d.asInstanceOf[Drone])
      }
    }
    def features(d : Platoon) : Map[String, AttributeValue]
  }
  case object Start extends States {
    override def attributes(): Seq[Attribute] = Seq(new Attribute(TrueFeatureName))

    override def features(d: Platoon): Map[String, AttributeValue] = {
      Map(TrueFeature)
    }

    override def name(): String = super.name()
  }
  case object Moving extends States {
    override def attributes(): Seq[Attribute] = Seq(new Attribute(TrueFeatureName))

    override def features(d: Platoon): Map[String, AttributeValue] = {
      Map(TrueFeature)
    }

    override def name(): String = "Moving"
  }
  case object Attacking extends States {
    override def attributes(): Seq[Attribute] = Seq(new Attribute(TrueFeatureName))

    override def features(d: Platoon): Map[String, AttributeValue] = {
      Map(TrueFeature)
    }

    override def name(): String = "Attacking"
  }
  case object Idle extends States {
    override def attributes(): Seq[Attribute] = Seq(new Attribute(TrueFeatureName))

    override def features(d: Platoon): Map[String, AttributeValue] = {
      Map(TrueFeature)
    }

    override def name(): String = "Idle"
  }

  trait Features
  case object Uninitialized extends Features
  case object MoveTarget extends Features
}

class Platoon extends Actor with LoggingFSM[Platoon.States, Platoon.Features] with HordeAgentFSM[Platoon.States, Platoon.Features] {

  import Platoon._

  startWith(Platoon.Start, Uninitialized)

  onTransition {
    case x -> y => log.debug("Entering " + y + " from " + x)
  }

  from(Platoon.Start) {
    Seq(
      To(Platoon.Idle, action(Platoon.Start, Platoon.Idle))
    )
  }

  from(Platoon.Moving) {
    Seq(
      To(Platoon.Attacking, action(Platoon.Moving, Platoon.Attacking)),
      To(Platoon.Idle, action(Platoon.Moving, Platoon.Idle))
    )
  }

  from(Platoon.Attacking) {
    Seq(
      To(Platoon.Moving, action(Platoon.Attacking, Platoon.Moving)),
      To(Platoon.Idle, action(Platoon.Attacking, Platoon.Idle))
    )
  }

  from(Platoon.Idle) {
    Seq(
      To(Platoon.Moving, action(Platoon.Idle, Platoon.Moving)),
      To(Platoon.Attacking, action(Platoon.Idle, Platoon.Moving))
    )
  }

  initialize

  private def action(fromState: Platoon.States, toState: Platoon.States): (Event) => Unit = {
    case Event(nextState: Platoon.States, _) =>
    case _ =>
  }

  private def onState(currentState: Platoon.States, message: Platoon.States) = {
    stay
  }
}

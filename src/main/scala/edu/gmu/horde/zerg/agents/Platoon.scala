package edu.gmu.horde.zerg.agents

import akka.actor._
import edu.gmu.horde._
import weka.core.Attribute

object Platoon {
  trait States extends AgentState with SimpleFeatures {

    override def features(d : AnyRef) = {
      feature(d.asInstanceOf[Platoon])
    }
    def feature(d : Platoon) : Map[String, AttributeValue]
  }
  case object Start extends States {
    override def attributes(): Seq[Attribute] = Seq(new Attribute(TrueFeatureName))
    override def name(): String = "Start"
    override def feature(d: Platoon): Map[String, AttributeValue] = {
      Map(TrueFeature)
    }
  }
  case object Moving extends States {
    override def attributes(): Seq[Attribute] = Seq(new Attribute(TrueFeatureName))
    override def name(): String = "Moving"
    override def feature(d: Platoon): Map[String, AttributeValue] = {
      Map(TrueFeature)
    }
  }
  case object Attacking extends States {
    override def attributes(): Seq[Attribute] = Seq(new Attribute(TrueFeatureName))
    override def name(): String = "Attacking"
    override def feature(d: Platoon): Map[String, AttributeValue] = {
      Map(TrueFeature)
    }
  }
  case object Idle extends States {
    override def attributes(): Seq[Attribute] = Seq(new Attribute(TrueFeatureName))
    override def name(): String = "Idle"
    override def feature(d: Platoon): Map[String, AttributeValue] = {
      Map(TrueFeature)
    }
  }
  // TODO add retreat

  trait Features
  case object Uninitialized extends Features
  case object MoveTarget extends Features
}

class Platoon(val envRef :ActorRef) extends Actor with LoggingFSM[Platoon.States, Platoon.Features] with HordeAgentFSM[Platoon.States, Platoon.Features] {
  override var env: ActorRef = envRef
  import Platoon._

  startWith(Start, Uninitialized)

  override def states = Start :: Moving :: Attacking :: Idle :: Nil

  onTransition {
    case x -> y => log.debug("Entering " + y + " from " + x)
  }

  from(Start) {
    Seq(
      To(Idle, action(Start, Idle))
    )
  }

  from(Moving) {
    Seq(
      To(Attacking, action(Moving, Attacking)),
      To(Idle, action(Moving, Idle))
    )
  }

  from(Attacking) {
    Seq(
      To(Moving, action(Attacking, Moving)),
      To(Idle, action(Attacking, Idle))
    )
  }

  from(Idle) {
    Seq(
      To(Moving, action(Idle, Moving)),
      To(Attacking, action(Idle, Moving))
    )
  }

  initialize

  private def action(fromState: States, toState: States): (Event) => Unit = {
    case Event(nextState: States, _) =>
    case _ =>
  }

  private def onState(currentState: States, message: States) = {
    stay
  }
}
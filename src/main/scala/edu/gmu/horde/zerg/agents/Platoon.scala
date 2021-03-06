package edu.gmu.horde.zerg.agents

import akka.actor._
import edu.gmu.horde._
import edu.gmu.horde.actors.{Action, AgentState, HordeAgentFSM}
import edu.gmu.horde.features.SimpleFeatures
import edu.gmu.horde.storage.AttributeValue
import weka.core.Attribute

object Platoon {
  trait States extends AgentState with SimpleFeatures {
    def features(d: Platoon): Map[String, AttributeValue]
    def attributes: Seq[Attribute]
  }
  case object Start extends States {
    override def attributes(): Seq[Attribute] = Seq(new Attribute(TrueFeatureName))
    override def name(): String = "Start"
    override def features(d: Platoon): Map[String, AttributeValue] = {
      Map(TrueFeature)
    }
  }
  case object Moving extends States {
    override def attributes(): Seq[Attribute] = Seq(new Attribute(TrueFeatureName))
    override def name(): String = "Moving"
    override def features(d: Platoon): Map[String, AttributeValue] = {
      Map(TrueFeature)
    }
  }
  case object Attacking extends States {
    override def attributes(): Seq[Attribute] = Seq(new Attribute(TrueFeatureName))
    override def name(): String = "Attacking"
    override def features(d: Platoon): Map[String, AttributeValue] = {
      Map(TrueFeature)
    }
  }
  case object Idle extends States {
    override def attributes(): Seq[Attribute] = Seq(new Attribute(TrueFeatureName))
    override def name(): String = "Idle"
    override def features(d: Platoon): Map[String, AttributeValue] = {
      Map(TrueFeature)
    }
  }
  // TODO add retreat

  trait Features
  case object Uninitialized extends Features
  case object MoveTarget extends Features
  def props(env: ActorRef, store: ActorRef): Props = Props(new Platoon(env, store))
}

class Platoon(override val env: ActorRef, override val attributeStore: ActorRef) extends Actor with LoggingFSM[Platoon.States, Platoon.Features] with HordeAgentFSM[Platoon.States, Platoon.Features] {
  import Platoon._

  startWith(Start, Uninitialized)

  override def states = Start :: Moving :: Attacking :: Idle :: Nil

  from(Start) {
    Idle :: Nil
  }

  from(Moving) {
    Attacking :: Idle :: Nil
  }

  from(Attacking) {
    Moving :: Idle :: Nil
  }

  from(Idle) {
    Moving :: Attacking :: Nil
  }

  initialize

  private def action(fromState: States, toState: States): (Event) => Unit = {
    case Event(nextState: States, _) =>
    case _                           =>
  }

  private def onState(currentState: States, message: States) = {
    stay
  }

  override def getAction(state: States): Action = ???

  override def features(state: States): Map[String, AttributeValue] = ???

  override def attributes(state: States): Seq[Attribute] = ???

  override def getType: String = Platoon.getClass.getSimpleName
}
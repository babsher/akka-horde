package edu.gmu.horde.zerg.agents

import akka.actor.{LoggingFSM, ActorRef, Props}
import edu.gmu.horde.zerg.UnitFeatures
import edu.gmu.horde._
import edu.gmu.horde.zerg.env.{BuildBuilding, MoveToNearestMineral}
import jnibwapi.{Unit => BUnit}
import weka.core.Attribute


object Drone {

  trait States extends AgentState with UnitFeatures with SimpleFeatures {
    override def features(d : AnyRef) = {
      d match {
        case Drone => features(d.asInstanceOf[Drone])
      }
    }
    def features(d : Drone) : Map[String, AttributeValue]
  }
  case object Harvest extends States {
    override def attributes(): Seq[Attribute] = Seq(new Attribute(UnitPositionXName), new Attribute(UnitPositionYName))
    override def name() : String = "Harvest"
    override def features(d : Drone) : Map[String, AttributeValue] = {
        implicit val unit = d.asInstanceOf[Drone].unit
        Map(UnitPositionX, UnitPositionY)
    }
  }
  case object Start extends States {
    override def attributes(): Seq[Attribute] = Seq(new Attribute(TrueFeatureName))
    override def name(): String = "Start"
    override def features(d: Drone): Map[String, AttributeValue] = {
      Map(TrueFeature)
    }
  }
  case object Moving extends States {
    override def attributes(): Seq[Attribute] = Seq(new Attribute(TrueFeatureName))
    override def name(): String = "Moving"
    override def features(d: Drone): Map[String, AttributeValue] = {
      Map(TrueFeature)
    }
  }
  case object Attacking extends States {
    override def attributes(): Seq[Attribute] = Seq(new Attribute(TrueFeatureName))
    override def name(): String = "Attacking"
    override def features(d: Drone): Map[String, AttributeValue] = {
      Map(TrueFeature)
    }
  }
  case object Retreat extends States {
    override def attributes(): Seq[Attribute] = Seq(new Attribute(TrueFeatureName))
    override def name(): String = "Retreat"
    override def features(d: Drone): Map[String, AttributeValue] = {
      Map(TrueFeature)
    }
  }
  case object Build extends States {
    override def attributes(): Seq[Attribute] = Seq(new Attribute(TrueFeatureName))
    override def name(): String = "Retreat"
    override def features(d: Drone): Map[String, AttributeValue] = {
      Map(TrueFeature)
    }
  }
  case object Idle extends States {
    override def attributes(): Seq[Attribute] = Seq(new Attribute(TrueFeatureName))
    override def name(): String = "Idle"
    override def features(d: Drone): Map[String, AttributeValue] = {
      Map(TrueFeature)
    }
  }

  trait Features
  case object Uninitialized extends Features

  def props(id: Int, unit : BUnit, env: ActorRef): Props = Props(new Drone(id, unit, env))
}

class Drone(id: Int, unit: BUnit, env: ActorRef) extends UnitAgent(id, unit, env) with LoggingFSM[Drone.States, Drone.Features] with HordeAgentFSM[Drone.States, Drone.Features] {

  import Drone._

  startWith(Start, Uninitialized)

  when(Idle) {
    case Event(Harvest, _) =>
      env ! MoveToNearestMineral(id)
      goto(Harvest)
  }

  from(Start) {
    Seq(
      To(Moving, action(Start, Moving)),
      To(Attacking, action(Start, Attacking)),
      To(Harvest, action(Start, Harvest))
    )
  }

  from(Moving) {
    Seq(
      To(Harvest, action(Moving, Harvest)),
      To(Attacking, action(Moving, Harvest))
    )
  }

  initialize

  private def action(fromState: States, toState: States): (Event) => Unit = {
    case Event(BuildBuilding(unitId, buildingType, region), _) =>
      env ! BuildBuilding(id, buildingType, region)
    case _ =>
  }

  private def onState(currentState: States, message: States) = {
    stay
  }
}

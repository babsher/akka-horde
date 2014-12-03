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

  def props(id: Int, unit : BUnit, env :ActorRef): Props = Props(new Drone(id, unit, env))
}

class Drone(id: Int, unit: BUnit, val envRef :ActorRef) extends UnitAgent(id, unit) with LoggingFSM[Drone.States, Drone.Features] with HordeAgentFSM[Drone.States, Drone.Features] {
  override var env: ActorRef = envRef
  log.debug("Created drone {}", id)
  import Drone._

  override def states = Drone.Idle :: Drone.Start :: Drone.Build :: Drone.Moving :: Drone.Retreat :: Nil

  startWith(Drone.Start, Drone.Uninitialized)

  from(Drone.Idle) {
    Seq(
      To(Drone.Moving, action(Drone.Idle, Drone.Moving)),
      To(Drone.Build, action(Drone.Idle, Drone.Build)),
      To(Drone.Harvest, action(Drone.Idle, Drone.Harvest))
    )
  }

  from(Drone.Start) {
    Seq(
      To(Drone.Moving, action(Drone.Start, Drone.Moving)),
      To(Drone.Attacking, action(Drone.Start, Drone.Attacking)),
      To(Drone.Harvest, action(Drone.Start, Drone.Harvest))
    )
  }

  from(Drone.Moving) {
    Seq(
      To(Drone.Harvest, action(Drone.Moving, Drone.Harvest)),
      To(Drone.Attacking, action(Drone.Moving, Drone.Attacking))
    )
  }

  from(Drone.Harvest) {
    Seq(
      To(Drone.Moving, action(Drone.Harvest, Drone.Moving)),
      To(Drone.Attacking, action(Drone.Harvest, Drone.Attacking))
    )
  }
  initialize

  private def action(fromState: Drone.States, toState: Drone.States): (Event) => Unit = {
    (fromState, toState) match {
      case Start -> Idle => startAction
      case _ -> Build => buildAction
      case _ -> Harvest => harvestAction
      case _ -> Moving => moveAction
      case tran @ _ => (e) => log.debug("Unhandled transition {}, Event: {}", tran, e)
    }
  }

  private def startAction :(Event) => Unit = {
    case Event(Harvest, _) =>
      log.debug("Going to harvest")
      goto(Harvest)
  }

  private def moveAction :(Event) => Unit = {
    case Event(Moving,_) => println("moving")
  }

  private def buildAction :(Event) => Unit = {
    case Event(BuildBuilding(unitId, buildingType, region), _) =>
      env ! BuildBuilding(id, buildingType, region)
  }

  private def harvestAction :(Event) => Unit = {
    case Event(Harvest, _) => println("Harvest")
  }

  onTransition {
    case _ -> Harvest =>
      env ! MoveToNearestMineral(id)
  }
}

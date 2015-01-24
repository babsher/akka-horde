package edu.gmu.horde.zerg.agents

import akka.actor.{ActorRef, LoggingFSM, Props}
import edu.gmu.horde._
import edu.gmu.horde.actors.{AgentState, HordeAgentFSM}
import edu.gmu.horde.features.{UnitFeatures, SimpleFeatures}
import edu.gmu.horde.storage.AttributeValue
import edu.gmu.horde.zerg.Subscribe
import edu.gmu.horde.zerg.env.{AttackNearest, BuildBuilding, MoveToNearestMineral}
import jnibwapi.{Unit => BUnit}
import weka.core.Attribute


object Drone {

  trait States extends AgentState with UnitFeatures with SimpleFeatures {
    override def features(d: AnyRef) = {
      d match {
        case Drone => features(d.asInstanceOf[Drone])
      }
    }
    def features(d: Drone): Map[String, AttributeValue]
  }

  case object Harvest extends States {
    override def attributes(): Seq[Attribute] = Seq(new Attribute(UnitPositionXName), new Attribute(UnitPositionYName))
    override def name(): String = "Harvest"
    override def features(d: Drone): Map[String, AttributeValue] = {
      implicit val unit = d.unit
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

  case object Attacking extends States {
    override def attributes(): Seq[Attribute] = Seq(new Attribute(TrueFeatureName))
    override def name(): String = "Attacking"
    override def features(d: Drone): Map[String, AttributeValue] = {
      implicit val unit = d.unit
      implicit val env = d.env
      Map(EnemyDistance)
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
  def props(id: Int, unit: BUnit, env: ActorRef): Props = Props(new Drone(id, unit, env))
}

class Drone(val id: Int, var unit: BUnit, val envRef: ActorRef) extends HordeAgentFSM[Drone.States, Drone.Features]
  with LoggingFSM[Drone.States, Drone.Features] {

  override var env: ActorRef = envRef
  env ! Subscribe(id, this.context.self)
  log.debug("Created drone {}", id)

  import edu.gmu.horde.zerg.agents.Drone._

  override def states = Drone.Idle :: Drone.Start :: Drone.Build :: Drone.Attacking :: Drone.Harvest :: Drone.Retreat :: Nil

  startWith(Drone.Start, Drone.Uninitialized)

  from(Drone.Idle) {
    Seq(
      To(Drone.Build, action(Drone.Idle, Drone.Build)),
      To(Drone.Harvest, action(Drone.Idle, Drone.Harvest))
    )
  }

  from(Drone.Start) {
    Seq(
      To(Drone.Attacking, action(Drone.Start, Drone.Attacking)),
      To(Drone.Harvest, action(Drone.Start, Drone.Harvest)),
      To(Drone.Build, action(Drone.Start, Drone.Build))
    )
  }

  from(Drone.Harvest) {
    Seq(
      To(Drone.Attacking, action(Drone.Harvest, Drone.Attacking)),
      To(Drone.Build, action(Drone.Harvest, Drone.Build))
    )
  }

  from(Drone.Attacking) {
    Seq(
      To(Drone.Harvest, action(Drone.Attacking, Drone.Harvest)),
      To(Drone.Build, action(Drone.Attacking, Drone.Build))
    )
  }

  from(Drone.Build) {
    Seq(
      To(Drone.Idle, action(Drone.Build, Drone.Idle))
    )
  }
  initialize

  private def action(fromState: Drone.States, toState: Drone.States): (Event) => Unit = {
    (fromState, toState) match {
      case Start -> Idle => startAction
      case _ -> Build => buildAction
      case _ -> Harvest => harvestAction
      case _ -> Attacking => attackAction
      case tran@_ => (e) => log.debug("Unhandled transition {}, Event: {}", tran, e)
    }
  }

  private def attackAction: (Event) => Unit = {
    case Event(Harvest, _) => println("Harvest")
  }

  private def startAction: (Event) => Unit = {
    case Event(Harvest, _) =>
      log.debug("Going to harvest")
      goto(Harvest)
  }

  private def buildAction: (Event) => Unit = {
    case Event(BuildBuilding(unitId, buildingType, region), _) =>
      env ! BuildBuilding(id, buildingType, region)
  }

  private def harvestAction: (Event) => Unit = {
    case Event(Harvest, _) => println("Harvest")
  }

  onTransition {
    case _ -> Harvest =>
      env ! MoveToNearestMineral(id)
    case _ -> Attacking =>
      env ! AttackNearest(id)
  }
}

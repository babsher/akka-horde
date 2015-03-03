package edu.gmu.horde.zerg.agents

import akka.actor.{ActorRef, LoggingFSM, Props}
import edu.gmu.horde._
import edu.gmu.horde.actors.{Action, AgentState, HordeAgentFSM}
import edu.gmu.horde.features.{SimpleFeatures, UnitFeatures}
import edu.gmu.horde.storage.AttributeValue
import edu.gmu.horde.zerg.env.{BuildBuilding, MoveToNearestMineral}
import jnibwapi.{Unit => BUnit}
import weka.core.Attribute

object Drone {

  trait States extends AgentState with UnitFeatures with SimpleFeatures {
    def features(d: Drone): Map[String, AttributeValue]
    def attributes: Seq[Attribute]
  }

  case object Harvest extends States {
    override def attributes: Seq[Attribute] = Seq(new Attribute(UnitPositionXName), new Attribute(UnitPositionYName))
    override def name(): String = "Harvest"
    override def features(d: Drone): Map[String, AttributeValue] = {
      implicit val unit = d.unit
      Map(UnitPositionX, UnitPositionY)
    }
  }

  case object Start extends States {
    override def attributes: Seq[Attribute] = Seq(new Attribute(TrueFeatureName))
    override def name(): String = "Start"
    override def features(d: Drone): Map[String, AttributeValue] = {
      Map(TrueFeature)
    }
  }

  case object Attacking extends States {
    override def attributes: Seq[Attribute] = Seq(new Attribute(TrueFeatureName))
    override def name(): String = "Attacking"
    override def features(d: Drone): Map[String, AttributeValue] = {
      implicit val unit = d.unit
      implicit val env = d.env
      Map(EnemyDistance)
    }
  }

  case object Retreat extends States {
    override def attributes: Seq[Attribute] = Seq(new Attribute(TrueFeatureName))
    override def name(): String = "Retreat"
    override def features(d: Drone): Map[String, AttributeValue] = {
      Map(TrueFeature)
    }
  }

  case object Build extends States {
    override def attributes: Seq[Attribute] = Seq(new Attribute(TrueFeatureName))
    override def name(): String = "Retreat"
    override def features(d: Drone): Map[String, AttributeValue] = {
      Map(TrueFeature)
    }
  }

  case object Idle extends States {
    override def attributes: Seq[Attribute] = Seq(new Attribute(TrueFeatureName))
    override def name(): String = "Idle"
    override def features(d: Drone): Map[String, AttributeValue] = {
      Map(TrueFeature)
    }
  }

  trait Features
  case object Uninitialized extends Features
  def props(id: Int, unit: BUnit, env: ActorRef): Props = Props(new Drone(id, unit, env))
  def id(id: Int): String = { "Drone-" + id}
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
    Drone.Build :: Drone.Harvest :: Nil
  }

  from(Drone.Start) {
    Drone.Idle ::
    Drone.Attacking ::
    Drone.Harvest ::
    Drone.Build :: Nil
  }

  from(Drone.Harvest) {
    Drone.Attacking :: Drone.Build :: Nil
  }

  from(Drone.Attacking) {
    Drone.Harvest :: Drone.Build :: Nil
  }
  from(Drone.Build) {
    Drone.Idle :: Nil
  }
  initialize

  private def attackAction(): Unit = {
    println("Attack")
  }

  def startAction(): Unit = {
    goto(Harvest)
  }

  private def buildAction(): Unit = {
    val buildingType = null
    val region = null
    env ! BuildBuilding(id, buildingType, region)
  }

  private def harvestAction(): Unit = {
    env ! MoveToNearestMineral(id)
  }

  def attributes(state: Drone.States): Seq[Attribute] = {
    state.attributes
  }

  def features(state: Drone.States): Map[String, AttributeValue] = {
    state.features(this)
  }

  def getAction(state: Drone.States): Action = state match {
    case Start =>     Action(() => {}, () => {startAction()}, () => {})
    case Harvest =>   Action(() => {harvestAction()}, () => {}, () => {})
    case Attacking => Action(() => {attackAction()}, () => {}, () => {})
    case Build =>     Action(() => {buildAction()}, () => {}, () => {})
    case default => 
      log.debug("Cound not find action for state: %s", state)
      NullAction
  }

  override def getType: String = Drone.getClass.getSimpleName
}

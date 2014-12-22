package edu.gmu.horde.zerg.env

import java.util.concurrent.TimeUnit

import akka.actor._
import edu.gmu.horde.env.BWInterface
import edu.gmu.horde.{Run, SetRoot, Environment}
import edu.gmu.horde.zerg.{NewUnit, OnFrame, UnitCmd}
import jnibwapi.types.UnitCommandType.UnitCommandTypes
import jnibwapi.types.UnitType
import jnibwapi.{Unit => BUnit, JNIBWAPI, Region, UnitCommand, Position}
import jnibwapi.types.UnitType.UnitTypes
import org.slf4j.LoggerFactory
import scala.collection.JavaConverters._
import scala.concurrent.duration._

object ZergEnvironment {
  val log = LoggerFactory.getLogger(ZergEnvironment.getClass())
  val mineralTypes = Set(UnitTypes.Resource_Mineral_Field)
}

case class MoveToNearestMineral(id :Int)
case class AttackNearest(id :Int)
case class BuildBuilding(id :Int, buildingType :UnitType, region :Region)
case class Supply(used :Int, total :Int)
case object Tick

trait HordeCommand {
  def run(bwapi :JNIBWAPI)
}
case class RightClickTarget(id :Int, target :Int) extends HordeCommand {
  override def run(bwapi :JNIBWAPI) : Unit = {
    val u = bwapi.getUnit(id)
    val targetUnit = bwapi.getUnit(target)
    u.rightClick(targetUnit, false)
  }
}
case class MorphLarva(id :Int, morphType :UnitType) extends HordeCommand {
  override def run(bwapi :JNIBWAPI) = {
    val u = bwapi.getUnit(id)
    u.morph(morphType)
  }
}
case class AttackUnit(id :Int, targetId :Int) extends HordeCommand {
  override def run(bwapi :JNIBWAPI) :Unit = {
    val u = bwapi.getUnit(id)
    val target = bwapi.getUnit(targetId)
    u.attack(target, false)
  }
}

class ZergEnvironment extends Environment {
  import ZergEnvironment._
  implicit val dispatcher = context.system.dispatcher
  val game = new BWInterface(context.self)

  override def receive =
    super.receive orElse {
      case Run(connect, train) =>
        if(connect) {
          log.debug("Connecting to game")
          game.start()
        }
        context.system.scheduler.schedule(1 seconds, 100 milliseconds, context.self, OnFrame)
      case OnFrame =>
        while (!game.newUnits.isEmpty()) {
          val id = game.newUnits.remove()
          log.debug("Found new units: {}", id)
          root ! NewUnit(id, game.units.get(id))
        }
        root ! Supply(game.currentSupply.get(), game.supplyCap.get())
        root ! OnFrame
      case MoveToNearestMineral(id: Int) =>
        val unit = game.bwapi.getUnit(id)
        val nearest = game.bwapi.getNeutralUnits.asScala.
          filter(mineralTypes contains _.getType).
          min(Ordering.by((m: BUnit) => m.getPosition.getApproxWDistance (unit.getPosition)))
        log.debug("Sending {} to nearest mineral {}", id, nearest.getPosition)
        game.commands add RightClickTarget(id, nearest.getID)
        log.debug("Commands {} size {}", game.commands, game.commands.size)
      case BuildBuilding(id :Int, buildingType :UnitType, region :Region) =>
        val unit = game.bwapi.getUnit(id)
        // TODO compute good location
      case msg @ MorphLarva(id, unitType) =>
        log.debug("Trying to build {}", msg)
        game.commands add msg
      case msg @ AttackNearest(id) =>
        log.debug("{} attacking", id)
        val unit = game.bwapi.getUnit(id)
        val nearest = game.bwapi.getEnemyUnits.asScala.
          filter(u => u.isVisible).
          min(Ordering.by((u: BUnit) => u.getPosition.getApproxWDistance (unit.getPosition)))
        game.commands add AttackUnit(id, nearest.getID)
    }
}

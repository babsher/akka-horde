package edu.gmu.horde.zerg.env

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

object ZergEnvironment {
  val log = LoggerFactory.getLogger(ZergEnvironment.getClass())

  val mineralTypes = Set(UnitTypes.Resource_Mineral_Field)
}

case class MoveToNearestMineral(id :Int)
case class BuildBuilding(id :Int, buildingType :UnitType, region :Region)
case class Supply(used :Int, total :Int)

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

class ZergEnvironment extends Environment {
  import ZergEnvironment._
  val game = new BWInterface(context.self)

  override def receive =
    super.receive orElse {
      case Run(connect, train) =>
        if(connect) {
          log.debug("Connecting to game")
          game.start()
        }
      case OnFrame =>
        while (!game.newUnits.isEmpty()) {
          val id = game.newUnits.poll()
          log.debug("Found new units: {}", id)
          root ! NewUnit(id, game.units.get(id))
          root ! Supply(game.currentSupply.get(), game.supplyCap.get())
        }
      case NewUnit(id, u) =>
        root ! NewUnit(id, u)
      case MoveToNearestMineral(id: Int) =>
        val unit = game.bwapi.getUnit(id)
        val minerals = game.bwapi.getNeutralUnits.asScala.filter(mineralTypes contains _.getType)
        val nearest = minerals.min(Ordering.by((m: BUnit) => m.getPosition.getApproxWDistance (unit.getPosition)))
        log.debug("Sending {} to nearest mineral {}", id, nearest.getPosition)
        game.commands add RightClickTarget(id, nearest.getID)
        log.debug("Commands {} size {}", game.commands, game.commands.size)
      case BuildBuilding(id :Int, buildingType :UnitType, region :Region) =>
        val unit = game.bwapi.getUnit(id)
        // TODO compute good location
//        game.commands add new UnitCommand(unit, UnitCommandTypes.Build, region.getCenter, buildingType.getID)
    }
}

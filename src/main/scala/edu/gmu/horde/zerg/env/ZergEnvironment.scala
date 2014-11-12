package edu.gmu.horde.zerg.env

import akka.actor._
import edu.gmu.horde.env.BWInterface
import edu.gmu.horde.{SetRoot, Environment}
import edu.gmu.horde.zerg.{NewUnit, OnFrame, UnitCmd}
import jnibwapi.types.UnitCommandType.UnitCommandTypes
import jnibwapi.{Unit => BUnit, UnitCommand, Position}
import jnibwapi.types.UnitType.UnitTypes
import org.slf4j.LoggerFactory
import scala.collection.JavaConverters._

object ZergEnvironment {
  val log = LoggerFactory.getLogger(ZergEnvironment.getClass())

  val mineralTypes = Set(UnitTypes.Resource_Mineral_Field)
}

case class MoveToNearestMineral(id: Int)

class ZergEnvironment extends Environment {
  import ZergEnvironment._
  val game = new BWInterface(context.self)

  override def receive =
    super.receive orElse {
      case UnitCmd(id, cmd) =>
        log.debug("Got command for: " + id)
        game.commands.add(cmd)
      case OnFrame =>
        while (!game.newUnits.isEmpty()) {
          val id = game.newUnits.poll()
          root ! NewUnit(id, game.units.get(id))
        }
      case MoveToNearestMineral(id: Int) =>
        val unit = game.bwapi.getUnit(id)
        val minerals = game.bwapi.getNeutralUnits.asScala.filter(mineralTypes contains _.getType)
        val nearest = minerals.min(Ordering.by((m: BUnit) => m.getPosition.getApproxWDistance (unit.getPosition)))
        game.commands add new UnitCommand(nearest, UnitCommandTypes.Right_Click_Position, nearest)
    }
}

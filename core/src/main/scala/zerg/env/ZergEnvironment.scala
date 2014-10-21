package edu.gmu.horde.zerg.env

import akka.actor.{ActorRef, Actor}
import akka.contrib.pattern.DistributedPubSubMediator
import edu.gmu.horde.{SetRoot, Environment}
import edu.gmu.horde.zerg.{NewUnit, OnFrame, UnitCmd}

class ZergEnvironment extends Environment {

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
    }
}

package edu.gmu.horde.zerg.agents

import akka.actor.{Props, ActorRef, Actor}
import edu.gmu.horde.zerg.agents.Drone.Harvest
import edu.gmu.horde.zerg.env.ZergEnvironment
import edu.gmu.horde.{SetEnvironment, SetManagers}
import edu.gmu.horde.zerg.NewUnit
import jnibwapi.types.UnitType
import jnibwapi.Position

class BaseManagerAgent extends Actor {

  var env: ActorRef = null

  override def receive: Receive = {
    case SetEnvironment(env: ActorRef) =>
      this.env = env
    case NewUnit(id: Int, unit: jnibwapi.Unit) =>
      val drone = context.actorOf(Drone.props(id, unit, env))
      drone ! Harvest
  }
}

package edu.gmu.horde.zerg.agents

import akka.actor.{ActorLogging, Props, ActorRef, Actor}
import edu.gmu.horde.actors.{NewAgent, Train, SetEnvironment}
import edu.gmu.horde.zerg.agents.Drone.Harvest
import edu.gmu.horde.zerg.env.ZergEnvironment
import edu.gmu.horde.zerg.NewUnit
import jnibwapi.types.UnitType
import jnibwapi.Position

object BaseManagerAgent {
  def props(env: ActorRef) = Props(new BaseManagerAgent(env))
}

class BaseManagerAgent(var env: ActorRef) extends Actor with ActorLogging {

  override def receive: Receive = {
    case msg @ NewUnit(id: Int, unit: jnibwapi.Unit) =>
      log.debug("Creating drone with {}", msg)
      val drone = context.actorOf(Drone.props(id, unit, env))
      context.parent ! NewAgent(Drone.id(id), drone, Drone.getClass.getSimpleName)
      drone ! SetEnvironment(env)
    case msg @ Train(train) =>
      context.children.map(child => child ! msg)
    case msg @ NewAgent(_,_,_) =>
      context.parent ! msg
  }
}

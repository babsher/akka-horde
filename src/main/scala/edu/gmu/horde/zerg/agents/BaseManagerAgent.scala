package edu.gmu.horde.zerg.agents

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import edu.gmu.horde.{Train, SetEnvironment, NewAgent, NewUnit}

object BaseManagerAgent {
  def props(env: ActorRef) = Props(new BaseManagerAgent(env))
}

class BaseManagerAgent(var env: ActorRef) extends Actor with ActorLogging {

  override def receive: Receive = {
    case msg @ NewUnit(id: Int, unit: jnibwapi.Unit) =>
      log.debug("Creating drone with {}", msg)
      val drone = context.actorOf(Drone.props(id, unit, env))
      context.parent ! NewAgent(drone, Drone.getClass.getSimpleName)
      drone ! SetEnvironment(env)
    case msg @ Train(train) =>
      context.children.map(child => child ! msg)
    case msg @ NewAgent =>
      context.parent ! msg
  }
}

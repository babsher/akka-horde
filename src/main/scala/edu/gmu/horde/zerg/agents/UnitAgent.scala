package edu.gmu.horde.zerg.agents

import akka.actor.Actor.Receive
import akka.actor.{FSM, _}
import edu.gmu.horde.zerg.{Activate, UnitUpdate}

import scala.concurrent.duration._

abstract class UnitAgent(id :Int, unit :jnibwapi.Unit, env :ActorRef) {
}
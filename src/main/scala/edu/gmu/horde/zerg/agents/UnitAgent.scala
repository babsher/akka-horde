package edu.gmu.horde.zerg.agents

import akka.actor._

abstract class UnitAgent(val id :Int, val unit :jnibwapi.Unit, val env :ActorRef) {

}
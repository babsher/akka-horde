package edu.gmu.horde.zerg.agents

import akka.actor.Actor.Receive
import akka.actor.{FSM, _}
import edu.gmu.horde.AttributeValue
import edu.gmu.horde.zerg.{Activate, UnitUpdate}

abstract class UnitAgent(val id :Int, val unit :jnibwapi.Unit, val env :ActorRef)

}
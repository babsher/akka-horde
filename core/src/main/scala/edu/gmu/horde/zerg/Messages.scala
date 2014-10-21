package edu.gmu.horde.zerg

import akka.actor.ActorRef
import jnibwapi.UnitCommand

sealed trait Messages
case class Subscribe(id: Int, ref: ActorRef)
case class Unsubscribe(ref: ActorRef, id: Option[Int] = None)
case class Publish(u: UnitUpdate)

case class UnitUpdate(id: Int, unit: jnibwapi.Unit)
case class UnitCmd(id: Int, cmd: UnitCommand)
case class OnFrame()
case class NewUnit(id: Int, unit: jnibwapi.Unit)

case object Activate
case object Train
case object Run
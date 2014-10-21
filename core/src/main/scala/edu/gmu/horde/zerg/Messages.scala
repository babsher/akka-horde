package edu.gmu.horde.zerg

import akka.actor.ActorRef
import jnibwapi.UnitCommand

sealed trait Messages
case class Subscribe(id: Integer, ref: ActorRef)
case class Unsubscribe(ref: ActorRef, id: Integer = None)
case class Publish(u: UnitUpdate)

case class UnitUpdate(id: Integer, unit: jnibwapi.Unit)
case class UnitCmd(id: Integer, cmd: UnitCommand)
case class OnFrame()
case class NewUnit(id: Integer, unit: jnibwapi.Unit)

case object Activate
case object Train
case object Run
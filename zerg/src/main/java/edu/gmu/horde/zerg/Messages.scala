package edu.gmu.horde.zerg

import jnibwapi.{UnitCommand, Unit}

sealed trait Messages
case class UnitUpdate(id: Integer, unit: Unit)
case class UnitCmd(id: Integer, cmd: UnitCommand)
case class OnFrame()
case class NewUnit(id: Integer, unit: Unit)

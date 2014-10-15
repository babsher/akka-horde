package edu.gmu.horde.zerg

import jnibwapi.Unit

sealed trait Messages
case class UnitUpdate(unit: Unit)

package edu.gmu.horde.zerg.agents

import akka.actor._
import edu.gmu.horde.{DoubleValue, AttributeValue}
import jnibwapi.{Unit => BUnit}

abstract class UnitAgent(val id :Int, var unit :BUnit) {

}
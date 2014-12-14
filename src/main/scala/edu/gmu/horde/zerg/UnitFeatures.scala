package edu.gmu.horde.zerg

import akka.actor.ActorRef
import edu.gmu.horde.{DoubleValue, AttributeValue}
import jnibwapi.{Unit => BUnit}


trait UnitFeatures {
  val UnitPositionXName = "positionX"
  def UnitPositionX(implicit unit :BUnit) : (String, AttributeValue) = {
    (UnitPositionXName, DoubleValue(unit.getPosition.getBX))
  }

  val UnitPositionYName = "positionY"
  def UnitPositionY(implicit unit :BUnit) : (String, AttributeValue) = {
    (UnitPositionYName, DoubleValue(unit.getPosition.getBY))
  }

  val EnemyDistanceName = "EnemyDistance"
  def EnemyDistance(implicit unit :BUnit,  env :ActorRef) : (String, AttributeValue) = {
    (EnemyDistanceName, DoubleValue(1))
  }
}

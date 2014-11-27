package edu.gmu.horde.zerg

import edu.gmu.horde.{DoubleValue, AttributeValue}
import jnibwapi.{Unit => BUnit}


trait UnitFeatures {
  val UnitPositionXName = "positionX"
  def UnitPositionX(implicit unit : BUnit) : (String, AttributeValue) = {
    ("positionX", DoubleValue(unit.getPosition.getBX))
  }

  val UnitPositionYName = "positionY"
  def UnitPositionY(implicit unit : BUnit) : (String, AttributeValue) = {
    ("positionY", DoubleValue(unit.getPosition.getBY))
  }
}

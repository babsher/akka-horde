package edu.gmu.horde.features

import edu.gmu.horde.DoubleValue
import edu.gmu.horde.storage.{DoubleValue, AttributeValue}

trait SimpleFeatures {
  val TrueFeatureName = "TrueFeature"
  def TrueFeature() : (String, AttributeValue) = {
    (TrueFeatureName, DoubleValue(1))
  }
}

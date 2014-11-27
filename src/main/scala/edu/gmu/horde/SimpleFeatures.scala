package edu.gmu.horde

trait SimpleFeatures {
  val TrueFeatureName = "TrueFeature"
  def TrueFeature() : (String, AttributeValue) = {
    (TrueFeatureName, DoubleValue(1))
  }
}

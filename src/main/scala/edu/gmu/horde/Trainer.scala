package edu.gmu.horde

import java.io.File

import com.google.common.io.Files
import weka.classifiers.Classifier
import weka.classifiers.trees.J48
import weka.core.{Attribute, Instances}
import weka.core.converters.ArffLoader

trait Trainer {
  def train(states :Seq[AgentState], dirName :String) : Map[AgentState, Classifier] = {
    (for(state <- states; c = trainState(state, dirName))
      yield (state -> c)) toMap
  }

  def trainState(state :AgentState, dirName :String) :Classifier = {
    val loader  = new ArffLoader
    val dir = new File(dirName + state.name)
    var instances: Instances = null
    for (file <- dir.listFiles()) {
      loader.setFile(file)
      if (instances == null) {
        instances = loader.getDataSet
      } else {
        for (i <- loader.getDataSet.enumerateInstances()) {
          instances.add(i)
        }
      }
    }
    for(attr :Attribute <- instances.enumerateAttributes()) {
      if(attr.name() eq "state") {
        instances.setClass(attr)
      }
    }
    val c = new J48
    c.buildClassifier(instances)
    weka.core.SerializationHelper.write(new File(dir, "j48.model").getCanonicalPath, c)
    return c
  }
}

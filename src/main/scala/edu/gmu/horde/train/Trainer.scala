package edu.gmu.horde.train

import java.io.File
import java.util.Enumeration
import edu.gmu.horde.actors.AgentState
import edu.gmu.horde.storage.AttributeIO
import weka.classifiers.Classifier
import weka.classifiers.trees.J48
import weka.core.converters.ArffLoader
import weka.core.{Attribute, Instance, Instances}


/**
 * Writes models to directory
 */
trait Trainer extends AttributeIO {
  def train(states :Seq[AgentState], dirName :String, agentName :String) : Map[AgentState, Classifier] = {
    val loader  = new ArffLoader

    def trainState(state :AgentState) :Classifier = {
      // TODO refactor to AttributeIO
      val dir = new File(dirName + state.name)
      var instances: Instances = null
      for (file <- dir.listFiles()) {
        loader.setFile(file)
        if (instances == null) {
          instances = loader.getDataSet
        } else {
          val enum :Enumeration[_]= loader.getDataSet.enumerateInstances
          while(enum.hasMoreElements) {
            instances.add(enum.nextElement().asInstanceOf[Instance])
          }
        }
      }
      val enum = instances.enumerateAttributes
      while(enum.hasMoreElements) {
        val attr = enum.nextElement().asInstanceOf[Attribute]
        if(attr.name() eq "state") {
          instances.setClass(attr)
        }
      }
      val c = new J48
      c.buildClassifier(instances)
      weka.core.SerializationHelper.write(new File(dir, agentName + ".model").getCanonicalPath, c)
      return c
    }

    (for(state <- states; c = trainState(state))
      yield (state -> c)) toMap
  }
}

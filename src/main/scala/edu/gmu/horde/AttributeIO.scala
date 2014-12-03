package edu.gmu.horde

import java.io.File

import com.google.common.io.Files
import org.slf4j.{LoggerFactory, Logger}
import weka.core.{FastVector, Attribute, Instance}

trait AttributeIO {
  private val log :Logger = LoggerFactory.getLogger(this.getClass)

  def getAttributes(attributes :Seq[Attribute]): FastVector = {
    val vector = new FastVector(attributes.size)
    for(a <- attributes) {
      vector.addElement(a)
    }
    return vector
  }

  def instanceStorageFile(directory :String, agentType :String, state :AgentState) :File = {
    val f = new File(directory + agentType + "/" + state.name + ".arff" )
    Files.createParentDirs(f)
    return f
  }

  def instance(attributes :Seq[Attribute], i :Map[String, AttributeValue]) :Instance = {
    val data = Array.fill(attributes.size){0.0}
    for (attr <- attributes) {
      if(i contains attr.name()) {
        i(attr.name) match {
          case StringValue(v: String) => data(attr.index) = attr.addStringValue(v).asInstanceOf[Double]
          case DoubleValue(v: Double) => data(attr.index) = v
        }
      } else {
        log.debug("did not mat attr {}", attr)
      }
    }
    new Instance(1.0, data)
  }
}

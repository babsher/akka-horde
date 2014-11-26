package edu.gmu.horde

import java.io.File

import akka.actor.{Actor, Props}
import org.slf4j.LoggerFactory
import weka.core.converters.{ArffSaver, Saver}
import weka.core.{Attribute, FastVector, Instance, Instances}

import scala.collection.immutable.List

sealed trait AttributeValue
case class StringValue(v: String) extends AttributeValue
case class DoubleValue(v: Double) extends AttributeValue

case class Write(i: Map[String, AttributeValue])
case object Close

object AttributeStorage {
  val log = LoggerFactory.getLogger(AttributeStorage.getClass)
  def props(directory :String, agentType :String, stateName :String, attributes :Seq[Attribute]): Props =
    Props(new AttributeStorage(directory, agentType, stateName, attributes))
}

class AttributeStorage(val directory :String, val agentType :String, val stateName :String, val attributes :Seq[Attribute]) extends Actor {
  import edu.gmu.horde.AttributeStorage.log

  var instances: Seq[Map[String, AttributeValue]] = List()
  val attrInfo = getAttributes()
  val inst = new Instances(agentType, attrInfo, 0)
  val saver = new ArffSaver()
  saver.setInstances(inst)
  saver.setRetrieval(Saver.INCREMENTAL)
  val f = new File(directory + agentType + "/" + stateName + "/" + context.parent.path + ".arff" )
  log.debug("Writing instances to {}", f.getAbsoluteFile)
  saver.setFile(f)
  saver.setDestination(f)

  override def receive: Receive = {
    case Write(i: Map[String, AttributeValue]) =>
      log.debug("Matches {}", i.size == attributes.size)
      log.debug("Adding instance: {}", i)
      instances = instances :+ i
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
      saver.writeIncremental(new Instance(1.0, data))
    case Close =>
      log.debug("closing - {}", instances)
      saver.writeIncremental(null)
    case a =>
      log.debug("Unmatched {}", a)
  }

  def getAttributes(): FastVector = {
    val f = new FastVector(attributes.size)
    for(a <- attributes) {
      f.addElement(a)
    }
    return f
  }
}

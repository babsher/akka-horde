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
  def props(directory :String, agentType :String, stateName :AgentState, attributes :Seq[Attribute]): Props =
    Props(new AttributeStorage(directory, agentType, stateName, attributes))
}

class AttributeStorage(val directory :String, val agentType :String, val state :AgentState, val attributes :Seq[Attribute]) extends Actor
  with AttributeIO {
  implicit val log = edu.gmu.horde.AttributeStorage.log

  var instances: Seq[Map[String, AttributeValue]] = List()
  val attrInfo = getAttributes(attributes)
  val inst = new Instances(agentType, attrInfo, 0)
  val saver = new ArffSaver()
  saver.setInstances(inst)
  saver.setRetrieval(Saver.INCREMENTAL)
  val f = instanceStorageFile(directory, agentType, state)
  log.debug("Writing instances to {}", f.getAbsoluteFile)
  saver.setFile(f)
  saver.setDestination(f)

  override def receive: Receive = {
    case Write(i: Map[String, AttributeValue]) =>
      log.debug("Matches {}", i.size == attributes.size)
      log.debug("Adding instance: {}", i)
      instances = instances :+ i
      saver.writeIncremental(instance(attributes, i))
    case Close =>
      log.debug("closing - {}", instances)
      saver.writeIncremental(null)
    case a =>
      log.debug("Unmatched {}", a)
  }
}

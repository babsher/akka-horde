package edu.gmu.horde

import java.io.{FileWriter, BufferedWriter, File}
import java.util.ArrayList
import jnibwapi.Unit
import weka.core.converters.{Saver, ArffSaver}
import weka.core.{Instances, FastVector, Instance, Attribute}
import org.slf4j.LoggerFactory

import akka.actor.{AbstractLoggingActor, Props, ActorRef, Actor}

import scala.collection.immutable.List
import scala.collection.mutable.MutableList

sealed trait AttributeValue
case class StringValue(v: String) extends AttributeValue
case class DoubleValue(v: Double) extends AttributeValue

case class Write(i: Map[String, AttributeValue])
case object Close

object AttributeStorage {
  val log = LoggerFactory.getLogger(AttributeStorage.getClass)
  def props(id: Int, attributes: Seq[Attribute]): Props = Props(new AttributeStorage(id: Int, attributes: Seq[Attribute]))
}

class AttributeStorage(id: Int, attributes: Seq[Attribute]) extends Actor {
  import AttributeStorage.log

  var instances: Seq[Map[String, AttributeValue]] = List()

  override def receive: Receive = {
    case Write(i: Map[String, AttributeValue]) =>
      log.debug("Matches {}", i.size == attributes.size)
      log.debug("Adding instance: {}", i)
      instances = instances :+ i
    case Close =>
      log.debug("closing - {}", instances)
      val name = "Drone"
      val attrInfo = getAttributes()
      val inst = new Instances(name + id, attrInfo, 0)
      val saver = new ArffSaver()
      saver.setInstances(inst)
      saver.setRetrieval(Saver.INCREMENTAL)
      val f = new File("./data/test.arff")
      log.debug("Writing instances to {}", f.getAbsoluteFile)
      saver.setFile(f)
      saver.setDestination(new File("./data/test.arff"))

      for(x <- instances) {
        val i = Array.fill(attributes.size){0.0}
        for (attr <- attributes) {
          if(x contains attr.name()) {
            x(attr.name) match {
              case StringValue(v: String) => i(attr.index) = attr.addStringValue(v).asInstanceOf[Double]
              case DoubleValue(v: Double) => i(attr.index) = v
            }
          } else {
            log.debug("did not mat attr {}", attr)
          }
        }
        saver.writeIncremental(new Instance(1.0, i))
      }
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

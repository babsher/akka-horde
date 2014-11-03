package edu.gmu.horde

import java.io.{FileWriter, BufferedWriter, File}
import java.util.ArrayList
import jnibwapi.Unit
import weka.core.converters.ArffSaver
import weka.core.{Instances, FastVector, Instance, Attribute}
import org.slf4j.LoggerFactory

import akka.actor.{AbstractLoggingActor, Props, ActorRef, Actor}

import scala.collection.immutable.List

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
      log.debug("Adding instance: {}", i)
      instances = instances :+ i
    case Close =>
      log.debug("closing - {}", instances)
      val name = "Drone"
      val attrInfo = new FastVector(attributes.size)
      val inst = new Instances(name + id, attrInfo, 10)
      for(x <- instances) {
        val i = new Instance(attributes.size)
        log.debug("new instance {}", i)
        for (attr <- attributes) {
          log.debug("checking {}", attr)
          if(x contains attr.name()) {
            log.debug("setting attr {}", attr)
            x(attr.name) match {
              case StringValue(v: String) => i.setValue(attr, v)
              case DoubleValue(v: Double) => i.setValue(attr, v)
            }
          } else {
            log.debug("did not mat attr {}", attr)
          }
        }
        inst.add(i)
      }
      val saver = new ArffSaver()
      saver.setInstances(inst)
      val f = new File("./data/test.arff")
      log.debug("Writing instances to {}", f.getAbsoluteFile)
      saver.setFile(f)
      saver.setDestination(new File("./data/test.arff"))
      saver.writeBatch()
    case a =>
      log.debug("Unmatched {}", a)
  }
}

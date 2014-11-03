package edu.gmu.horde

import java.io.{FileWriter, BufferedWriter, File}
import java.util.ArrayList
import jnibwapi.Unit
import weka.core.converters.ArffSaver
import weka.core.{Instances, FastVector, Instance}

import akka.actor.{Props, ActorRef, Actor}
import akka.actor.Actor.Receive
import com.google.common.io.Files

import scala.collection.immutable.List

case class Write(i: Instance)
case object Close

object AttributeStorage {
  def props(id: Int, attInfo: FastVector): Props = Props(new AttributeStorage(id: Int, attInfo: FastVector))
}

class AttributeStorage(id: Int, attInfo: FastVector) extends Actor {

  val vect: Seq[Instance] = List()
  var out: BufferedWriter = _

  override def receive: Receive = {
    case Write(i: Instance) =>
      vect :+ i
    case Close =>
      val name = "Drone"
      val inst = new Instances(name + id, attInfo, 10)
      vect.foreach(inst.add)
      val saver = new ArffSaver()
      saver.setInstances(inst)
      saver.setFile(new File("./data/test.arff"))
      saver.setDestination(new File("./data/test.arff"))
      saver.writeBatch()
  }
}

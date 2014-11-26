package edu.gmu.horde

import java.text.SimpleDateFormat
import java.util.Date

import akka.actor.{ActorRef, Actor}
import akka.actor.Actor.Receive
import edu.gmu.horde.AttributeStore.{AStore, NewAttributeStore}
import weka.core.Attribute

object AttributeStore {
  case class NewAttributeStore(agentName :String, stateName :String, attributes :Seq[Attribute])
  case class AStore(ref :ActorRef)
}

class AttributeStore extends Actor {

  val session = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date())
  var stores :Map[String, ActorRef] = Map()

  override def receive: Receive = {
    case NewAttributeStore(agentName, stateName, attributes) =>
      val key = agentName + stateName
      val store = stores.getOrElse(key, context.actorOf(AttributeStorage.props(agentName, stateName, attributes)))
      stores += (key -> store)
      sender ! AStore(store)
  }
}

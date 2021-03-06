package edu.gmu.horde.storage

import java.text.SimpleDateFormat
import java.util.Date

import akka.actor.{Actor, ActorRef, Props}
import com.typesafe.config.ConfigFactory
import edu.gmu.horde.actors.AgentState
import edu.gmu.horde.storage.AttributeStore.{AStore, NewAttributeStore}
import weka.core.Attribute

object AttributeStore {
  def props(scenario :String): Props = {
    Props(new AttributeStore(scenario))
  }

  case class NewAttributeStore(agentName :String, stateName :AgentState, attributes :Seq[Attribute])
  case class AStore(ref :ActorRef)
}

class AttributeStore(val name :String) extends Actor {

  val session = name + new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date())
  var stores :Map[String, ActorRef] = Map()
  val directory = ConfigFactory.load().getString("horde.instanceDir") + session + "/"

  override def receive: Receive = {
    case NewAttributeStore(agentName, state, attributes) =>
      val key = agentName + state.name
      val store = stores.getOrElse(key, context.actorOf(AttributeStorage.props(directory, agentName, state, attributes)))
      stores += (key -> store)
      sender ! AStore(store)
  }
}

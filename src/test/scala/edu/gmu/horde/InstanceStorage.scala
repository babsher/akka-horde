package edu.gmu.horde

import akka.actor.Actor
import akka.actor.Actor.Receive
import org.slf4j.LoggerFactory

case object InstanceStorage {
  val log = LoggerFactory.getLogger(InstanceStorage.getClass)
}

case class Instance(fsm: Any, state: Any, features: Seq[Any])
case class NewInstance(i: Instance)

class InstanceStorage extends Actor {
  import InstanceStorage.log

  override def receive: Receive = {
    case NewInstance(i: Instance) =>
      log.debug("New Instance: {}", i)
  }
}

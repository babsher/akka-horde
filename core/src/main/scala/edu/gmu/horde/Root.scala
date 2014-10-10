package edu.gmu.horde

import akka.actor._
import akka.actor.Actor.Receive

class Root extends Actor{
  var env: ActorRef = null

  override def receive = {
    case SetEnvironment(environment) => this.env = environment
  }
}

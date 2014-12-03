package edu.gmu.horde

import akka.actor.Actor
import akka.actor.Actor.Receive

/**
 * Sends messages to agents for training
 */
class AgentTrainer extends Actor {
  override def receive: Receive = {
    case Train(train) =>
      println("Training")
      // become trainer?
  }
}

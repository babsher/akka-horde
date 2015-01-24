package edu.gmu.horde.train

import akka.actor.Actor
import edu.gmu.horde.actors.Train

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

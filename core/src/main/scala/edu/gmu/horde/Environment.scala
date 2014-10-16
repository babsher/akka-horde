package edu.gmu.horde

import akka.actor._
import akka.actor.Actor.Receive
import akka.contrib.pattern.DistributedPubSubExtension
import akka.contrib.pattern.DistributedPubSubMediator
import akka.contrib.pattern.DistributedPubSubMediator.{Subscribe, SubscribeAck}
import org.slf4j.LoggerFactory
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import akka.cluster.MemberStatus

object Environment {
  val log = LoggerFactory.getLogger(Environment.getClass())
}

abstract class Environment extends Actor with ActorLogging {
  import Environment.log
  import DistributedPubSubMediator.{ Subscribe, SubscribeAck }

  val mediator = DistributedPubSubExtension(context.system).mediator
  // subscribe to the topic named "content"
  mediator ! Subscribe("content", self)

}

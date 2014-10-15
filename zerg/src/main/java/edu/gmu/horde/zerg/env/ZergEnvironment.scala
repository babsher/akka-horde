package edu.gmu.horde.zerg.env

import akka.actor.Actor
import akka.contrib.pattern.DistributedPubSubMediator
import edu.gmu.horde.Environment

class ZergEnvironment extends Environment {
  import DistributedPubSubMediator.{ Subscribe, SubscribeAck, Publish }

  mediator ! Publish("/units/my/id/", )
}

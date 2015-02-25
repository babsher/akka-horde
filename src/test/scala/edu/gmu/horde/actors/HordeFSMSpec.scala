package edu.gmu.horde.actors

import akka.actor.ActorSystem
import akka.testkit.{TestActorRef, ImplicitSender, TestKit}
import edu.gmu.horde.zerg.agents.ProductionAgent
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class HordeFSMSpec extends TestKit(ActorSystem("TestKitUsageSpec"))
    with ImplicitSender
    with WordSpecLike
    with Matchers
    with BeforeAndAfterAll {

}

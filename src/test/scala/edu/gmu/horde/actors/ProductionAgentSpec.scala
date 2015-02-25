package edu.gmu.horde.actors

import akka.actor.{Actor, ActorSystem}
import akka.testkit.{TestActorRef, ImplicitSender, TestKit}
import edu.gmu.horde.{NewUnit, NewAgent}
import edu.gmu.horde.zerg.agents.{Drone, ProductionAgent}
import edu.gmu.horde.zerg.env.ZergEnvironment
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class ProductionAgentSpec extends TestKit(ActorSystem("TestKitUsageSpec"))
  with ImplicitSender
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll
  with MockitoSugar {

  val envRef = TestActorRef[ZergEnvironment]
  val droneRef = TestActorRef(new Actor {
    def receive = {
      case "hello" => throw new IllegalArgumentException("boom")
    }
  })
  val actorRef = TestActorRef(new ProductionAgent(envRef))

  "Production Agent " must {
    "will forward to parent " in {
      actorRef ! NewAgent(droneRef, "Drone")
    }
    
    "will forward new drone " in {
      val unit = mock[jnibwapi.Unit]
      when(unit.getID).thenReturn(1)
      actorRef ! NewUnit(1, unit)
    }
  }
}

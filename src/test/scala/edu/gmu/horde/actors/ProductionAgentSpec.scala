package edu.gmu.horde.actors

import akka.actor.{Actor, ActorSystem}
import akka.testkit.{TestActorRef, ImplicitSender, TestKit}
import edu.gmu.horde.{NewUnit, NewAgent}
import edu.gmu.horde.zerg.agents.{Drone, ProductionAgent}
import edu.gmu.horde.zerg.env.ZergEnvironment
import jnibwapi.types.UnitType
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
  val actorRef = TestActorRef(new ProductionAgent(envRef, self))

  "Production Agent " must {
    "will send root new Agent" in {
      val msg = NewAgent(droneRef, self, "Drone", Option(1))
      actorRef ! msg
    }

    "will forward new drone " in {
      val player = mock[jnibwapi.Player]
      when(player.isSelf).thenReturn(true)
      
      val unit = mock[jnibwapi.Unit]
      when(unit.getID).thenReturn(1)
      when(unit.getPlayer).thenReturn(player)
      when(unit.getType).thenReturn(UnitType.UnitTypes.Zerg_Drone)
      actorRef ! NewUnit(1, unit)
    }
  }
}

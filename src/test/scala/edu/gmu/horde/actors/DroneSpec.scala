package edu.gmu.horde.actors

import akka.actor.{Actor, ActorSystem}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import edu.gmu.horde._
import edu.gmu.horde.storage.DoubleValue
import edu.gmu.horde.zerg.agents.Drone
import jnibwapi.types.UnitType
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class DroneSpec extends TestKit(ActorSystem("TestKitUsageSpec"))
with WordSpecLike
with ImplicitSender
with BeforeAndAfterAll
with Matchers
with MockitoSugar
with Messages {

  val envRef = TestActorRef(new Actor {
    def receive = {
      case default =>
    }
  })

  val player = mock[jnibwapi.Player]
  when(player.isSelf).thenReturn(true)

  val unit = mock[jnibwapi.Unit]
  val unitId = 1
  when(unit.getID).thenReturn(unitId)
  when(unit.getPlayer).thenReturn(player)
  when(unit.getType).thenReturn(UnitType.UnitTypes.Zerg_Drone)

  "Drone Agent" must {
    val droneRef = TestActorRef(new Drone(unitId, unit, envRef))

    "return agent detail when requested" in {
      droneRef ! RequestAgentDetail
      val possibleStates =
        AgentPossibleStates("Idle",     true, false) ::
        AgentPossibleStates("Start",    true, true) ::
        AgentPossibleStates("Retreat",  true, false) ::
        AgentPossibleStates("Attacking", true, false) ::
        AgentPossibleStates("Harvest",  true, false) ::
        AgentPossibleStates("Retreat",  false, false) :: Nil
      expectMsg(AgentDetail(droneRef, "Drone$", State("Start"), possibleStates, Map("TrueFeature" -> DoubleValue(1))))
    }
  }

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }
}

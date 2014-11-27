package edu.gmu.horde

import com.typesafe.config.ConfigFactory
import edu.gmu.horde.zerg.env.ZergEnvironment
import org.junit._
import akka.testkit._
import akka.actor.{Props, FSM, ActorSystem}
import scala.concurrent.duration._

class HordeFSMTest {

  @Test
  def test = {
    implicit val system = ActorSystem("HordeDebug", ConfigFactory.load())
    val fsm = TestFSMRef(new HordeFSM)
    val mustBeTypedProperly: TestActorRef[HordeFSM] = fsm
    assert(fsm.stateName == Idle)
    assert(fsm.stateData == Uninitialized)

    val msg = Scenario("A Name")
    fsm ! msg
    Thread.sleep(1000)
    assert(fsm.stateName == Running)
    println(fsm.stateData)
    assert(fsm.stateData == EnvironmentData(system.actorOf(Props[ZergEnvironment])))
    assert(fsm.stateName == Running)
  }
}

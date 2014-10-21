package edu.gmu.horde

import akka.actor.Actor.Receive
import akka.actor._
import akka.testkit.{TestActorRef, TestFSMRef}
import com.typesafe.config.ConfigFactory
import edu.gmu.horde.zerg.{Publish, Subscribe, UnitUpdate}
import org.junit.Test

class EnvironmentTest {

  class TestActor extends Actor {
    override def receive: Receive = {
      case UnitUpdate(id, u) =>
        println(id)
        println(u)
    }
  }

  @Test
  def testSubscribe = {
    implicit val system = ActorSystem("HordeDebug", ConfigFactory.load())
    val env = TestFSMRef(new Environment)
    val mustBeTypedProperly: TestActorRef[Environment] = env
    val test = TestActorRef(new TestActor)
    env ! Subscribe(1, test)
    jnibwapi.Unit u =
    env ! new Publish(UnitUpdate(1, u))
  }

}

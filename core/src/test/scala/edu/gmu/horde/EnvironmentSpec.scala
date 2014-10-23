package edu.gmu.horde

import akka.actor.Actor.Receive
import akka.actor._
import akka.testkit._
import com.typesafe.config.ConfigFactory
import edu.gmu.horde.zerg.{Publish, Subscribe, UnitUpdate}
import org.junit.Test

import collection.mutable.Stack
import org.scalatest._

class EnvironmentSpec extends TestKit(_system) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  def this() = this(ActorSystem("EnvironmentSpec"))

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "Environment actor" must {

    "Send messages to subscribed actors" in {
      val echo = system.actorOf(TestActors.echoActorProps)
      echo ! "hello world"
      expectMsg("hello world")
      val env = TestActorRef(new Environment)
      val mustBeTypedProperly: TestActorRef[Environment] = env
      val test = TestActorRef(new TestActor)
      env ! Subscribe(1, test)
      val u: jnibwapi.Unit = Mockito.mock(jnibwapi.Unit)
      env ! new Publish(UnitUpdate(1, u))
    }
  }
}


//class EnvironmentTest {
//
//  class TestActor extends Actor {
//    override def receive: Receive = {
//      case UnitUpdate(id, u) =>
//        println(id)
//        println(u)
//    }
//  }
//
//  @Test
//  def testSubscribe = {
//    implicit val system = ActorSystem("HordeDebug", ConfigFactory.load())
//    val env = TestActorRef(new Environment)
//    val mustBeTypedProperly: TestActorRef[Environment] = env
//    val test = TestActorRef(new TestActor)
//    env ! Subscribe(1, test)
//    val u: jnibwapi.Unit = Mockito.mock(jnibwapi.Unit)
//    env ! new Publish(UnitUpdate(1, u))
//  }
//
//}

package edu.gmu.horde.actors

import akka.actor._
import akka.testkit.{ImplicitSender, TestKit}
import edu.gmu.horde.{Publish, Subscribe, UnitUpdate}
import org.scalatest._

import scala.concurrent.duration._

class EnvironmentSpec extends TestKit(ActorSystem("TestKitUsageSpec"))
  with ImplicitSender
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll {

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "Environment actor" must {

    "Send messages to subscribed actors" in {
      val env = system.actorOf(Props[Environment])
      val sub = Subscribe(1, testActor)
      env ! sub
      within(5 seconds) {
        val u: jnibwapi.Unit = null
        val msg = UnitUpdate(1, u)
        val pub = new Publish(msg)
        env ! pub
        expectMsg(msg)
      }
    }
  }
}
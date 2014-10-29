package edu.gmu.horde

import akka.actor._
import akka.testkit.{ TestActors, DefaultTimeout, ImplicitSender, TestKit }
import com.typesafe.config.ConfigFactory
import edu.gmu.horde.zerg.{Publish, Subscribe, UnitUpdate}
import org.junit.Test
import scala.concurrent.duration._

import org.scalatest._

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
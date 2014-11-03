package edu.gmu.horde

import akka.actor.{Props, ActorSystem, Actor}
import akka.actor.Actor.Receive
import akka.testkit.{ImplicitSender, TestKit}
import edu.gmu.horde.zerg.agents.Drone
import edu.gmu.horde.zerg.{Publish, UnitUpdate, Subscribe}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import weka.core.{FastVector, Attribute, Instance}
import scala.concurrent.duration._
import org.slf4j.LoggerFactory


class AttributeStorageSpec extends TestKit(ActorSystem("TestKitUsageSpec"))
  with ImplicitSender
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll {

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "Attribute actor" must {

    "Write instance to file" in {
      val a1 = new Attribute("firstNumeric")
      val a2 = new Attribute("secondNumeric")
      val a = new FastVector(2)
      a.addElement(a1)
      a.addElement(a2)

      val attr = system.actorOf(AttributeStorage.props(1, a))
      println(a1.index())
      val i = new Instance(2)
      i.setValue(a1, 1)
      i.setValue(a2, 2)
      attr ! Write(i)
      within(5 seconds) {
        attr ! Close
      }
    }
  }
}

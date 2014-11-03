package edu.gmu.horde

import javax.management.AttributeValueExp

import akka.actor.{Props, ActorSystem, Actor}
import akka.actor.Actor.Receive
import akka.testkit.{TestActorRef, ImplicitSender, TestKit}
import edu.gmu.horde.zerg.agents.Drone
import edu.gmu.horde.zerg.{Publish, UnitUpdate, Subscribe}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import weka.core.{FastVector, Attribute, Instance}
import scala.concurrent.duration._
import org.slf4j.LoggerFactory


class AttributeStorageSpec extends WordSpecLike {
  implicit val system = ActorSystem("MySpec")

  "Attribute actor" must {

    "Write instance to file" in {
      val a = List(new Attribute("firstNumeric", 0), new Attribute("secondNumeric", 1))
      val attr = TestActorRef(new AttributeStorage(1, a))
      println(a)
      val i = Map(a(0).name() -> DoubleValue(1), a(1).name() -> DoubleValue(1))
      attr ! Write(i)
      Thread.sleep(1000)
      attr ! Close
    }
  }
}

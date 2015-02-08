package edu.gmu.horde

import javax.management.AttributeValueExp

import akka.actor.{Props, ActorSystem, Actor}
import akka.actor.Actor.Receive
import akka.testkit.{TestActorRef, ImplicitSender, TestKit}
import edu.gmu.horde.actors.AgentState
import edu.gmu.horde.storage._
import edu.gmu.horde.zerg.agents.Drone
import edu.gmu.horde.zerg.UnitUpdate
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import weka.core.{FastVector, Attribute, Instance}
import scala.concurrent.duration._
import org.slf4j.LoggerFactory


class AttributeStorageSpec extends WordSpecLike {
  implicit val system = ActorSystem("MySpec")
  val attributes = List(new Attribute("firstNumeric", 0), new Attribute("secondNumeric", 1))

  class TestState extends AgentState {
    override def name: String = "TestState"
    def attributes(): Seq[Attribute] = attributes
    def features(agent: AnyRef): Map[String, AttributeValue] = ???
  }

  "Attribute actor" must {

    "Write instance to file" in {
      val directory = "testdata"
      val agentType = "AgentClass"
      val stateName = "Start"
      val state = new TestState()
      print(attributes.size)
      val attr = TestActorRef(new AttributeStorage(directory, agentType, state, attributes))
      println(attributes)
      val i = Map(attributes(0).name() -> DoubleValue(1), attributes(1).name() -> DoubleValue(1))
      attr ! Write(i)
      Thread.sleep(1000)
      attr ! Close
    }
  }
}

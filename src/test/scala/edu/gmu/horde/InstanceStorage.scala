package edu.gmu.horde

import akka.actor.ActorSystem
import akka.testkit.TestActorRef
import edu.gmu.horde.actors.AgentState
import edu.gmu.horde.storage._
import org.scalatest.WordSpecLike
import weka.core.Attribute


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

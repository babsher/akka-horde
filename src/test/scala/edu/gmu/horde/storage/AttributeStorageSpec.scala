package edu.gmu.horde.storage

import java.io.File

import akka.actor.ActorSystem
import akka.testkit.TestActorRef
import edu.gmu.horde.actors.AgentState
import org.scalatest.WordSpecLike
import weka.core.Attribute


class AttributeStorageSpec extends WordSpecLike {
  implicit val system = ActorSystem("MySpec")
  val attributes = List(new Attribute("firstNumeric", 0), new Attribute("secondNumeric", 1))

  val file = new File("testdataAgentClass")
  if(file.exists()) {
    file.delete()
  }

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
      // TODO test that this works
      Thread.sleep(1000)
      attr ! Close

      val file = new File("testdataAgentClass")
      if(file.exists()) {
        val lines = scala.io.Source.fromFile("testdataAgentClass/TestState.arff").mkString
        lines.map(println _)
      } else {
        fail("Storage directory not created")
      }
    }
  }
}

package edu.gmu.horde.train

import akka.actor.ActorSystem
import edu.gmu.horde.actors.AgentState
import edu.gmu.horde.storage.AttributeValue
import org.scalatest.WordSpecLike
import weka.core.Attribute


class TrainerSpec extends WordSpecLike with Trainer {
  val attributes = List(new Attribute("firstNumeric", 0), new Attribute("secondNumeric", 1))

  class TrainTestState extends AgentState {
    override def name: String = "TrainTestState"
    def attributes(): Seq[Attribute] = attributes
    def features(agent: AnyRef): Map[String, AttributeValue] = ???
  }
  
  "Trainer " must {
    
    "read instances and create model " in {
      
      
    }
  }
}

package edu.gmu.horde

import akka.pattern.ask
import akka.actor.{ActorContext, ActorRef, LoggingFSM, FSM}
import com.typesafe.config.ConfigFactory
import edu.gmu.horde.AttributeStore.NewAttributeStore
import edu.gmu.horde.zerg.agents.Drone
import weka.classifiers.Classifier
import weka.core.Attribute

import scala.concurrent.Await
import scala.concurrent.duration._

/**
 * A simple extension of Akka's <code>FSM</code>.  In this class, state transitions can be defined with the
 * <code>from</code> state and <code>to</code> state constructs.
 * <p>
 * Note that <code>Function1</code> can be replaced with <code>PartialFunction</code>.
 *
 * @param S denotes the user specified state, e.g. Initialized, Started
 * @param D denotes the user specified data model
 */
trait HordeAgentFSM[S <: AgentState, D] {
  this: FSM[S, D] =>
  var attributeStore : ActorRef = _
  var store :Map[S, ActorRef] = Map()
  var training = false
  val models :Map[S, Classifier] = loadModels(ConfigFactory.load().getString("horde.modelDir"))

  def loadModels(dir :String): Map[S, Classifier] = {
    (for(state <- this.states; c = weka.core.SerializationHelper.read(dir + state.name))
      yield (state -> c)) toMap
  }
  def states : Seq[S]

  case class To(state :S, f: (Event) => Unit)
  case class Action()

  def from(fromState: S)(toStates: Seq[To]) {
    when(fromState) {
      case Event(Action, _) =>
        val nextState = getNextState(fromState, fromState.features(this), toStates)
        if(nextState == fromState) {
          stay
        } else {
          goto(nextState)
        }
      case Event(SetAttributeStore(storeActor: ActorRef), _) =>
        attributeStore = storeActor
        stay
      case e@Event(nextState: S, _) =>
        val option = toStates.find((to: To) => to.state == nextState)
        option match {
          case Some(toState) =>
            if(training) {
              store(fromState, toState.state)
            }
            toState.f(e)
            goto(nextState)
          case None =>
            stay
        }
      case Event(msg @ _, _) =>
        val nextState = getNextState(fromState, fromState.features(this, msg), toStates)
        if(nextState == fromState) {
          stay
        } else {
          goto(nextState)
        }

      case a@_ =>
        log.debug("Unknown state transition {}", a)
        stay
    }

    def getNextState(currentState :S, features :Map[String, AttributeValue], toStates :Seq[To]) : S = {
      val state = classifiy(features)
      val s = toStates.filter(x => x.state.name == state)
      if(s.isEmpty) {
        log.debug("No state matching {}", state)
        return currentState
      } else {
        return s(0).state
      }
    }

    def classifiy(features :Map[String, AttributeValue]) : String = {
      ""
    }
  }

  def store(fromState :S, toState :S) : Unit = {
    val storeActor = if(!store.contains(fromState)) {
      val attr = Seq(new Attribute("nextState")) ++ fromState.attributes
      val future = ask(attributeStore, NewAttributeStore(getClass.getCanonicalName, fromState.name, attr))(5 second)
      Await.result(future, 5 seconds).asInstanceOf[ActorRef]
    } else {
      store(fromState)
    }
    val instance = fromState.features(this) + ("nextState" -> StringValue(toState.name))
    storeActor ! Write(instance)
  }
}

trait AgentState {
  def attributes() : Seq[Attribute] = ???
  def features(agent :AnyRef) : Map[String, AttributeValue] = ???
  def features(agent :AnyRef, msg :Any) :Map[String, AttributeValue] = {
    features(agent) + ("msg" -> StringValue(msg.toString))
  }
  def name() : String = ???
}

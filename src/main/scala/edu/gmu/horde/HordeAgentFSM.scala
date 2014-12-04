package edu.gmu.horde

import akka.pattern.ask
import akka.actor.{ActorContext, ActorRef, LoggingFSM, FSM}
import com.typesafe.config.ConfigFactory
import edu.gmu.horde.AttributeStore.NewAttributeStore
import edu.gmu.horde.zerg.agents.Drone
import weka.classifiers.Classifier
import weka.classifiers.trees.J48
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
trait HordeAgentFSM[S <: AgentState, D] extends AttributeIO {
  this: FSM[S, D] =>
  var attributeStore : ActorRef = _
  var store :Map[S, ActorRef] = Map()
  var training = false
  val targetAttribute = "nextState"
  val target = new Attribute(targetAttribute)
  var env :ActorRef
  for(state <- states) {
    target.addStringValue(state.name)
  }
  lazy val models :Map[S, Classifier] = loadModels(ConfigFactory.load().getString("horde.modelDir"))

  def loadModels(dir :String) = {
    (for(state <- this.states; c :Classifier = weka.core.SerializationHelper.read(dir + state.name).asInstanceOf[Classifier])
      yield (state -> c)) toMap
  }
  def states : Seq[S]

  case class To(state :S, f: (Event) => Unit)
  case class Action()

  def from(fromState: S)(toStates: Seq[To]) {
    when(fromState) {
      case Event(SetEnvironment(ref), _) =>
        env = ref
        stay
      case Event(Train(train), _) =>
        training = train
        if(training) {
          cancelTimer("action")
        } else {
          setTimer("action", Action, 500 millis, true)
        }
        context.children.map(child => child ! Train(training))
        stay
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

    def getNextState(currState :S, features :Map[String, AttributeValue], toStates :Seq[To]) : S = {
      val state = classify(currState, features)
      val s = toStates.filter(x => x.state.name equals state)
      if(s.isEmpty) {
        log.debug("No state matching {} in toStates: {}", state, toStates)
        return currState
      } else {
        return s(0).state
      }
    }

    def classify(currState :S, features :Map[String, AttributeValue]) : String = {
      val i = instance(currState.attributes, features)
      val next = models(currState).classifyInstance(i)
      // TODO models(currState).distributionForInstance(i)
      target.value(next.toInt)
    }
  }

  def store(fromState :S, toState :S) : Unit = {
    val storeActor = if(!store.contains(fromState)) {
      val attr = Seq(target) ++ fromState.attributes
      val future = ask(attributeStore, NewAttributeStore(agentName, fromState, attr))(5 second)
      Await.result(future, 5 seconds).asInstanceOf[ActorRef]
    } else {
      store(fromState)
    }
    val instance = fromState.features(this) + (targetAttribute -> StringValue(toState.name))
    storeActor ! Write(instance)
  }

  def agentName = getClass.getCanonicalName
}

trait AgentState {
  def name : String
  def attributes() : Seq[Attribute]
  def features(agent :AnyRef) : Map[String, AttributeValue]
  def features(agent :AnyRef, msg :Any) :Map[String, AttributeValue] = {
    features(agent) + ("msg" -> StringValue(msg.toString))
  }
}

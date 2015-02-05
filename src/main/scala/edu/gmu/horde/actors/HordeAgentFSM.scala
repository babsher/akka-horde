package edu.gmu.horde.actors

import akka.actor.{ ActorRef, FSM, Actor }
import akka.pattern.ask
import com.typesafe.config.ConfigFactory
import edu.gmu.horde.storage._
import AttributeStore.NewAttributeStore
import edu.gmu.horde._
import weka.classifiers.Classifier
import weka.core.{Instance, Attribute}
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
  var attributeStore: ActorRef = _
  var store: Map[S, ActorRef] = Map()
  var training = false
  var env: ActorRef
  // create target attribute
  val targetAttribute = "nextState"
  val target = new Attribute(targetAttribute)
  for (state <- states) {
    target.addStringValue(state.name)
  }
  lazy val models: Map[S, Classifier] = loadModels(ConfigFactory.load().getString("horde.modelDir"))

  def loadModels(dir: String) = {
    (for (state <- this.states; c: Classifier = weka.core.SerializationHelper.read(dir + state.name).asInstanceOf[Classifier])
      yield (state -> c)) toMap
  }
  def states: Seq[S]

  case class To(state: S)
  // tells agent to do compute its next action
  case class ActionTimeout()

  def from(fromState: S)(toStates: Seq[To]) {
    when(fromState) {
      case Event(SetEnvironment(ref), _) =>
        env = ref
        stay
      case Event(Train(train), _) =>
        training = train
        if (training) {
          cancelTimer("action")
        } else {
          setTimer("action", ActionTimeout, 500 millis, true)
        }
        context.children.map(child => child ! Train(training))
        stay
      case Event(ActionTimeout, _) =>
        val nextState = getNextState(fromState, toStates)
        if (nextState == fromState) {
          getAction(nextState).onTick()
          stay
        } else {
          // exit/enter actions handled by #onTransition
          goto(nextState)
        }
      case Event(SetAttributeStore(storeActor: ActorRef), _) =>
        attributeStore = storeActor
        stay
      case Event(nextState: S, _) =>
        toStates.find((to: To) => to.state == nextState) match {
          case Some(toState) =>
            if (training) {
              store(fromState, toState.state)
            }
            goto(nextState)
          case None =>
            log.warning("Unknown state " + nextState)
            stay
        }

      case a @ _ =>
        log.debug("Unknown state transition {}", a)
        stay
    }
  }

  onTransition {
    case fromState -> toState =>
      getAction(fromState).onExit()
      getAction(toState).onEnter()
  }

  def getNextState(currState: S, toStates: Seq[To]): S = {
    val f = features(currState)
    val state = classify(currState, f)
    val s = toStates.filter(x => x.state.name equals state)
    if (s.isEmpty) {
      log.debug("No state matching {} in toStates: {}", state, toStates)
      return currState
    } else {
      return s(0).state
    }
  }

  def classify(currState: S, features: Map[String, AttributeValue]): String = {
    val attr = attributes(currState)
    sendAttributes(features)
    val i = instance(attr, features)
    val next = models(currState).classifyInstance(i)
    // TODO make probabilistic models(currState).distributionForInstance(i)
    target.value(next.toInt)
  }

  case class AgentAttributes(features: Map[String, AttributeValue])

  def sendAttributes(features: Map[String, AttributeValue]) = {
    val msg = AgentAttributes(features)
    for(l: ActorRef <- listeners) {
      l ! msg
    }
  }

  def store(fromState: S, toState: S): Unit = {
    val storeActor = if (!store.contains(fromState)) {
      val attr = Seq(target) ++ attributes(fromState)
      val future = ask(attributeStore, NewAttributeStore(agentName, fromState, attr))(5 second)
      Await.result(future, 5 seconds).asInstanceOf[ActorRef]
    } else {
      store(fromState)
    }
    val instance = features(fromState) + (targetAttribute -> StringValue(toState.name))
    storeActor ! Write(instance)
  }

  def agentName = getClass.getCanonicalName

  def getAction(state: S): Action
  def attributes(state: S): Seq[Attribute]
  def features(state: S): Map[String, AttributeValue]
}

case class Action(onEnter: () => Unit, onTick: () => Unit, onExit: () => Unit);

trait HasAction[T] {
  def getAction(t: T): Action
  def NullAction = Action(() => {}, () => {}, () => {})
}

trait AgentState {
  def name: String
}
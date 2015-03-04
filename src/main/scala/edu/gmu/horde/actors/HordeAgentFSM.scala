package edu.gmu.horde.actors

import akka.actor.{ActorRef, FSM}
import akka.pattern.ask
import com.typesafe.config.ConfigFactory
import edu.gmu.horde.storage.AttributeStore.NewAttributeStore
import edu.gmu.horde.storage._
import edu.gmu.horde.{State => StateMsg, _}
import weka.classifiers.Classifier
import weka.core.Attribute

import scala.concurrent.Await
import scala.concurrent.duration._

/**
 */
trait HordeAgentFSM[S <: AgentState, D] extends FSM[S, D] with AttributeIO with Messages {
  val attributeStore: ActorRef
  var store: Map[S, ActorRef] = Map()
  var training = false
  val env: ActorRef
  // create target attribute
  val targetAttribute = "nextState"
  val target = new Attribute(targetAttribute)
  for (state <- states) {
    // TODO make sure name matches classname
    target.addStringValue(state.name)
  }
  lazy val models: Map[S, Classifier] = loadModels(ConfigFactory.load().getString("horde.modelDir"))

  def loadModels(dir: String) = {
    (for (state <- this.states; c: Classifier = weka.core.SerializationHelper.read(dir + state.name).asInstanceOf[Classifier])
    yield (state -> c)) toMap
  }

  // tells agent to do compute its next action
  case class ActionTimeout()

  def from(fromState: S)(toStates: Seq[S]) {
    when(fromState) {
      case Event(Train(train), _) =>
        log.debug("Setting {} to train: {}", self, train)
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

      case Event(RequestAgentDetail, _) =>
        println("States " + states)
        val possibleStates = for(s <- states) yield
          AgentPossibleStates(s.name, (toStates contains s) || isCurrentState(s), isCurrentState(s))
        println("Possibles states " + possibleStates)
        sender ! AgentDetail(self, training, getType, currentState, possibleStates, features(fromState))
        stay

      case Event(stateMsg: StateMsg, _) =>
        if(training) {
          states.find(_.name == stateMsg.state) match {
            case Some(nextState) =>
              toStates.find(_ == nextState) match {
                case Some(toState) =>
                  store(fromState, toState)
                  sender ! stateMsg
                  goto(nextState)
                case None =>
                  sender ! StateMsg("ERROR")
                  log.warning("Not a valid state transition to {} from {}", nextState, fromState.name)
              }
            case None =>
              sender ! StateMsg("ERROR")
              log.warning("Not a valid state {} ", stateMsg)
          }
        } else {
          log.warning("Cannot transition to state '{}' if not training", stateMsg)
        }
        stay

      case Event(RequestState, _) =>
        respondToRequestState(sender())
        stay

      case a@_ =>
        log.debug("Unknown message {}", a)
        stay
    }
  }

  onTransition {
    case fromState -> toState =>
      getAction(fromState).onExit()
      getAction(toState).onEnter()
  }

  def currentState: StateMsg = {
    StateMsg(stateName.name)
  }
  
  def isCurrentState(state: S) = {
    stateName equals state
  }

  def respondToRequestState(sender: ActorRef): Unit = {
    sender ! currentState
  }

  def getNextState(currState: S, toStates: Seq[S]): S = {
    val f = features(currState)
    val state = classify(currState, f)
    val s = toStates.filter(_.name equals state)
    if (s.isEmpty) {
      log.debug("No state matching {} in toStates: {}", state, toStates)
      return currState
    } else {
      return s(0)
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
    // Broadcase agent features to listeners
    val msg = AgentAttributes(features)
    gossip(msg)
  }

  def store(fromState: S, toState: S): Unit = {
    log.debug("Recording state transition {}=>{}", fromState, toState)
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

  def states: Seq[S]

  def getAction(state: S): Action

  def attributes(state: S): Seq[Attribute]

  def features(state: S): Map[String, AttributeValue]

  def getType: String

  def NullAction = Action(() => {}, () => {}, () => {})
}

case class Action(onEnter: () => Unit, onTick: () => Unit, onExit: () => Unit);

trait AgentState {
  def name: String
}

package edu.gmu.horde

import akka.pattern.ask
import akka.actor.{ActorContext, ActorRef, LoggingFSM, FSM}
import edu.gmu.horde.AttributeStore.NewAttributeStore
import edu.gmu.horde.zerg.agents.Drone
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

  case class To(state: S, f: (Event) => Unit)

  def from(fromState: S)(toStates: Seq[To]) {
    when(fromState) {
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

      case a@_ =>
        log.debug("Unknown state transition {}", a)
        stay
    }
  }

  def store(fromState :S, toState :S) : Unit = {
    val storeActor = if(!store.contains(fromState)) {
      val future = ask(attributeStore, NewAttributeStore(getClass.getCanonicalName, fromState.name, fromState.attributes))(5 second)
      Await.result(future, 5 seconds).asInstanceOf[ActorRef]
    } else {
      store(fromState)
    }

    storeActor ! Write(fromState.features(this))
  }
}

trait AgentState {
  def attributes() : Seq[Attribute] = ???
  def features(d : AnyRef) : Map[String, AttributeValue] = ???
  def name() : String = ???
}

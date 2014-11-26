package edu.gmu.horde

import akka.pattern.ask
import akka.actor.{ActorContext, ActorRef, LoggingFSM, FSM}
import edu.gmu.horde.AttributeStore.NewAttributeStore
import edu.gmu.horde.zerg.agents.Drone
import weka.core.Attribute

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
  val attributeStore : ActorRef

  case class To[S](state: S, f: (Event) => Unit)

  def from(fromState: S)(toStates: Seq[To[S]]) {

    when(fromState) {
      case e@Event(nextState: S, _) =>
        val option = toStates.find((to: To[S]) => to.state == nextState)
        option match {
          case Some(toState) =>
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
    val future = ask(attributeStore, NewAttributeStore(getClass.getCanonicalName, fromState.name, fromState.attributes))
    val store :ActorRef =
    store ! Write(fromState.features(this))
  }
}

trait AgentState[T <: HordeAgentFSM] {
  var store : ActorRef
  def attributes() : Seq[Attribute] = ???
  def features(d : HordeAgentFSM) : Map[String, AttributeValue] = ???
  def name() : String = ???
}

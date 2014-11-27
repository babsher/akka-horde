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
trait HordeAgentFSM[S <: AgentState[HordeAgentFSM[S, D]], D] {
  this: FSM[S, D] =>
  val attributeStore : ActorRef
  var store :Map[S, ActorRef] = Map()

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
    if(!store.contains(fromState)) {
      val future = ask(attributeStore, NewAttributeStore(getClass.getCanonicalName, fromState.name, fromState.attributes))(5 second)
      val s :ActorRef = Await.result(future, 5 seconds).asInstanceOf[ActorRef]
      s ! Write(fromState.features(this))
    } else {
      store(fromState) ! Write(fromState.features(this))
    }
  }
}

trait AgentState[AgentType] {
  def attributes() : Seq[Attribute] = ???
  def features(d : AgentType) : Map[String, AttributeValue] = ???
  def name() : String = ???
}

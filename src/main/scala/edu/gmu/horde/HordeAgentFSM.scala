package edu.gmu.horde

import akka.actor.{LoggingFSM, FSM}

/**
 * A simple extension of Akka's <code>FSM</code>.  In this class, state transitions can be defined with the
 * <code>from</code> state and <code>to</code> state constructs.
 * <p>
 * Note that <code>Function1</code> can be replaced with <code>PartialFunction</code>.
 *
 * @param S denotes the user specified state, e.g. Initialized, Started
 * @param D denotes the user specified data model
 */
trait HordeAgentFSM[S, D] {
  this: FSM[S, D] =>
  import akka.actor.FSM.Event

  case class To[S](state: S, f: (Event) => Unit, features: () => Map[String, AttributeValue])

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
}
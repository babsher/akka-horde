package edu.gmu.horde

import akka.actor._
import edu.gmu.horde.zerg.env.ZergEnvironment
import org.slf4j.LoggerFactory
import scala.concurrent.duration._

case object HordeFSM {
  def logger = LoggerFactory.getLogger(this.getClass)
}

sealed trait HordeFSMState
case object Idle extends HordeFSMState
case object Running extends HordeFSMState
case object Training extends HordeFSMState
case object Stopping extends HordeFSMState
case object Stopped extends HordeFSMState

sealed trait HordeFSMData
case object Uninitialized extends HordeFSMData
case class EnvironmentData(env: ActorRef) extends HordeFSMData
case object Halt extends HordeFSMData

case class Scenario(name: String)
case class SetEnvironment(env: ActorRef)
case class SetRoot(env: ActorRef)
case class SetAttributeStore(store: ActorRef)
case class Run(connect :Boolean)
case object Stop

class HordeFSM extends Actor with LoggingFSM[HordeFSMState, HordeFSMData] {
  import HordeFSM.logger

  var env :ActorRef = null
  var root :ActorRef = null
  var store :ActorRef = null

  startWith(Idle, Uninitialized)

  when(Idle) {
    case Event(StateTimeout, _) =>
      goto(Idle)
    case Event(Scenario(name), Uninitialized) =>
      logger.debug("Setting up Scenario {}", name)
      root = context.actorOf(Props[Root], "root")
      env = context.actorOf(Props[ZergEnvironment], "env")
      store = context.actorOf(AttributeStore.props(name), "store")
      root ! SetEnvironment(env)
      env ! SetRoot(root)
      stay
    case Event(Run(value), _) =>
      if(root != null) {
        root ! Run(value)
        env ! Run(value)
        goto(Running) using new EnvironmentData(env)
      } else {
        stay
      }
  }

  when(Running) {
    case Event(StateTimeout, _) =>
      goto(Stopped)
    case Event(Stop, _) =>
      goto(Stopped)
  }

  when(Stopped) (FSM.NullFunction)

  onTransition {
    case x -> y => log.debug("Entering " + y + " from " + x)
  }

  initialize()
}

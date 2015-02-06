package edu.gmu.horde.actors

import akka.actor._
import edu.gmu.horde.storage.AttributeStore
import edu.gmu.horde.zerg.env.ZergEnvironment
import org.slf4j.LoggerFactory

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
case class Run(connect :Boolean, train :Boolean)
case class Train(train :Boolean)
case object Stop

class HordeFSM extends Actor with LoggingFSM[HordeFSMState, HordeFSMData] {
  import edu.gmu.horde.actors.HordeFSM.logger

  var env :ActorRef = null
  var root :ActorRef = context.actorOf(Props[Root], "root")
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
    case Event(Run(conn, train), _) =>
      doRun(conn, train)
    case Event(RequestAgentInfo(null), _) =>
      root ! RequestAgentInfo(context.sender())
      stay
  }

  when(Running) {
    case Event(Stop, _) =>
      goto(Stopped)
    case Event(Run(conn, train), _) =>
      doRun(conn, train)
  }

  when(Training) {
    case Event(Stop, _) =>
      goto(Stopped)
    case Event(Run(conn, train), _) =>
      doRun(conn, train)
  }

  when(Stopped) (FSM.NullFunction)

  def doRun(connect :Boolean, train :Boolean) = {
    if(root != null) {
      root ! Run(connect, train)
      env ! Run(connect, train)
      if(train) {
        goto(Training) using new EnvironmentData(env)
      } else {
        goto(Running) using new EnvironmentData(env)
      }
    } else {
      stay
    }
  }

  onTransition {
    case _ -> Stopped =>
      root ! Stop
      env ! Stop
  }

  initialize()
}

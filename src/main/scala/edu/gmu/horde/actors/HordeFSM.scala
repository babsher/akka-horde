package edu.gmu.horde.actors

import akka.actor._
import edu.gmu.horde._
import edu.gmu.horde.storage.{AttributeValue, AttributeStore}
import edu.gmu.horde.zerg._
import edu.gmu.horde.zerg.env
import edu.gmu.horde.zerg.env.ZergEnvironment
import org.slf4j.LoggerFactory

case object HordeFSM {
  def logger = LoggerFactory.getLogger(this.getClass)
}

sealed trait HordeFSMState
case object Running extends HordeFSMState
case object Stopping extends HordeFSMState
case object Stopped extends HordeFSMState

sealed trait HordeFSMData
case object Uninitialized extends HordeFSMData
case class EnvironmentData(env: ActorRef) extends HordeFSMData
case object Halt extends HordeFSMData

class HordeFSM extends Actor with LoggingFSM[HordeFSMState, HordeFSMData] with Messages {
  import edu.gmu.horde.actors.HordeFSM.logger

  var env :ActorRef = null
  var root :ActorRef = context.actorOf(Props[Root], "root")
  var store :ActorRef = null

  startWith(Stopped, Uninitialized)

  when(Stopped) {
    case Event(StateTimeout, _) =>
      goto(Stopped)
    case Event(Scenario(name), Uninitialized) =>
      logger.debug("Setting up Scenario {}", name)
      root = context.actorOf(Props[Root], "root")
      env = context.actorOf(Props[ZergEnvironment], "env")
      store = context.actorOf(AttributeStore.props(name), "store")
      root ! SetEnvironment(env)
      env ! SetRoot(root)
      stay
    case Event(Run(conn), _) =>
      doRun(conn)
    case Event(RequestAgentInfo(null), _) =>
      if(root != null) {
        root ! RequestAgentInfo(context.sender())
      }
      stay
    case Event(RequestState, _) =>
      respondToRequestState(sender())
      stay
    case Event(State(stateName), _) =>
      if(stateName == Running.toString) {
          goto(Running)
      } else {
        stay
      }
  }

  when(Running) {
    case Event(Stop, _) =>
      goto(Stopped)
    case Event(Run(conn), _) =>
      doRun(conn)
    case Event(RequestState, _) =>
      respondToRequestState(sender())
      stay
    case Event(State(stateName), _) =>
      if(stateName == Running.toString) {
        stay
      } else {
        context.children.map(_ ! PoisonPill)
        goto(Stopping)
      }
  }
  
  def respondToRequestState(sender: ActorRef): Unit = {
    sender ! HordeState(self, State(this.stateName.toString), root)
  }

  def doRun(connect :Boolean) = {
    if(root != null) {
      root ! Run(connect)
      env ! Run(connect)
      goto(Running) using new EnvironmentData(env)
    } else {
      stay
    }
  }

  onTransition {
    case _ -> Stopped =>
      root ! Run(false)
      env ! Run(false)
  }

  initialize()
}

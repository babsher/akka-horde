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

class HordeFSM extends Actor with LoggingFSM[HordeFSMState, HordeFSMData] {
  import HordeFSM.logger

  startWith(Idle, Uninitialized)

  when(Idle) {
    case Event(StateTimeout, _) =>
      goto(Idle)
    case Event(Scenario(name), Uninitialized) =>
      logger.debug("Now running {}", name)
      val root = context.actorOf(Props[Root])
      val env = context.actorOf(Props[ZergEnvironment])
      root ! SetEnvironment(env)
      env ! SetRoot(root)
      goto(Running) using new EnvironmentData(env)
  }

  when(Running, stateTimeout = 10 seconds) {
    case Event(StateTimeout, _) =>
      goto(Stopped)
  }

  when(Stopped) (FSM.NullFunction)

  initialize()
}

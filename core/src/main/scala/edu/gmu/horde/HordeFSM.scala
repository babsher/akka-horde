package edu.gmu.horde

import akka.actor._
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
case class EnvironmentData(env: Environment) extends HordeFSMData
case object Halt extends HordeFSMData

final case class Scenario(name: String)

class HordeFSM extends Actor with LoggingFSM[HordeFSMState, HordeFSMData] {
  import HordeFSM.logger

  startWith(Idle, Uninitialized)

  when(Idle) {
    case Event(StateTimeout, _) =>
      goto(Idle)
    case Event(Scenario(name), Uninitialized) =>
      logger.debug("Now running {}", name)
      goto(Running) using new EnvironmentData(null)
  }

  when(Running, stateTimeout = 10 seconds) {
    case Event(StateTimeout, _) =>
      goto(Stopped)
  }

  when(Stopped) (FSM.NullFunction)

  initialize()
}

package edu.gmu.horde.zerg.agents

import akka.actor.{FSM, Actor}


object Platoon {
  trait States
  case object Start extends States
  case object Moving extends States
  case object Attacking extends States
  case object Idle extends States

  trait Features
  case object Uninitialized extends Features
  case object MoveTarget extends Features
}

class Platoon extends Actor with FSM[Platoon.States, Platoon.Features] {
  
}

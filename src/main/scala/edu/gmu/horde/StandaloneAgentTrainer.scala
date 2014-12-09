package edu.gmu.horde

import jnibwapi.{Player, JNIBWAPI, BWAPIEventListener, Position, Unit => BUnit}
import jnibwapi.types.RaceType.RaceTypes

import scala.collection.JavaConversions._

class StandaloneAgentTrainer extends App with Trainer with BWAPIEventListener {

  val bwapi = new JNIBWAPI(new StandaloneAgentTrainer(), false)
  var self :Player = null
  bwapi.start()

  override def connected(): Unit = {
    self = bwapi.getPlayers.find(p => p.getRace == RaceTypes.Zerg).get
  }

  override def matchFrame(): Unit = {

  }

  override def keyPressed(i: Int): Unit = ???

  override def unitComplete(i: Int): Unit = ???

  override def playerLeft(i: Int): Unit = ???

  override def nukeDetect(position: Position): Unit = ???

  override def nukeDetect(): Unit = ???

  override def unitDiscover(i: Int): Unit = ???

  override def unitMorph(i: Int): Unit = ???

  override def unitShow(i: Int): Unit = ???

  override def saveGame(s: String): Unit = ???

  override def sendText(s: String): Unit = ???

  override def playerDropped(i: Int): Unit = ???

  override def unitCreate(i: Int): Unit = ???

  override def unitRenegade(i: Int): Unit = ???

  override def matchEnd(b: Boolean): Unit = ???

  override def unitHide(i: Int): Unit = ???

  override def unitDestroy(i: Int): Unit = ???

  override def receiveText(s: String): Unit = ???

  override def unitEvade(i: Int): Unit = ???

  override def matchStart(): Unit = ???
}

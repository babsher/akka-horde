package edu.gmu.horde

import java.io.{PrintStream, File}

import jnibwapi.types.UnitCommandType
import jnibwapi.types.UnitType.UnitTypes
import jnibwapi.{Unit => BUnit, Position, BWAPIEventListener, Player, JNIBWAPI}
import jnibwapi.types.RaceType.RaceTypes

import scala.collection.JavaConversions._

class StandaloneAgentTrainer extends App with Trainer {
  val lastCommands = collection.mutable.Map[Int, Int]()
  val outputFile = new File("data.out")
  val out = new PrintStream(outputFile)
  val bwapi = new JNIBWAPI(new BWListener(), false)
  var self :Player = null
  bwapi.start()



  class BWListener extends BWAPIEventListener {

    override def connected(): Unit = {
      self = bwapi.getPlayers.find(p => p.getRace == RaceTypes.Zerg).get
    }

    override def matchFrame(): Unit = {
      val drones = bwapi.getUnits(self)
        .filter(u => u.getType == UnitTypes.Zerg_Drone)
      for(drone <- drones) {
        val cmd = drone.getLastCommand
        if(lastCommands contains drone.getID) {
          if(lastCommands(drone.getID) != drone.getLastCommandFrame) {
            writeCommand(drone, cmd)
          }
        } else {
          writeCommand(drone, cmd)
        }
      }
    }

    def writeCommand(u :BUnit, cmd :UnitCommandType) = {
      out.println(u.getID + " " + cmd.getName)
      lastCommands(u.getID) = u.getLastCommandFrame
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
}

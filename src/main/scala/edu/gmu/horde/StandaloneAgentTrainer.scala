package edu.gmu.horde

import java.io._

import jnibwapi.types.OrderType.OrderTypes
import jnibwapi.types.{OrderType, UnitCommandType}
import jnibwapi.types.UnitType.UnitTypes
import jnibwapi.{Unit => BUnit, Position, BWAPIEventListener, Player, JNIBWAPI}
import jnibwapi.types.RaceType.RaceTypes
import org.slf4j.LoggerFactory

import scala.collection.JavaConversions._
import collection.mutable.{HashMap, MultiMap, Set}

object StandaloneAgentTrainer extends App with Trainer {

  case class HordeOrderType(id: Int, name: String)
  case class HordePosition(x: Int, y: Int)
  case class HordeTarget(unit: Int, unitType: Int, pos: HordePosition)
  case class HordeUnit(unitId: Int, unitType: Int)
  case class UnitOrder(order: HordeOrderType, unitPos: HordePosition, target: Option[HordeTarget], targetPos: Option[HordePosition], atTarget: List[HordeUnit])

  implicit def toHorde(pos: Position): HordePosition = {
    if(pos == null) {
      return null
    }
    HordePosition(pos.getPX, pos.getPY)
  }

  implicit def toHorde(unit: BUnit): HordeTarget = {
    if(unit == null) {
      return null
    }
    HordeTarget(unit.getID, unit.getType.getID, unit.getPosition)
  }

  implicit def toHorde(order: OrderType): HordeOrderType = {
    if(order == null) {
      return null
    }
    cmdName(order.getID)
  }

  val cmdName = Map(
    0 -> HordeOrderType(0, "Die"),
    1 -> HordeOrderType(1, "Stop"),
    2 -> HordeOrderType(2, "Guard"),
    3 -> HordeOrderType(3, "PlayerGuard"),
    4 -> HordeOrderType(4, "TurretGuard"),
    5 -> HordeOrderType(5, "BunkerGuard"),
    6 -> HordeOrderType(6, "Move"),
    // public static final OrderType ReaverStop = new OrderType(7); // Unused
    8 -> HordeOrderType(8, "Attack"),
    9 -> HordeOrderType(9, "AttackShrouded"),
    10 -> HordeOrderType(10, "AttackUnit"),
    // public static final OrderType AttackFixedRange = new OrderType(11); // Unused
    12 -> HordeOrderType(12, "AttackTile"),
    13 -> HordeOrderType(13, "Hover"),
    14 -> HordeOrderType(14, "AttackMove"),
    15 -> HordeOrderType(15, "InfestedCommandCenter"),
    16 -> HordeOrderType(16, "UnusedNothing"),
    17 -> HordeOrderType(17, "UnusedPowerup"),
    18 -> HordeOrderType(18, "TowerGuard"),
    19 -> HordeOrderType(19, "FailedCasting"),
    20 -> HordeOrderType(20, "VultureMine"),
    23 -> HordeOrderType(23, "Nothing"),
    24 -> HordeOrderType(24, "Nothing3"),
    28 -> HordeOrderType(27, "CastInfestation"),
    29 -> HordeOrderType(29, "InfestingCommandCenter"),
    30 -> HordeOrderType(30, "PlaceBuilding"),
    32 -> HordeOrderType(32, "BuildProtoss2"),
    33 -> HordeOrderType(33, "ConstructingBuilding"),
    34 -> HordeOrderType(34, "Repair"),
    36 -> HordeOrderType(36, "PlaceAddon"),
    37 -> HordeOrderType(37, "BuildAddon"),
    38 -> HordeOrderType(38, "Train"),
    39 -> HordeOrderType(39, "RallyPointUnit"),
    40 -> HordeOrderType(40, "RallyPointTile"),
    41 -> HordeOrderType(41, "ZergBirth"),
    42 -> HordeOrderType(42, "ZergUnitMorph"),
    43 -> HordeOrderType(43, "ZergBuildingMorph"),
    44 -> HordeOrderType(44, "IncompleteBuilding"),
    46 -> HordeOrderType(46, "BuildNydusExit"),
    47 -> HordeOrderType(47, "EnterNydusCanal"),
    49 -> HordeOrderType(49, "Follow"),
    50 -> HordeOrderType(50, "Carrier"),
    51 -> HordeOrderType(51, "ReaverCarrierMove"),
    53 -> HordeOrderType(53, "CarrierAttack1"),
    54 -> HordeOrderType(54, "CarrierAttack2"),
    55 -> HordeOrderType(55, "CarrierIgnore2"),
    58 -> HordeOrderType(58, "Reaver"),
    59 -> HordeOrderType(59, "ReaverAttack1"),
    60 -> HordeOrderType(60, "ReaverAttack2"),
    63 -> HordeOrderType(63, "TrainFighter"),
    64 -> HordeOrderType(64, "InterceptorAttack"),
    65 -> HordeOrderType(65, "ScarabAttack"),
    66 -> HordeOrderType(66, "RechargeShieldsUnit"),
    67 -> HordeOrderType(67, "RechargeShieldsBattery"),
    68 -> HordeOrderType(68, "ShieldBattery"),
    69 -> HordeOrderType(69, "InterceptorReturn"),
    71 -> HordeOrderType(71, "BuildingLand"),
    72 -> HordeOrderType(72, "BuildingLiftOff"),
    73 -> HordeOrderType(73, "DroneLiftOff"),
    74 -> HordeOrderType(74, "LiftingOff"),
    75 -> HordeOrderType(75, "ResearchTech"),
    76 -> HordeOrderType(76, "Upgrade"),
    77 -> HordeOrderType(77, "Larva"),
    78 -> HordeOrderType(78, "SpawningLarva"),
    79 -> HordeOrderType(79, "Harvest1"),
    80 -> HordeOrderType(80, "Harvest2"),
    81 -> HordeOrderType(81, "MoveToGas"),
    82 -> HordeOrderType(82, "WaitForGas"),
    83 -> HordeOrderType(83, "HarvestGas"),
    84 -> HordeOrderType(84, "ReturnGas"),
    85 -> HordeOrderType(85, "MoveToMinerals"),
    86 -> HordeOrderType(86, "WaitForMinerals"),
    87 -> HordeOrderType(87, "MiningMinerals"),
    88 -> HordeOrderType(88, "Harvest3"),
    89 -> HordeOrderType(89, "Harvest4"),
    90 -> HordeOrderType(90, "ReturnMinerals"),
    91 -> HordeOrderType(91, "Interrupted"),
    92 -> HordeOrderType(92, "EnterTransport"),
    93 -> HordeOrderType(93, "PickupIdle"),
    94 -> HordeOrderType(94, "PickupTransport"),
    95 -> HordeOrderType(95, "PickupBunker"),
    96 -> HordeOrderType(96, "Pickup4"),
    97 -> HordeOrderType(97, "PowerupIdle"),
    98 -> HordeOrderType(98, "Sieging"),
    99 -> HordeOrderType(99, "Unsieging"),
    101 -> HordeOrderType(101, "InitCreepGrowth"),
    102 -> HordeOrderType(102, "SpreadCreep"),
    103 -> HordeOrderType(103, "StoppingCreepGrowth"),
    104 -> HordeOrderType(104, "GuardianAspect"),
    105 -> HordeOrderType(105, "ArchonWarp"),
    106 -> HordeOrderType(106, "CompletingArchonsummon"),
    107 -> HordeOrderType(107, "HoldPosition"),
    109 -> HordeOrderType(109, "Cloak"),
    110 -> HordeOrderType(110, "Decloak"),
    111 -> HordeOrderType(111, "Unload"),
    112 -> HordeOrderType(112, "MoveUnload"),
    113 -> HordeOrderType(113, "FireYamatoGun"),
    114 -> HordeOrderType(114, "MoveToFireYamatoGun"),
    115 -> HordeOrderType(115, "CastLockdown"),
    116 -> HordeOrderType(116, "Burrowing"),
    117 -> HordeOrderType(117, "Burrowed"),
    118 -> HordeOrderType(118, "Unburrowing"),
    119 -> HordeOrderType(119, "CastDarkSwarm"),
    120 -> HordeOrderType(120, "CastParasite"),
    121 -> HordeOrderType(121, "CastSpawnBroodlings"),
    122 -> HordeOrderType(122, "CastEMPShockwave"),
    123 -> HordeOrderType(123, "NukeWait"),
    124 -> HordeOrderType(124, "NukeTrain"),
    125 -> HordeOrderType(125, "NukeLaunch"),
    126 -> HordeOrderType(126, "NukePaint"),
    127 -> HordeOrderType(127, "NukeUnit"),
    128 -> HordeOrderType(128, "CastNuclearStrike"),
    129 -> HordeOrderType(129, "NukeTrack"),
    131 -> HordeOrderType(131, "CloakNearbyUnits"),
    132 -> HordeOrderType(132, "PlaceMine"),
    133 -> HordeOrderType(133, "RightClickAction"),
    137 -> HordeOrderType(137, "CastRecall"),
    138 -> HordeOrderType(138, "TeleporttoLocation"),
    139 -> HordeOrderType(139, "CastScannerSweep"),
    140 -> HordeOrderType(140, "Scanner"),
    141 -> HordeOrderType(141, "CastDefensiveMatrix"),
    142 -> HordeOrderType(142, "CastPsionicStorm"),
    143 -> HordeOrderType(143, "CastIrradiate"),
    144 -> HordeOrderType(144, "CastPlague"),
    145 -> HordeOrderType(145, "CastConsume"),
    146 -> HordeOrderType(146, "CastEnsnare"),
    147 -> HordeOrderType(147, "CastStasisField"),
    148 -> HordeOrderType(148, "CastHallucination"),
    149 -> HordeOrderType(149, "Hallucination2"),
    150 -> HordeOrderType(150, "ResetCollision"),
    152 -> HordeOrderType(152, "Patrol"),
    153 -> HordeOrderType(153, "CTFCOPInit"),
    154 -> HordeOrderType(154, "CTFCOP1"),
    155 -> HordeOrderType(155, "CTFCOP2"),
    156 -> HordeOrderType(156, "ComputerAI"),
    157 -> HordeOrderType(157, "AtkMoveEP"),
    158 -> HordeOrderType(158, "HarassMove"),
    159 -> HordeOrderType(159, "AIPatrol"),
    160 -> HordeOrderType(160, "GuardPost"),
    161 -> HordeOrderType(161, "RescuePassive"),
    162 -> HordeOrderType(162, "Neutral"),
    163 -> HordeOrderType(163, "ComputerReturn"),
    165 -> HordeOrderType(165, "SelfDestrucing"),
    166 -> HordeOrderType(166, "Critter"),
    167 -> HordeOrderType(167, "HiddenGun"),
    168 -> HordeOrderType(168, "OpenDoor"),
    169 -> HordeOrderType(169, "CloseDoor"),
    170 -> HordeOrderType(170, "HideTrap"),
    171 -> HordeOrderType(171, "RevealTrap"),
    172 -> HordeOrderType(172, "Enabledoodad"),
    173 -> HordeOrderType(173, "Disabledoodad"),
    174 -> HordeOrderType(174, "Warpin"),
    175 -> HordeOrderType(175, "Medic"),
    176 -> HordeOrderType(176, "MedicHeal1"),
    177 -> HordeOrderType(177, "HealMove"),
    179 -> HordeOrderType(179, "MedicHeal2"),
    180 -> HordeOrderType(180, "CastRestoration"),
    181 -> HordeOrderType(181, "CastDisruptionWeb"),
    182 -> HordeOrderType(182, "CastMindControl"),
    183 -> HordeOrderType(183, "DarkArchonMeld"),
    184 -> HordeOrderType(184, "CastFeedback"),
    185 -> HordeOrderType(185, "CastOpticalFlare"),
    186 -> HordeOrderType(186, "CastMaelstrom"),
    187 -> HordeOrderType(187, "JunkYardDog"),
    188 -> HordeOrderType(188, "Fatal"),
    189 -> HordeOrderType(189, "None"),
    190 -> HordeOrderType(190, "Unknown")
  )

  val log = LoggerFactory.getLogger(StandaloneAgentTrainer.getClass)
  val lastCommands = new HashMap[Int, Set[UnitOrder]] with MultiMap[Int, UnitOrder]
  val outputFile = new File("data.out")
  val out = new PrintWriter(new BufferedOutputStream(new FileOutputStream(outputFile)))
  val bwapi = new JNIBWAPI(new BWListener(), false)
  var self: Player = null
  bwapi.start()

  class BWListener extends BWAPIEventListener {

    override def matchStart(): Unit = {
      bwapi.setGameSpeed(0)
      bwapi.enablePerfectInformation()
      bwapi.sendText("Started horde interface")
      bwapi.drawIDs(true)
      self = bwapi.getPlayers.find(p => p.getRace == RaceTypes.Zerg).get
    }

    override def connected(): Unit = {
      log.debug("Connected")
    }

    override def matchFrame(): Unit = {
      // TODO map cmd to state changes
      // TODO create actors in training and send Set state
      val drones = bwapi.getUnits(self).filter(u => u.getType == UnitTypes.Zerg_Drone)
      for (drone <- drones) {
        if(drone.getSecondaryOrder != OrderTypes.Nothing) {
          log.debug("Not nothing")
        }
        val atTarget = bwapi.getUnitsOnTile(drone.getTargetPosition).map(u => HordeUnit(u.getID, u.getTypeID)).toList
        val cmd = UnitOrder(drone.getOrder,
          drone.getPosition,
          Some(drone.getTarget),
          Some(drone.getTargetPosition),
          atTarget)
        if (lastCommands contains drone.getID) {
          if (lastCommands.entryExists(drone.getID, _ != cmd.order.id)) {
            writeCommand(bwapi.getFrameCount, drone, cmd)
          } else {
            log.trace("Order already exists " + cmd + " in " + lastCommands(drone.getID))
          }
        } else {
          writeCommand(bwapi.getFrameCount, drone, cmd)
        }
      }
    }

    def writeCommand(frame: Int, u: BUnit, cmd: UnitOrder) = {
      out.println(frame + " " + u.getID + " " + cmd)
      lastCommands.addBinding(u.getID, cmd)
    }

    override def matchEnd(b: Boolean): Unit = {
      out.flush()
      out.close()
    }

    override def keyPressed(i: Int): Unit = {}

    override def unitComplete(i: Int): Unit = {}

    override def playerLeft(i: Int): Unit = {}

    override def nukeDetect(position: Position): Unit = {}

    override def nukeDetect(): Unit = {}

    override def unitDiscover(i: Int): Unit = {}

    override def unitMorph(i: Int): Unit = {}

    override def unitShow(i: Int): Unit = {}

    override def saveGame(s: String): Unit = {}

    override def sendText(s: String): Unit = {}

    override def playerDropped(i: Int): Unit = {}

    override def unitCreate(i: Int): Unit = {}

    override def unitRenegade(i: Int): Unit = {}

    override def unitHide(i: Int): Unit = {}

    override def unitDestroy(i: Int): Unit = {}

    override def receiveText(s: String): Unit = {}

    override def unitEvade(i: Int): Unit = {}
  }

}

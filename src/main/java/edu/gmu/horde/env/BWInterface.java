package edu.gmu.horde.env;

import akka.actor.ActorRef;
import com.google.common.collect.Lists;
import edu.gmu.horde.NewUnit;
import edu.gmu.horde.zerg.env.HordeCommand;
import edu.gmu.horde.zerg.env.MorphLarva;
import jnibwapi.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class BWInterface {
    private final Reader reader;
    public final AtomicInteger supplyCap;
    public final AtomicInteger currentSupply;
    public final AtomicBoolean connected;
    public final AtomicBoolean hasWinner;
    public final Map<Integer, Unit> units;
    public final Map<Integer, Unit> enemyUnits;
    public final Queue<Integer> newUnits;
    public final Queue<HordeCommand> commands;
    public final ActorRef env;
    public JNIBWAPI bwapi;
    private static final Logger log = LoggerFactory.getLogger(BWInterface.class);

    public BWInterface(ActorRef e) {
        env = e;
        hasWinner = new AtomicBoolean(false);
        commands = new ConcurrentLinkedQueue<>();
        newUnits = new ConcurrentLinkedQueue<>();
        units = new ConcurrentHashMap<>();
        enemyUnits = new ConcurrentHashMap<>();
        connected = new AtomicBoolean(false);
        supplyCap = new AtomicInteger(0);
        currentSupply = new AtomicInteger(0);
        reader = new Reader(this);
    }

    public boolean hasWinner() {
        return hasWinner.get();
    }

    public boolean isConnected() {
        if(reader.isAlive()) {
            return this.connected.get();
        }
        return false;
    }

    public void start() {
        this.bwapi = new JNIBWAPI(reader, false);
        reader.bwapi = this.bwapi;
        reader.start();
        while(!connected.get()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) { }
        }
        log.info("BW Interface Connected!");
    }

    public void shutdown() {
        // Try and kill the thread
        reader.interrupt();
    }

    private static class Reader extends Thread implements BWAPIEventListener {
        public JNIBWAPI bwapi;
        private final BWInterface bwInterface;
        private static final Logger log = LoggerFactory.getLogger(Reader.class);

        public Reader(BWInterface bwInterface) {
            this.bwInterface = bwInterface;
        }

        @Override
        public void run() {
            log.debug("Starting bridge");
            bwapi.start();
            bwInterface.connected.set(false);
        }

        @Override
        public void connected() {
            this.bwInterface.connected.set(true);
            log.info("Connected Called");
        }

        @Override
        public void matchStart() {
            bwapi.enableUserInput();
//            bwapi.setGameSpeed(0);
            bwapi.sendText("Started horde interface");
            bwapi.drawIDs(true);

            bwInterface.enemyUnits.clear();
            bwInterface.units.clear();
            bwInterface.commands.clear();
            bwInterface.newUnits.clear();
            bwInterface.hasWinner.set(false);
            log.info("Match Started");
        }

        @Override
        public void matchFrame() {
            bwInterface.supplyCap.set(bwapi.getSelf().getSupplyTotal());
            bwInterface.currentSupply.set(bwapi.getSelf().getSupplyUsed());
            List<HordeCommand> undoable = Lists.newArrayList();
            while(!bwInterface.commands.isEmpty()) {
                HordeCommand cmd = bwInterface.commands.poll();
                try {
                    if(cmd instanceof MorphLarva) {
                        if(!bwapi.canMake(((MorphLarva)cmd).morphType())) {
                            undoable.add(cmd);
                        } else {
                            cmd.run(bwapi);
                        }
                    } else {
                        cmd.run(bwapi);
                    }
                } catch (Throwable e) {
                    log.error("Error while running command " + cmd, e);
                }
            }
            // add undoable commands to the list again
            bwInterface.commands.addAll(undoable);
        }

        @Override
        public void matchEnd(boolean winner) {
            this.bwInterface.hasWinner.set(true);
        }

        @Override
        public void unitDiscover(int unitID) {}

        @Override
        public void unitEvade(int unitID) {}

        @Override
        public void unitShow(int unitID) {
            Unit u = bwapi.getUnit(unitID);
            log.debug("shown {} at {} ", u.getType().getName(), u.getPosition());
            if(bwapi.getEnemies().contains(u.getPlayer())) {
                bwInterface.enemyUnits.put(unitID, u);
            }
        }

        @Override
        public void unitHide(int unitID) {
            bwInterface.enemyUnits.remove(unitID);
            log.debug("hide {}", unitID);
        }

        @Override
        public void unitCreate(int unitID) {
            Unit u = bwapi.getUnit(unitID);
            if(bwapi.getSelf().equals(u.getPlayer()) && !bwInterface.units.containsKey(unitID)) {
                log.debug("Created {} at {}", u.getType().getName(), u.getPosition());
                bwInterface.units.put(unitID, u);
                bwInterface.newUnits.add(unitID);
                bwInterface.env.tell(new NewUnit(unitID, u), null);
            } else if (!bwapi.getEnemies().contains(u.getPlayer())) {
                log.trace("Nonplayer {} at {}", u.getType().getName(), u.getPosition());
            }
        }

        @Override
        public void unitDestroy(int unitID) {
            bwInterface.units.remove(unitID);
            bwInterface.enemyUnits.remove(unitID);
        }

        @Override
        public void unitComplete(int unitID) {
            unitCreate(unitID);
        }

        @Override
        public void unitMorph(int unitID) {
            unitCreate(unitID);
        }

        @Override
        public void unitRenegade(int unitID) {}

        @Override
        public void saveGame(String gameName) {}

        @Override
        public void playerDropped(int playerID) {}

        @Override
        public void keyPressed(int keyCode) {}

        @Override
        public void sendText(String text) {}

        @Override
        public void receiveText(String text) {}

        @Override
        public void playerLeft(int playerID) {}

        @Override
        public void nukeDetect(Position p) {}

        @Override
        public void nukeDetect() {}
    }
}

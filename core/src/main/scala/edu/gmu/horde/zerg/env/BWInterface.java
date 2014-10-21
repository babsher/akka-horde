package edu.gmu.horde.zerg.env;

import akka.actor.ActorRef;
import edu.gmu.horde.zerg.OnFrame;
import jnibwapi.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class BWInterface {
    private final Thread reader;
    public final AtomicInteger supplyCap;
    public final AtomicInteger currentSupply;
    public final AtomicBoolean connected;
    public final AtomicBoolean hasWinner;
    public final Map<Integer, Unit> units;
    public final Map<Integer, Unit> enemyUnits;
    public final Queue<Integer> newUnits;
    public final Queue<UnitCommand> commands;
    public final ActorRef env;

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
        reader = new Thread(new Reader(this));
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
        reader.start();
        while(!connected.get()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) { }
        }
    }

    public void shutdown() {
        // Try and kill the thread
        reader.interrupt();
    }

    private static class Reader implements Runnable, BWAPIEventListener {
        private final JNIBWAPI bwapi;
        private final BWInterface bwInterface;
        private static final Logger log = LoggerFactory.getLogger(Reader.class);

        public Reader(BWInterface bwInterface) {
            this.bwInterface = bwInterface;
            this.bwapi = new JNIBWAPI(this, false);
        }

        @Override
        public void run() {
            bwapi.start();
            bwInterface.connected.set(false);
        }

        @Override
        public void connected() {
            this.bwInterface.connected.set(true);
            log.info("BW Interface Connected!");
        }

        @Override
        public void matchStart() {
            bwapi.enableUserInput();
            bwapi.setGameSpeed(0);

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
            while(!bwInterface.commands.isEmpty()) {
                bwapi.issueCommand(bwInterface.commands.poll());
            }

            bwInterface.env.tell(new OnFrame(), null);
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
            log.trace("shown %s at %s ", u.getType().getName(), u.getPosition());
            if(bwapi.getEnemies().contains(u.getPlayer())) {
                bwInterface.enemyUnits.put(unitID, u);
            }
        }

        @Override
        public void unitHide(int unitID) {
            bwInterface.enemyUnits.remove(unitID);
            log.trace("hide %s", unitID);
        }

        @Override
        public void unitCreate(int unitID) {
            Unit u = bwapi.getUnit(unitID);
            if(bwapi.getSelf().equals(u.getPlayer())) {
                log.trace("Created %s at %s", u.getType().getName(), u.getPosition());
                bwInterface.units.put(unitID, u);
                bwInterface.newUnits.offer(unitID);
            } else if (!bwapi.getEnemies().contains(u.getPlayer())) {
                log.trace("Nonplayer %s at %s", u.getType().getName(), u.getPosition());
            }
        }

        @Override
        public void unitDestroy(int unitID) {
            bwInterface.units.remove(unitID);
            bwInterface.enemyUnits.remove(unitID);
        }

        @Override
        public void unitComplete(int unitID) {}

        @Override
        public void unitMorph(int unitID) {}

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

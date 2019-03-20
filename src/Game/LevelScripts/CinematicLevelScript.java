package Game.LevelScripts;

import Data.Coordinate;
import Data.SerializationVersion;
import Game.Entities.CombatEntity;
import Game.Entities.Entity;
import Game.FactionManager;

import java.util.ArrayList;

public class CinematicLevelScript extends LevelScript {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    protected ArrayList<Entity> getEntitiesOfName(String name){
        ArrayList<Entity> list = new ArrayList<>();
        for (Entity e : level.getEntities())
            if (e.getName().equals(name)) list.add(e);
        return list;
    }

    protected Entity getFirstEntityofName(String name){
        ArrayList<Entity> entities = getEntitiesOfName(name);
        if (entities.size() > 0)
            return entities.get(0);
        else
            return null;
    }

    protected void pathfindCombatEntities(ArrayList<Entity> entities, Coordinate goalPos){
        pathfindCombatEntities(entities, goalPos, Integer.MAX_VALUE, 150);
    }

    protected void pathfindCombatEntities(ArrayList<Entity> entities, Coordinate goalPos, int numCycles, int interval){
        for (int i = 0; i < numCycles; i++) {
            boolean entitiesMoved = false;
            for (Entity e : entities) {
                if (e instanceof CombatEntity) {
                    CombatEntity combatEntity = (CombatEntity) e;
                    if (combatEntity.pathToPosition(goalPos) > 0) entitiesMoved = true;
                }
            }
            if (entitiesMoved) sleep(interval);
            else return;
        }
    }

    protected void sleep(long millis){
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected byte getFactionPlayerOpinion(String factionName){
        byte bonus = (gi.getPlayer().getFactionAlignments().contains(factionName.toLowerCase())) ? gi.getFactionManager().getMembershipOpinionBonus() : 0;
        FactionManager.Faction faction = gi.getFactionManager().getFaction(factionName);
        if (faction != null) {
            return (byte)(bonus + faction.getOpinionOf("player"));
        } else
            return bonus;
    }
}

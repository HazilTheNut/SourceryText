package Game.Entities;

import Data.Coordinate;
import Data.EntityArg;
import Data.EntityStruct;
import Data.SerializationVersion;
import Engine.LayerManager;
import Game.Debug.DebugWindow;
import Game.GameInstance;

import java.util.ArrayList;

public class GameCharacter extends BasicEnemy {

    /**
     * GameCharacter:
     *
     * A refined version of the BasicEnemy, which uses the Faction system to determine allies and enemies.
     */

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    private ArrayList<String> factionAlignments;

    @Override
    public ArrayList<EntityArg> generateArgs() {
        ArrayList<EntityArg> args = super.generateArgs();
        args.add(new EntityArg("faction", "<faction1>, <faction2>..."));
        return args;
    }

    @Override
    public void initialize(Coordinate pos, LayerManager lm, EntityStruct entityStruct, GameInstance gameInstance) {
        super.initialize(pos, lm, entityStruct, gameInstance);
        factionAlignments = new ArrayList<>();
        String factionInput = readStrArg(searchForArg(entityStruct.getArgs(), "faction"), "");
        factionAlignments = gi.getFactionManager().extractFactionList(factionInput);
        DebugWindow.reportf(DebugWindow.MISC, "GameCharacter.initialize","uid: %1$d", getUniqueID());
        for (String faction : factionAlignments){
            DebugWindow.reportf(DebugWindow.MISC, "GameCharacter.initialize", faction);
        }
    }

    public ArrayList<String> getFactionAlignments() {
        return factionAlignments;
    }

    @Override
    protected CombatEntity getNearestEnemy() {
        ArrayList<Entity> entities = gi.getCurrentLevel().getEntities();
        for (Entity e : entities){
            if (e instanceof GameCharacter && e.getLocation().stepDistance(getLocation()) <= detectRange) {
                GameCharacter gc = (GameCharacter) e;
                if (gi.getFactionManager().getOpinion(this, gc) < 0)
                    return gc;
            }
        }
        return null;
    }

    @Override
    public void onTurn() {
        if (target instanceof GameCharacter) {
            GameCharacter gc = (GameCharacter) target;
            if (gi.getFactionManager().getOpinion(this, gc) > 0)
                target = null;
        }
        super.onTurn();
    }
}

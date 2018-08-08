package Game.Entities;

import Data.Coordinate;
import Data.EntityArg;
import Data.EntityStruct;
import Data.SerializationVersion;
import Engine.LayerManager;
import Engine.SpecialText;
import Game.Debug.DebugWindow;
import Game.GameInstance;

import java.awt.*;
import java.util.ArrayList;

public class GameCharacter extends BasicEnemy {

    /**
     * GameCharacter:
     *
     * A refined version of the BasicEnemy, which uses the Faction system to determine allies and enemies.
     */

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    protected ArrayList<String> factionAlignments;
    private SpecialText originalSprite;

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
        originalSprite = getSprite().getSpecialText(0, 0);
    }

    public ArrayList<String> getFactionAlignments() {
        return factionAlignments;
    }

    @Override
    protected CombatEntity getNearestEnemy() {
        DebugWindow.reportf(DebugWindow.GAME, String.format("GameCharacter#%1$05d.getNearestEnemy", getUniqueID()), "BEGIN EVAL");
        GameCharacter newTarget = null;
        int minDistance = Integer.MAX_VALUE;
        ArrayList<Entity> entities = gi.getCurrentLevel().getEntities();
        for (Entity e : entities){
            int dist = e.getLocation().stepDistance(getLocation());
            if (e instanceof GameCharacter && dist <= Math.min(detectRange, minDistance)) {
                GameCharacter gc = (GameCharacter) e;
                byte opinion = gi.getFactionManager().getOpinion(this, gc);
                DebugWindow.reportf(DebugWindow.GAME, String.format("GameCharacter#%1$05d.getNearestEnemy", getUniqueID()), "\'%1$-14s\' #%2$05d (%3$ d) dist: %4$d ", gc.getName(), gc.getUniqueID(), opinion, dist);
                if (opinion < 0) {
                    minDistance = dist;
                    newTarget = gc;
                }
            }
        }
        return newTarget;
    }

    @Override
    public void onTurn() {
        if (isAutonomous)
            if (target instanceof GameCharacter) {
                GameCharacter gc = (GameCharacter) target;
                if (gi.getFactionManager().getOpinion(this, gc) > 0)
                    target = null;
            }
        super.onTurn();
        if (isAutonomous)
            if (target != null)
                DebugWindow.reportf(DebugWindow.ENTITY, String.format("GameCharacter#%1$05d.onTurn", getUniqueID()), "target: %1$s #%2$d", target.getName(), target.getUniqueID());
            else
                DebugWindow.reportf(DebugWindow.ENTITY, String.format("GameCharacter#%1$05d.onTurn", getUniqueID()), "target: null");
    }

    @Override
    public void onLevelEnter() {
        super.onLevelEnter();
        updateSprite();
    }

    @Override
    protected void updateSprite() {
        if (gi.getFactionManager().getOpinion(this, gi.getPlayer()) < 0)
            redifyIcon();
        else
            setIcon(originalSprite);
        super.updateSprite();
    }

    private void redifyIcon(){
        setIcon(new SpecialText(originalSprite.getCharacter(), redifyColor(originalSprite.getFgColor()), redifyColor(originalSprite.getBkgColor())));
    }

    private Color redifyColor(Color color){
        float[] hsb = new float[3];
        hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsb);
        hsb[0] = 0;
        Color fromHsb = Color.getHSBColor(hsb[0], hsb[1], hsb[2]);
        return new Color(fromHsb.getRed(), fromHsb.getGreen(), fromHsb.getBlue(), color.getAlpha());
    }
}

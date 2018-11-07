package Game.Entities;

import Data.Coordinate;
import Data.EntityArg;
import Data.EntityStruct;
import Data.SerializationVersion;
import Engine.LayerManager;
import Engine.SpecialText;
import Game.*;
import Game.Debug.DebugWindow;

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

    private GameCharacter recentDamageDealer;

    private ArrayList<String> interactText;
    private String rawInteractText;
    private int interactTextPointer = 0;

    @Override
    public ArrayList<EntityArg> generateArgs() {
        ArrayList<EntityArg> args = super.generateArgs();
        args.add(new EntityArg("faction", "<faction1>, <faction2>..."));
        args.add(new EntityArg("interactText", "\"message1\",\"message2\",..."));
        return args;
    }

    @Override
    public void initialize(Coordinate pos, LayerManager lm, EntityStruct entityStruct, GameInstance gameInstance) {
        factionAlignments = new ArrayList<>();
        String factionInput = readStrArg(searchForArg(entityStruct.getArgs(), "faction"), "");
        factionAlignments = gameInstance.getFactionManager().extractFactionList(factionInput);
        DebugWindow.reportf(DebugWindow.MISC, "GameCharacter.initialize","uid: %1$d", getUniqueID());
        for (String faction : factionAlignments){
            DebugWindow.reportf(DebugWindow.MISC, "GameCharacter.initialize", faction);
        }
        originalSprite = readSpecTxtArg(searchForArg(entityStruct.getArgs(), "icon"), entityStruct.getDisplayChar());
        super.initialize(pos, lm, entityStruct, gameInstance);
        interactText = readStringList(searchForArg(entityStruct.getArgs(), "interactText"));
        rawInteractText = readStrArg(searchForArg(entityStruct.getArgs(), "interactText"), "");
        dialogueParser = new DialogueParser(getGameInstance(), rawInteractText);
    }

    public ArrayList<String> getFactionAlignments() {
        return factionAlignments;
    }

    @Override
    protected byte getOpinion(Entity e) {
        if (e instanceof GameCharacter) {
            GameCharacter gc = (GameCharacter) e;
            byte opinion = gi.getFactionManager().getOpinion(this, gc);
            DebugWindow.reportf(DebugWindow.GAME, String.format("GameCharacter#%1$05d.getOpinion", getUniqueID()), "\'%1$-14s\' #%2$05d (%3$ d) dist: %4$d ", gc.getName(), gc.getUniqueID(), opinion, getLocation().stepDistance(e.getLocation()));
            return opinion;
        }
        return 0;
    }

    @Override
    public CombatEntity getNearestEnemy() {
        DebugWindow.reportf(DebugWindow.GAME, String.format("GameCharacter#%1$05d.getNearestEnemy", getUniqueID()), "BEGIN EVAL");
        GameCharacter newTarget = null;
        int minDistance = Integer.MAX_VALUE;
        byte maxHate = -3;
        ArrayList<Entity> entities = gi.getCurrentLevel().getEntities();
        for (Entity e : entities){
            int dist = e.getLocation().stepDistance(getLocation());
            byte opinion = getOpinion(e);
            if (isWithinDetectRange(e.getLocation(), detectRange) && e instanceof GameCharacter && opinion <= maxHate && dist > 0 && (dist <= minDistance || opinion < maxHate)) {
                GameCharacter gc = (GameCharacter) e;
                minDistance = dist;
                maxHate = opinion;
                DebugWindow.reportf(DebugWindow.GAME, String.format("GameCharacter#%1$05d.getNearestEnemy", getUniqueID()), "New Target: dist = %1$d, opinion = %2$d", minDistance, maxHate);
                newTarget = gc;
            }
        }
        return newTarget;
    }

    @Override
    protected void alertNearbyAllies() {
        ArrayList<Entity> entities = gi.getCurrentLevel().getEntities();
        for (Entity e : entities){
            int dist = e.getLocation().stepDistance(getLocation());
            if (e instanceof GameCharacter && dist <= alertRadius && isAlly(e)) {
                GameCharacter gc = (GameCharacter) e;
                gc.setTarget(target);
            }
        }
    }

    @Override
    public void onTurn() {
        super.onTurn();
        reportTarget();
    }

    private void reportTarget(){
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
    public void onReceiveDamage(int amount, TagHolder source, GameInstance gi) {
        if (source instanceof Projectile) {
            Projectile projectile = (Projectile) source;
            if (projectile.getSource() instanceof GameCharacter) {
                recentDamageDealer = (GameCharacter) projectile.getSource();
            }
        } else if (source instanceof GameCharacter) {
            recentDamageDealer = (GameCharacter) source;
        }
        super.onReceiveDamage(amount, source, gi);
    }

    @Override
    public void selfDestruct() {
        super.selfDestruct();
        if (isAutonomous && recentDamageDealer instanceof Player)
            dislikePlayer();
    }

    private void dislikePlayer(){
        for (String factionName : factionAlignments){
            FactionManager.Faction faction = gi.getFactionManager().getFaction(factionName);
            if (faction != null) {
                faction.addOpinion("player", -2);
            }
        }
    }

    @Override
    protected void updateSprite() {
        if (gi.getFactionManager().getOpinion(this, gi.getPlayer()) < 0 || (target != null && target.equals(gi.getPlayer())))
            redifyIcon();
        else
            setIcon(originalSprite);
        super.updateSprite();
    }

    @Override
    public void setTarget(CombatEntity target) {
        super.setTarget(target);
        updateSprite();
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

    private DialogueParser dialogueParser;

    @Override
    public void onInteract(Player player) {
        /*
        if (getOpinion(player) >= 0 && interactText.size() > 0){
            gi.getTextBox().showMessage(interactText.get(interactTextPointer), getName());
            interactTextPointer = Math.min(interactText.size() - 1, interactTextPointer + 1); //Increments the pointer, but loops at the maximum value
            DebugWindow.reportf(DebugWindow.MISC, "GameCharacter.onInteract", "Raw Interaction Text: %1$s", rawInteractText);
        }
        */
        dialogueParser.startParser(this);
    }
}

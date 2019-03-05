package Game.LevelScripts;

import Data.Coordinate;
import Data.EntityStruct;
import Data.LayerImportances;
import Data.SerializationVersion;
import Engine.Layer;
import Engine.SpecialText;
import Game.DialogueParser;
import Game.Entities.CombatEntity;
import Game.Entities.Entity;
import Game.Entities.GameCharacter;
import Game.QuickMenu;
import Game.Registries.TagRegistry;

import java.util.Random;

public class CinemaBanditFortress extends CinematicLevelScript {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    private Layer beesLayer;

    private String getBanditKingName(){
        return "Bandit King";
    }

    private GameCharacter getBanditKing(){
        return (GameCharacter)getFirstEntityofName(getBanditKingName());
}

    private Entity getBeehive(){
        for (Entity e : level.getEntities())
            if (e.getName().equals("Beehive"))
                return e;
        return null;
    }

    @Override
    public String[] getMaskNames() {
        return new String[]{"KingGreeting"};
    }

    @Override
    public void onLevelLoad() {
        if (gi.eventHappened("BanditsJoined")){
            getBanditKing().setPos(new Coordinate(223, 26));
        }
    }

    @Override
    public void onLevelEnter() {
        if (beesLayer == null)
            beesLayer = new Layer(level.getWidth(), level.getHeight(), "beesLayer", 0, 0, LayerImportances.VFX);
        gi.getLayerManager().addLayer(beesLayer);
    }

    @Override
    public void onLevelExit() {
        gi.getLayerManager().removeLayer(beesLayer);
    }

    @Override
    public void onRemoveEntity(Entity e) {
        if (e.getName().equals(getBanditKingName())){
            gi.recordEvent("BanditKingKilled");
        }
    }

    private boolean greetingHappened = false;

    @Override
    public void onTurnEnd() {
        if (!greetingHappened && getMaskDataAt("KingGreeting", gi.getPlayer().getLocation())){
            DialogueParser parser = new DialogueParser(gi, "#0#\"Ay, you that new kid my grunts keep talkin' about? You don't look tough, what gives?\"{I'll let the results speak for themselves.=1|I'm a wizard. I don't need muscles to get the job done.=2}#1#\"We Bandits got a brand to uphold, and your looks ain't helpin'. That bein' said, you could be exactly what we need.<np>We'll talk further in the War Room.\"<9>#2#\"A wizard? You gotta be joking. They're all stuck up in the Magic Academy casting curses everywhere, what business do they have sendin' you over?\"{The only person sending me here is myself.=4|I assure you, the past is behind me.=5}#4#\"Let me make this clear to you: if you're a Bandit, it's just us now. You can't be leaking our hidden treasure to any outsiders.<np>Meet me in the War Room. I've got a job for you.\"<9>#9#!trigger|moveToWarRoom!;");
            parser.startParser(getBanditKing());
            greetingHappened = true;
        }
    }

    @Override
    public boolean onPlayerInteract(Coordinate interactLoc) {
        Entity beehive = getBeehive();
        if (beehive != null && interactLoc.equals(beehive.getLocation())) {
            QuickMenu qm = gi.getQuickMenu();
            qm.clearMenu();
            qm.addMenuItem("Yes", () -> {
                Thread animThread = new Thread(this::performBeehiveFallingAnimation);
                animThread.start();
            });
            qm.addMenuItem("No", () -> {});
            qm.showMenu("Kick Beehive?", false);
            return true;
        }
        return false;
    }

    private void performBeehiveFallingAnimation(){
        Entity beehive = getBeehive();
        if (beehive != null) {
            gi.setCameraLocked(false);
            for (int i = 0; i < 20; i++) {
                beehive.move(0, 1);
                if (i > 4) gi.getLayerManager().moveCameraPos(0, 1);
                sleep(100);
            }
            beehive.selfDestruct();
            growBeehiveCircle(beehive.getLocation());
            moveBees(beehive.getLocation().copy());
            sleep(500);
            gi.setCameraLocked(true);
        }
    }

    private void growBeehiveCircle(Coordinate center){
        for (int i = 0; i < 10; i++) {
            drawBees(center, (i / 2) + 1, 0.9f - (i * 0.05f));
            sleep(100);
        }
    }

    private void moveBees(Coordinate origin){
        CombatEntity pathfinder = new CombatEntity(); //We need the bare minimums to get this CombatEntity to do its pathfinding without NullPointerExceptions.
        pathfinder.simpleInit(new EntityStruct(-1, "BEES!", new SpecialText(' ')), origin);
        pathfinder.setSprite(new Layer(1, 1, "", 0, 0, 0));
        pathfinder.setGameInstance(gi);
        //Find nearest CombatEntity (note: the one instantiated above has not been placed into the level)
        CombatEntity target = null;
        double lowestDistance = 20;
        for (Entity e : gi.getCurrentLevel().getEntities()){
            if (e instanceof CombatEntity) {
                CombatEntity ce = (CombatEntity) e;
                double dist = ce.getLocation().hypDistance(origin);
                if (dist < lowestDistance)
                    target = ce;
            }
        }
        //Move the bees to the target
        if (target != null) {
            while (!pathfinder.getLocation().equals(target.getLocation())){
                Coordinate newPos = pathfinder.getNextPathingPosition(target.getLocation(), Integer.MAX_VALUE).get(0);
                Coordinate relativePos = newPos.subtract(pathfinder.getLocation());
                gi.getLayerManager().moveCameraPos(relativePos.getX(), relativePos.getY());
                if (gi.getLayerManager().getCameraPos().getY() < -1) gi.getLayerManager().setCameraPos(gi.getLayerManager().getCameraPos().getX(), -1);
                pathfinder.setPos(newPos);
                drawBees(pathfinder.getLocation(), 5, 0.4f);
                sleep(100);
            }
            beesLayer.editLayer(4, 4, ' ');
            target.receiveDamage(9001);
            if (target.getName().equals(getBanditKingName())) gi.recordEvent("BanditKingStungToDeath");
        } else {
            for (int i = 0; i < 20; i++) {
                drawBees(pathfinder.getLocation(), 5, 0.4f / i);
                sleep(100);
            }
        }
        beesLayer.clearLayer();
    }

    private final int[][] circleMatrix = {
            {9, 9, 4, 4, 4, 9, 9},
            {9, 4, 3, 3, 3, 4, 9},
            {4, 3, 3, 2, 3, 3, 4},
            {4, 3, 2, 1, 2, 3, 4},
            {4, 3, 3, 2, 3, 3, 4},
            {9, 4, 3, 3, 3, 4, 9},
            {9, 9, 4, 4, 4, 9, 9},
    };

    private void drawBees(Coordinate center, int radius, float density){
        beesLayer.clearLayer();
        Coordinate bias = new Coordinate(-3, -3);
        Random random = new Random();
        for (int col = 0; col < circleMatrix.length; col++) {
            for (int row = 0; row < circleMatrix[0].length; row++) {
                Coordinate matrixPos = new Coordinate(col, row);
                Coordinate levelPos = center.add(matrixPos).add(bias);
                if (!gi.getCurrentLevel().checkTileTag(levelPos, TagRegistry.TILE_WALL, false) && radius >= circleMatrix[matrixPos.getX()][matrixPos.getY()] && density >= random.nextFloat()) {
                    beesLayer.editLayer(levelPos, new SpecialText('.'));
                }
            }
        }
    }
}
package Game.Spells;

import Data.Coordinate;
import Data.LayerImportances;
import Data.SerializationVersion;
import Engine.Layer;
import Engine.SpecialText;
import Game.Debug.DebugWindow;
import Game.Entities.Entity;
import Game.GameInstance;
import Game.Registries.TagRegistry;
import Game.Tile;

import java.awt.*;

public class SandWallSpell extends Spell {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    @Override
    public String getName() {
        return "Petramansy";
    }

    @Override
    public Color getColor() {
        return new Color(255, 255, 181);
    }

    private Coordinate spreadOrigin;
    private Layer preview;

    @Override
    public void readySpell(Coordinate targetLoc, Entity spellCaster, GameInstance gi, int magicPower) {
        spreadOrigin = targetLoc;
        preview = new Layer(gi.getCurrentLevel().getBackdrop().getCols(), gi.getCurrentLevel().getBackdrop().getRows(), "sandwallpreview:" + spellCaster.getUniqueID(), 0, 0, LayerImportances.ANIMATION);
        gi.getLayerManager().addLayer(preview);
    }

    @Override
    public void spellDrag(Coordinate targetLoc, Entity spellCaster, GameInstance gi, int magicPower) {
        drawPreview(targetLoc, gi, magicPower);
    }

    @Override
    public int castSpell(Coordinate targetLoc, Entity spellCaster, GameInstance gi, int magicPower) {
        gi.getLayerManager().removeLayer(preview);
        if (!isSandyAt(spreadOrigin, gi) || targetLoc.equals(spreadOrigin)) return 0;
        createWall(spreadOrigin, gi);
        spreadFromOrigin(targetLoc, gi, magicPower, pos -> createWall(pos, gi), true);
        return calculateCooldown(22, magicPower);
    }

    private void spreadFromOrigin(Coordinate target, GameInstance gi, int magicPower, SpreadAction spreadAction, boolean beSlow){
        //Calculate angle, borrowed from CombatEntity's calculateMeleeDirection
        int dy = spreadOrigin.getY() - target.getY(); //Need y coordinate to be in terms of a mathematical xy-plane, so its value is reversed.
        int dx = target.getX() - spreadOrigin.getX();
        if (dy == 0 && dx == 0) return;
        double angle = (180 / Math.PI) * Math.atan2(dy, dx);
        if (angle < 0) angle += 180;
        //Use angle to pick direction
        int range = 4 + (magicPower / 16);
        if (angle <= 22.5){ //E & W
            doSpread(spreadOrigin, gi, new Coordinate(1, 0), new Coordinate(-1, 0), range, spreadAction, beSlow);
        } else if (angle <= 67.5){ //NE & SW
            doSpread(spreadOrigin, gi, new Coordinate(-1, 1), new Coordinate(1, -1), range, spreadAction, beSlow);
        } else if (angle <= 112.5){ //N & S
            doSpread(spreadOrigin, gi, new Coordinate(0, 1), new Coordinate(0, -1), range, spreadAction, beSlow);
        } else if (angle <= 157.5){//NW & SE
            doSpread(spreadOrigin, gi, new Coordinate(1, 1), new Coordinate(-1, -1), range, spreadAction, beSlow);
        } else { //W & E
            doSpread(spreadOrigin, gi, new Coordinate(1, 0), new Coordinate(-1, 0), range, spreadAction, beSlow);
        }
    }

    //Does the actual spreading action from the origin
    private void doSpread(Coordinate origin, GameInstance gi, Coordinate vector1, Coordinate vector2, int range, SpreadAction spreadAction, boolean beSlow){
        Coordinate testLoc1 = origin.copy();
        Coordinate testLoc2 = origin.copy();
        boolean dir1Ended = false;
        boolean dir2Ended = false;
        int cycle = 0;
        long startTime = System.nanoTime();
        do {
            //Direction one
            if (isSandyAt(testLoc1.add(vector1), gi)) {
                testLoc1.movePos(vector1.getX(), vector1.getY());
                spreadAction.atLoc(testLoc1.copy());
            } else
                dir1Ended = true;
            //Direction two
            if (isSandyAt(testLoc2.add(vector2), gi)) {
                testLoc2.movePos(vector2.getX(), vector2.getY());
                spreadAction.atLoc(testLoc2.copy());
            } else
                dir2Ended = true;
            if (beSlow) {
                //Wait a little for show
                try {
                    Thread.sleep(70);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            cycle++;
        } while ((!dir1Ended || !dir2Ended) && cycle < range);
        DebugWindow.reportf(DebugWindow.STAGE, "SandWallSpell.doSpread","time to complete: %1$.2fms", (double)(System.nanoTime() - startTime) / 1000000);
    }

    private boolean isSandyAt(Coordinate loc, GameInstance gi){
        Tile tile = gi.getTileAt(loc);
        return tile != null && (tile.hasTag(TagRegistry.SAND) || tile.hasTag(TagRegistry.ASH)) && gi.isSpaceAvailable(loc, TagRegistry.TILE_WALL);
    }

    private void createWall(Coordinate pos, GameInstance gi){
        Tile tile = gi.getTileAt(pos);
        if (tile.hasTag(TagRegistry.SAND)){
            createSandWall(pos, gi);
        } else if (tile.hasTag(TagRegistry.ASH)){
            createAshWall(pos, gi);
        }
    }

    private void createSandWall(Coordinate pos, GameInstance gi){
        Tile wallTile = new Tile(pos, "Sand Wall", gi.getCurrentLevel());
        int[] tags = { TagRegistry.NO_PATHING, TagRegistry.TILE_WALL, TagRegistry.SAND, TagRegistry.DIGGABLE};
        for (int id : tags) wallTile.addTag(id, wallTile);
        gi.getCurrentLevel().addOverlayTile(wallTile);
        gi.getCurrentLevel().getOverlayTileLayer().editLayer(wallTile.getLocation(), new SpecialText('^', new Color(166, 123, 94), new Color(110, 70, 20)));
    }

    private void createAshWall(Coordinate pos, GameInstance gi){
        Tile wallTile = new Tile(pos, "Ash Wall", gi.getCurrentLevel());
        int[] tags = { TagRegistry.NO_PATHING, TagRegistry.TILE_WALL, TagRegistry.ASH, TagRegistry.DIGGABLE};
        for (int id : tags) wallTile.addTag(id, wallTile);
        gi.getCurrentLevel().addOverlayTile(wallTile);
        gi.getCurrentLevel().getOverlayTileLayer().editLayer(wallTile.getLocation(), new SpecialText('^', new Color(140, 135, 135), new Color(102, 99, 94)));
    }

    @Override
    public Spell copy() {
        return new SandWallSpell();
    }

    private void drawPreview(Coordinate target, GameInstance gi, int magicPower){
        preview.clearLayer();
        preview.editLayer(spreadOrigin, new SpecialText(' ', Color.WHITE, new Color(153, 82, 46, 45)));
        spreadFromOrigin(target, gi, magicPower, pos -> preview.editLayer(pos, new SpecialText(' ', Color.WHITE, new Color(155, 120, 45, 45))), false);
    }

    private interface SpreadAction{
        void atLoc(Coordinate pos);
    }
}

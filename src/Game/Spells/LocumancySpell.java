package Game.Spells;

import Data.Coordinate;
import Data.LayerImportances;
import Data.SerializationVersion;
import Engine.Layer;
import Engine.SpecialText;
import Game.Entities.Entity;
import Game.GameInstance;
import Game.Registries.TagRegistry;
import Game.UI.InventoryPanel;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

public class LocumancySpell extends Spell {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    private transient ArrayList<Coordinate> validLocations;
    private transient ArrayList<Entity> toTransport;
    private transient Layer previewLayer;

    private static final int BASE_RANGE   = 5;
    private static final int BASE_COOLDOWN = 26;

    private static final double SCALAR_RANGE = 0.0625;

    @Override
    public String getName() {
        return "Locumancy";
    }

    @Override
    public Color getColor() {
        return new Color(200, 181, 255);
    }

    @Override
    public void readySpell(Coordinate targetLoc, Entity spellCaster, GameInstance gi, int magicPower) {
        validLocations = new ArrayList<>();
        int range = calculatePower(BASE_RANGE, magicPower, SCALAR_RANGE); //At 80 (max) magic power, range doubles.
        previewLayer = new Layer(range * 2 + 1, range * 2 + 1, "Locumancy_preview:" + spellCaster.getName(), targetLoc.getX() - range, targetLoc.getY() - range, LayerImportances.ANIMATION);
        ArrayList<Entity> atLoc = gi.getCurrentLevel().getEntitiesAt(targetLoc);
        toTransport = new ArrayList<>();
        for (Entity e : atLoc) if (!e.hasTag(TagRegistry.IMMOVABLE)) toTransport.add(e);
        if (toTransport.size() > 0) {
            //Draw the diamond-shape
            spreadPoints(targetLoc, gi, targetLoc, range);
            for (int i = 0; i < validLocations.size();) {
                spreadPoints(validLocations.get(i), gi, targetLoc, range);
                i++; //You laugh, but this gets IntelliJ to shut up about using a foreach loop, which won't work.
            }
            gi.getLayerManager().addLayer(previewLayer);
        }
    }

    private void spreadPoints(Coordinate seed, GameInstance gi, Coordinate startLoc, int range){
        attemptPoint(seed.add(new Coordinate(1, 0)), gi, startLoc, range);
        attemptPoint(seed.add(new Coordinate(0, 1)), gi, startLoc, range);
        attemptPoint(seed.add(new Coordinate(0, -1)), gi, startLoc, range);
        attemptPoint(seed.add(new Coordinate(-1, 0)), gi, startLoc, range);
    }

    private void attemptPoint(Coordinate loc, GameInstance gi, Coordinate startLoc, int range){
        if (gi.isSpaceAvailable(loc, TagRegistry.TILE_WALL) && loc.stepDistance(startLoc) <= range && !validLocations.contains(loc)){
            validLocations.add(loc);
            Coordinate layerLoc = loc.subtract(startLoc).add(new Coordinate(previewLayer.getCols(), previewLayer.getRows()).multiplyTruncated(0.5));
            if (gi.isSpaceAvailable(loc, TagRegistry.NO_PATHING))
                previewLayer.editLayer(layerLoc, new SpecialText(' ', Color.WHITE, new Color(157, 0, 255, 100)));
            else
                previewLayer.editLayer(layerLoc, new SpecialText(' ', Color.WHITE, new Color(187, 0, 57, 25)));
        }
    }

    @Override
    public int castSpell(Coordinate targetLoc, Entity spellCaster, GameInstance gi, int magicPower) {
        gi.getLayerManager().removeLayer(previewLayer);
        if (toTransport.size() < 1 || !validLocations.contains(targetLoc) || !gi.isSpaceAvailable(targetLoc, TagRegistry.NO_PATHING))
            return 0;
        playTeleportAnimation(toTransport.get(0).getLocation(), targetLoc, gi);
        for (Entity e : toTransport) {
            e.teleport(targetLoc);
        }
        return calculateCooldown(BASE_COOLDOWN, magicPower);
    }

    public void teleportEntity(Entity e, Coordinate pos){
        playTeleportAnimation(e.getLocation().copy(), pos, e.getGameInstance());
        e.teleport(pos);
    }

    private void playTeleportAnimation(Coordinate fromLoc, Coordinate targetLoc, GameInstance gi){
        Color[] colors = {
            new Color(157, 0, 255),
            new Color(172, 128, 255),
            new Color(219, 94, 255),
            new Color(86, 61, 161),
            new Color(168, 154, 216),
            new Color(145, 81, 241)
        };
        char[] chars = {'#', '@', '%', ' ', '^', '$', 'H', 'i', ' ', 't', 'h', 'e', 'r', 'E', '!'};
        Layer fromLayer = new Layer(1, 1, "Lcoumancy_from", fromLoc.getX(), fromLoc.getY(), LayerImportances.ANIMATION);
        Layer toLayer =   new Layer(1, 1, "Lcoumancy_to", targetLoc.getX(), targetLoc.getY(), LayerImportances.ANIMATION);
        gi.getLayerManager().addLayer(fromLayer);
        gi.getLayerManager().addLayer(toLayer);
        Random random = new Random();
        for (int i = 0; i < 6; i++){
            fromLayer.editLayer(0, 0, new SpecialText(chars[random.nextInt(chars.length)], colors[random.nextInt(colors.length)], colors[random.nextInt(colors.length)]));
            toLayer.editLayer(0, 0, new SpecialText(chars[random.nextInt(chars.length)], colors[random.nextInt(colors.length)], colors[random.nextInt(colors.length)]));
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        gi.getLayerManager().removeLayer(fromLayer);
        gi.getLayerManager().removeLayer(toLayer);
    }

    @Override
    public int getDescriptionHeight() {
        return 5;
    }

    @Override
    public Layer drawDescription(Layer baseLayer, int magicPower) {
        //Draw flavor text
        baseLayer.inscribeString("Relocates a target\nobject.\nUse via drag-&-drop", 1, 1, InventoryPanel.FONT_WHITE, true);
        baseLayer.inscribeString(" ~~~ ", 1, 4, InventoryPanel.FONT_GRAY);
        //Draw base stats
        baseLayer.inscribeString(String.format("Distance : %1$d", BASE_RANGE),   1, 5, InventoryPanel.FONT_WHITE);
        baseLayer.inscribeString(String.format("Cooldown : %1$d", BASE_COOLDOWN), 1, 6, InventoryPanel.FONT_WHITE);
        //Draw modifiers
        baseLayer.inscribeString(String.format("(+%1$d)", calculatePower(0, magicPower, SCALAR_RANGE)), 15, 5, InventoryPanel.FONT_BLUE);
        baseLayer.inscribeString(String.format("(-%1$d)", BASE_COOLDOWN - calculateCooldown(BASE_COOLDOWN, magicPower)),  15, 6, InventoryPanel.FONT_BLUE);
        return baseLayer;
    }

    @Override
    public Spell copy() {
        return new LocumancySpell();
    }
}

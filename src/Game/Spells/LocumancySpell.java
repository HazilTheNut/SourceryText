package Game.Spells;

import Data.Coordinate;
import Data.LayerImportances;
import Data.SerializationVersion;
import Engine.Layer;
import Engine.SpecialText;
import Game.Entities.Entity;
import Game.GameInstance;
import Game.Registries.TagRegistry;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

public class LocumancySpell extends Spell {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    private transient ArrayList<Coordinate> validLocations;
    private transient ArrayList<Entity> toTransport;
    private transient Layer previewLayer;

    @Override
    public String getName() {
        return "Locumancy";
    }

    @Override
    public void readySpell(Coordinate targetLoc, Entity spellCaster, GameInstance gi, int magicPower) {
        validLocations = new ArrayList<>();
        int range = 5 + (magicPower / 16); //At 80 (max) magic power, range doubles.
        previewLayer = new Layer(range * 2 + 1, range * 2 + 1, "Locumancy_preview", targetLoc.getX() - range, targetLoc.getY() - range, LayerImportances.ANIMATION);
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
        if (gi.isSpaceAvailable(loc, TagRegistry.NO_PATHING) && loc.stepDistance(startLoc) <= range && !validLocations.contains(loc)){
            validLocations.add(loc);
            Coordinate layerLoc = loc.subtract(startLoc).add(new Coordinate(previewLayer.getCols(), previewLayer.getRows()).multiply(0.5));
            previewLayer.editLayer(layerLoc, new SpecialText(' ', Color.WHITE, new Color(157, 0, 255, 100)));
        }
    }

    @Override
    public int castSpell(Coordinate targetLoc, Entity spellCaster, GameInstance gi, int magicPower) {
        gi.getLayerManager().removeLayer(previewLayer);
        if (toTransport.size() < 1 || !validLocations.contains(targetLoc))
            return 0;
        for (Entity e : toTransport) {
            playTeleportAnimation(targetLoc, gi);
            e.teleport(targetLoc);
        }
        return calculateCooldown(26, magicPower);
    }

    private void playTeleportAnimation(Coordinate targetLoc, GameInstance gi){
        Color[] colors = {
            new Color(157, 0, 255),
            new Color(172, 128, 255),
            new Color(219, 94, 255),
            new Color(86, 61, 161),
            new Color(168, 154, 216),
            new Color(145, 81, 241)
        };
        char[] chars = {'#', '@', '%', ' ', '^', '$', 'h', 'e', 'y', 's', 't', 'u', 'p', 'i', 'd', '!'};
        Layer fromLayer = new Layer(1, 1, "Lcoumancy_from", toTransport.get(0).getLocation().getX(),toTransport.get(0).getLocation().getY(), LayerImportances.ANIMATION);
        Layer toLayer =   new Layer(1, 1, "Lcoumancy_to", targetLoc.getX(),targetLoc.getY(), LayerImportances.ANIMATION);
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
}

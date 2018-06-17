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
        int range = 7 + (magicPower / 16);
        previewLayer = new Layer(range * 2 + 1, range * 2 + 1, "Locumancy_preview", targetLoc.getX() - range, targetLoc.getY() - range, LayerImportances.ANIMATION);
        ArrayList<Entity> atLoc = gi.getCurrentLevel().getEntitiesAt(targetLoc);
        toTransport = new ArrayList<>();
        for (Entity e : atLoc) if (!e.hasTag(TagRegistry.IMMOVABLE)) toTransport.add(e);
        if (toTransport.size() > 0) {
            //Draw the diamond-shape
            for (int x = -1 * range; x <= range; x++) {
                int height = range - Math.abs(x);
                for (int y = -1 * height; y <= height; y++) {
                    Coordinate localPos = new Coordinate(x, y);
                    if (gi.isSpaceAvailable(targetLoc.add(localPos),TagRegistry.NO_PATHING)) {
                        validLocations.add(targetLoc.add(localPos));
                        previewLayer.editLayer(new Coordinate(x + range, y + range), new SpecialText(' ', Color.WHITE, new Color(157, 0, 255, 100)));
                    }
                }
            }
            gi.getLayerManager().addLayer(previewLayer);
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

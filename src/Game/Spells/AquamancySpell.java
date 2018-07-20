package Game.Spells;

import Data.Coordinate;
import Data.LayerImportances;
import Data.SerializationVersion;
import Engine.Layer;
import Engine.LayerManager;
import Engine.SpecialText;
import Game.Entities.Entity;
import Game.GameInstance;
import Game.Registries.TagRegistry;

import java.awt.*;

public class AquamancySpell extends Spell {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    @Override
    public String getName() {
        return "Aquamancy";
    }

    @Override
    public Spell copy() {
        return new AquamancySpell();
    }

    @Override
    public int castSpell(Coordinate targetLoc, Entity spellCaster, GameInstance gi, int magicPower) {
        if (gi.getTileAt(targetLoc).hasTag(TagRegistry.WET) && gi.isSpaceAvailable(targetLoc, TagRegistry.NO_PATHING)) {
            playTeleportAnimation(targetLoc, gi.getLayerManager());
            spellCaster.teleport(targetLoc);
            return calculateCooldown(25, magicPower);
        }
        return 0;
    }

    private void playTeleportAnimation(Coordinate targetLoc, LayerManager lm){
        Layer animLayer = new Layer(1, 1, "aquamncy", targetLoc.getX(), targetLoc.getY(), LayerImportances.ANIMATION);
        Color fgColor = new Color(170, 210, 255);
        Color bgColor = new Color(61, 74, 176, 30);
        lm.addLayer(animLayer);
        while (lm.isDrawingFrame())
            sleep(20);
        char[] animSst = {'.','o','O'};
        for (char c : animSst) {
            animLayer.editLayer(0, 0, new SpecialText(c, fgColor, bgColor));
            sleep(75);
        }
        lm.removeLayer(animLayer);
    }

    private void sleep(long millis){
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

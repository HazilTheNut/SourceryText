package Game.Entities;

import Data.Coordinate;
import Data.EntityArg;
import Data.EntityStruct;
import Data.SerializationVersion;
import Engine.LayerManager;
import Engine.SpecialText;
import Game.Entities.PuzzleElements.Powerable;
import Game.GameInstance;
import Game.Projectile;
import Game.Registries.TagRegistry;
import Game.TagEvent;
import Game.Tags.Tag;

import java.awt.*;
import java.util.ArrayList;

public class Magnet extends Entity implements Powerable {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    private boolean isAttractive = true; //Insert joke about 'being attractive' having both the romantic and magnetic meaning.

    @Override
    public void initialize(Coordinate pos, LayerManager lm, EntityStruct entityStruct, GameInstance gameInstance) {
        super.initialize(pos, lm, entityStruct, gameInstance);
        MagneticTag magneticTag = new MagneticTag();
        magneticTag.setId(TagRegistry.MAGNETIC);
        magneticTag.setName("Magnetic");
        addTag(magneticTag, null);
        isAttractive = readBoolArg(searchForArg(entityStruct.getArgs(), "isAttractive"), true);
        updateIcon();
    }

    @Override
    public ArrayList<EntityArg> generateArgs() {
        ArrayList<EntityArg> args = super.generateArgs();
        args.add(new EntityArg("isAttractive", "true"));
        return args;
    }

    @Override
    public void onProjectileFly(Projectile projectile) {
        if (projectile.hasTag(TagRegistry.METALLIC)) {
            double magneticFactor = (isAttractive) ? 1 : -1; //Adjustment number for strength of pull
            double dx = projectile.getXpos() - getLocation().getX();
            double dy = projectile.getYpos() - getLocation().getY();
            double distanceFactor = (Math.pow(dx, 2) + Math.pow(dy, 2)); //Pretend as if it were square-rooted. It's just that it's going to be squared immediately afterward.
            double pullX = (magneticFactor * -dx) / (Math.pow(distanceFactor, 1.5)); //The math checks out
            double pullY = (magneticFactor * -dy) / (Math.pow(distanceFactor, 1.5));
            projectile.adjust(0, 0, pullX, pullY);
        }
    }

    private void updateIcon(){
        int hueAdjust = 100;
        if (isAttractive)
            getSprite().editLayer(0, 0, new SpecialText('M', new Color(150, 150, 150 + hueAdjust), new Color(100, 100, 100 + hueAdjust, 15)));
        else
            getSprite().editLayer(0, 0, new SpecialText('M', new Color(150 + hueAdjust, 150, 150), new Color(100 + hueAdjust, 100, 100, 15)));
    }

    private void toggle(){
        isAttractive = !isAttractive;
        updateIcon();
    }

    @Override
    public void onPowerOff() {
        toggle();
    }

    @Override
    public void onPowerOn() {
        toggle();
    }

    private class MagneticTag extends Tag{
        private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

        @Override
        public void onContact(TagEvent e) {
            if (e.getTarget().hasTag(TagRegistry.ELECTRIC)) {
                toggle();
            }
        }
    }
}

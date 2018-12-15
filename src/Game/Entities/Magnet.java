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
import Game.ProjectileListener;
import Game.Registries.TagRegistry;
import Game.TagEvent;
import Game.Tags.MagneticTag;
import Game.Tags.Tag;

import java.awt.*;
import java.util.ArrayList;

public class Magnet extends Entity implements Powerable {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    private MagneticTag magneticTag;

    @Override
    public void initialize(Coordinate pos, LayerManager lm, EntityStruct entityStruct, GameInstance gameInstance) {
        super.initialize(pos, lm, entityStruct, gameInstance);
        magneticTag = (MagneticTag)TagRegistry.getTag(TagRegistry.MAGNETIC);
        magneticTag.setAttractive(readBoolArg(searchForArg(entityStruct.getArgs(), "isAttractive"), true));
        addTag(magneticTag, this);
        updateSprite();
    }

    @Override
    public ArrayList<EntityArg> generateArgs() {
        ArrayList<EntityArg> args = super.generateArgs();
        args.add(new EntityArg("isAttractive", "true"));
        return args;
    }

    private void toggle(){
        magneticTag.toggle();
    }

    @Override
    public void onPowerOff() {
        toggle();
    }

    @Override
    public void onPowerOn() {
        toggle();
    }
}

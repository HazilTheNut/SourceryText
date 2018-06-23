package Game.Entities.PuzzleElements;

import Data.Coordinate;
import Data.EntityArg;
import Data.EntityStruct;
import Data.SerializationVersion;
import Engine.LayerManager;
import Engine.SpecialText;
import Game.Entities.Entity;
import Game.GameInstance;
import Game.Registries.TagRegistry;

import java.awt.*;
import java.util.ArrayList;

public class PoweredDoor extends Entity implements Powerable {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    private SpecialText closedIcon;
    private SpecialText openIcon;
    private boolean isSolid = true;

    private boolean defaultClosed = true;

    @Override
    public void initialize(Coordinate pos, LayerManager lm, EntityStruct entityStruct, GameInstance gameInstance) {
        super.initialize(pos, lm, entityStruct, gameInstance);
        closedIcon = entityStruct.getDisplayChar();
        openIcon = new SpecialText(' ', Color.WHITE, closedIcon.getBkgColor().darker());
        defaultClosed = readBoolArg(searchForArg(entityStruct.getArgs(), "defaultClosed"), true);
        onPowerOff();
    }

    @Override
    public ArrayList<EntityArg> generateArgs() {
        ArrayList<EntityArg> args = super.generateArgs();
        args.add(new EntityArg("defaultClosed", "true"));
        return args;
    }

    @Override
    public boolean isSolid() {
        return isSolid;
    }

    @Override
    public void onPowerOff() {
        //Return to default state.
        if (defaultClosed)
            close();
        else
            open();
    }

    @Override
    public void onPowerOn() {
        //Go to non-default state
        if (defaultClosed)
            open();
        else
            close();
    }

    private void open(){
        setIcon(openIcon);
        isSolid = false;
        removeTag(TagRegistry.NO_PATHING);
    }

    private void close(){
        setIcon(closedIcon);
        isSolid = true;
        addTag(TagRegistry.NO_PATHING, this);
    }
}

package Game.Entities.PuzzleElements;

import Data.Coordinate;
import Data.EntityArg;
import Data.EntityStruct;
import Data.SerializationVersion;
import Engine.LayerManager;
import Engine.SpecialText;
import Game.*;
import Game.Entities.Entity;
import Game.Registries.TagRegistry;
import Game.Tags.Tag;

import java.awt.*;
import java.util.ArrayList;

public class ToggleSwitch extends GenericPowerSource {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    private SpecialText iconOn  = new SpecialText('*', new Color(180, 160, 100), new Color(85, 85, 55));
    private SpecialText iconOff = new SpecialText('*', new Color(44,  44,  89),  new Color(10, 10, 26, 100));
    private boolean isOn = false;

    @Override
    public void initialize(Coordinate pos, LayerManager lm, EntityStruct entityStruct, GameInstance gameInstance) {
        super.initialize(pos, lm, entityStruct, gameInstance);
        if (readBoolArg(searchForArg(entityStruct.getArgs(), "isOn"), false))
            setToOn();
        else
            setToOff();

        TogglingTag togglingTag = new TogglingTag();
        togglingTag.setId(TagRegistry.TOGGLING);
        addTag(togglingTag, this);
    }

    @Override
    public ArrayList<EntityArg> generateArgs() {
        ArrayList<EntityArg> args = super.generateArgs();
        args.add(new EntityArg("isOn","false"));
        return args;
    }

    private void toggle(){
        if (isOn)
            setToOff();
        else
            setToOn();
    }

    private void setToOn(){
        isOn = true;
        powerOn();
        setIcon(iconOn);
        updateSprite();
        updateBrightTag(this);
    }

    public void setToOff(){
        isOn = false;
        powerOff();
        setIcon(iconOff);
        updateSprite();
        updateBrightTag(this);
    }

    public boolean isOn() {
        return isOn;
    }

    @Override
    public void onInteract(Player player) {
        toggle();
    }
    
    private void updateBrightTag(TagHolder tagHolder){
        if (isOn)
            tagHolder.addTag(TagRegistry.BRIGHT, tagHolder);
        else
            tagHolder.removeTag(TagRegistry.BRIGHT);
    }

    private class TogglingTag extends Tag {
        private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

        @Override
        public void onContact(TagEvent e) {
            e.addFutureAction(event -> {
                if (!(e.getTarget() instanceof Tile))
                    toggle();
            });
        }

        @Override
        public String getName() {
            return "Toggling";
        }
    }
}

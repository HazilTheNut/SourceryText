package Game.Entities.PuzzleElements;

import Data.Coordinate;
import Data.EntityArg;
import Data.EntityStruct;
import Data.SerializationVersion;
import Engine.LayerManager;
import Engine.SpecialText;
import Game.Entities.Entity;
import Game.GameInstance;
import Game.Player;
import Game.Registries.TagRegistry;
import Game.TagEvent;
import Game.TagHolder;
import Game.Tags.Tag;

import java.awt.*;
import java.util.ArrayList;

public class ToggleSwitch extends Entity {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    private SpecialText iconOn  = new SpecialText('*', new Color(180, 160, 100), new Color(85, 85, 55));
    private SpecialText iconOff = new SpecialText('*', new Color(44,  44,  89),  new Color(10, 10, 26, 100));
    private boolean isOn = false;

    private ArrayList<Coordinate> powerToLocs;

    @Override
    public void initialize(Coordinate pos, LayerManager lm, EntityStruct entityStruct, GameInstance gameInstance) {
        super.initialize(pos, lm, entityStruct, gameInstance);
        isOn = readBoolArg(searchForArg(entityStruct.getArgs(), "isOn"), false);
        powerToLocs = readCoordListArg(searchForArg(entityStruct.getArgs(), "powerTo"));

        TogglingTag togglingTag = new TogglingTag();
        togglingTag.setId(TagRegistry.TOGGLING);
        addTag(togglingTag, this);

        isOn = !isOn;
    }

    @Override
    public ArrayList<EntityArg> generateArgs() {
        ArrayList<EntityArg> args = super.generateArgs();
        args.add(new EntityArg("isOn","false"));
        args.add(new EntityArg("powerTo","[0,0],[0,0],..."));
        return args;
    }

    private void toggle(){
        isOn = !isOn;
        for (Coordinate pos : powerToLocs) {
            for (Entity e : gi.getCurrentLevel().getEntitiesAt(pos)){
                if (e instanceof Powerable) {
                    if (isOn)
                        ((Powerable) e).onPowerOn();
                    else
                        ((Powerable) e).onPowerOff();
                }
            }
        }
        if (isOn){
            setIcon(iconOn);
            updateSprite();
        } else {
            setIcon(iconOff);
            updateSprite();
        }
    }

    @Override
    public void onInteract(Player player) {
        toggle();
        updateBrightTag(this);
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
            toggle();
            e.addFutureAction(event -> updateBrightTag(e.getSource()));
        }

        @Override
        public String getName() {
            return "Toggling";
        }
    }
}

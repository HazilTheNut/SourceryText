package Game.Entities;

import Data.Coordinate;
import Data.EntityArg;
import Data.EntityStruct;
import Data.SerializationVersion;
import Engine.LayerManager;
import Game.GameInstance;
import Game.Player;
import Game.Registries.TagRegistry;
import Game.TagHolder;
import Game.Tags.Tag;

import java.util.ArrayList;

/**
 * Created by Jared on 5/11/2018.
 */
public class Sign extends Entity {

    /**
     * Sign:
     *
     * Upon interaction, it sends a message to the GameInstance's TextBox, which of course then displays it to the player.
     */

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    private String text;
    private int fireLifetime = -1;

    @Override
    public ArrayList<EntityArg> generateArgs() {
        ArrayList<EntityArg> args = super.generateArgs();
        args.add(new EntityArg("text", ""));
        args.add(new EntityArg("flammable", "true"));
        return args;
    }

    @Override
    public void onInteract(Player player) {
        if (!text.equals("")){
            gi.getTextBox().showMessage(text, getName());
        }
    }

    @Override
    public void initialize(Coordinate pos, LayerManager lm, EntityStruct entityStruct, GameInstance gameInstance) {
        super.initialize(pos, lm, entityStruct, gameInstance);
        text = readStrArg(searchForArg(entityStruct.getArgs(), "text"), "");
        if (!readBoolArg(searchForArg(entityStruct.getArgs(), "flammable"), true)){
            removeTag(TagRegistry.FLAMMABLE);
        }
    }

    @Override
    public void addTag(Tag tag, TagHolder source) {
        super.addTag(tag, source);
        if (tag.getId() == TagRegistry.ON_FIRE){
            fireLifetime = 3;
        }
    }

    @Override
    public void onTurn() {
        if (fireLifetime > 0)
            fireLifetime--;
        if (fireLifetime == 0)
            selfDestruct();
    }
}

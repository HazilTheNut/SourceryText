package Game.Entities;

import Data.Coordinate;
import Data.EntityStruct;
import Data.SerializationVersion;
import Engine.LayerManager;
import Engine.SpecialText;
import Game.Explosion;
import Game.GameInstance;
import Game.Registries.TagRegistry;
import Game.TagEvent;
import Game.TagHolder;
import Game.Tags.Tag;

import java.awt.*;
import java.util.ArrayList;

public class MagicBomb extends CombatEntity {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    private int timer;
    private Color fgColor = new Color(75, 92, 189, 125);
    private Color bgColor = new Color(38, 55, 153, 50);

    private int explosionDamage;

    private ArrayList<Integer> startingTagIds;

    @Override
    public void initialize(Coordinate pos, LayerManager lm, EntityStruct entityStruct, GameInstance gameInstance) {
        super.initialize(pos, lm, entityStruct, gameInstance);
        setMaxHealth(20);
        timer = 9;
        indicateTimer();
        startingTagIds = new ArrayList<>();
        for (Tag tag : getTags())
            startingTagIds.add(tag.getId());
    }

    public void setExplosionDamage(int explosionDamage) {
        this.explosionDamage = explosionDamage;
    }

    @Override
    public void onTurn() {
        super.onTurn();
        timer--;
        indicateTimer();
        if (timer < 1) selfDestruct();
    }

    private void indicateTimer(){
        String timerText = String.valueOf(timer);
        setIcon(new SpecialText(timerText.charAt(timerText.length()-1), fgColor, bgColor));
        updateSprite();
    }

    @Override
    public void selfDestruct() {
        super.selfDestruct();
        explode();
    }

    private void explode(){
        Explosion explosion = new Explosion();
        explosion.addTag(TagRegistry.DIGGING, explosion);
        explosion.addToTransmissionBlacklist(TagRegistry.DIGGING);
        for (Tag tag : getTags()) {
            if (!startingTagIds.contains(tag.getId()))
                explosion.addTag(tag, this);
        }
        explosion.explode(explosionDamage, getLocation().copy(), getGameInstance(), getSprite().getSpecialText(0,0).getFgColor());
    }

    @Override
    public void addTag(Tag tag, TagHolder source) {
        if (!hasTag(tag.getId())) {
            getTags().add(tag);
            TagEvent e = new TagEvent(0, true, source, this, null);
            for (int i = 0; i < getTags().size(); i++){
                getTags().get(i).onAdd(e);
            }
            e.doFutureActions();
            if (hasTag(tag.getId())) { //If the tag hasn't already been removed
                tag.onAddThis(e);
                if (e.eventPassed()) {
                    e.doCancelableActions();
                }
                //Cancelling the event will not remove the tag. Stuff like flammability shouldn't matter in this context.
            }
        }
    }
}

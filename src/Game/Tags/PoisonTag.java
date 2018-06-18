package Game.Tags;

import Data.SerializationVersion;
import Game.Item;
import Game.Registries.TagRegistry;
import Game.TagEvent;
import Game.TagHolder;

import java.awt.*;

public class PoisonTag extends Tag{

    /**
     * PoisonTag:
     *
     * The Tag that handles poison.
     *
     * For All TagHolders:
     * > Has a turn timer. When the timer is finished, this tag removes itself.
     *
     * For Items:
     * > Transmits poison upon damaging, only if the Item also has a SharpTag.
     *
     * For All but Items:
     * > Deals 2 damage to owner of tag every turn.
     */

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    public static final int LIFETIME_START = 11;
    private int lifetime;

    @Override
    public void onAddThis(TagEvent e) {
        super.onAddThis(e);
        if (lifetime == 0) lifetime = LIFETIME_START;
    }

    @Override
    public void onTurn(TagEvent e) {
        if (!(e.getSource() instanceof Item))
            e.getSource().onReceiveDamage(2, e.getSource(), e.getGameInstance());
        lifetime--;
        if (lifetime < 1) e.addFutureAction(event -> e.getSource().removeTag(TagRegistry.POISON));
    }

    @Override
    public void onDealDamage(TagEvent e) {
        if (e.getSource() instanceof Item) {
            Item source = (Item) e.getSource();
            if (source.hasTag(TagRegistry.SHARP)){ //Gotta puncture the target to poison it.
                transmit(e.getTarget());
            }
        }
    }

    @Override
    public void onContact(TagEvent e) {
        if (e.getTarget() instanceof Item) {
            Item target = (Item) e.getTarget();
            if (target.hasTag(TagRegistry.SHARP)){
                transmit(target);
            }
        }
    }

    void transmit(TagHolder target){
        PoisonTag poisonTag = (PoisonTag)target.getTag(TagRegistry.POISON);
        if (poisonTag != null)
            poisonTag.lifetime = Math.max(poisonTag.lifetime, lifetime);
        else {
            PoisonTag toAdd = (PoisonTag)TagRegistry.getTag(TagRegistry.POISON);
            toAdd.lifetime = lifetime;
            target.addTag(toAdd, null);
        }
    }

    @Override
    public String getName() {
        return String.format("Poison (%1$d)", lifetime);
    }

    @Override
    public Color getTagColor() {
        return new Color(179, 219, 87);
    }
}

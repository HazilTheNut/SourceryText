package Game.Tags;

import Data.SerializationVersion;
import Game.TagEvent;
import Game.TagHolder;

import java.awt.*;
import java.io.Serializable;

/**
 * Created by Jared on 3/26/2018.
 */
public class Tag implements Serializable {

    /**
     * Tag:
     *
     * The Backdrop, Tiles, Entities, Warp Zones, and Level Scripts are all essential to the Sourcery Text Level,
     * but the Tag is the central foundation of Sourcery Text itself.
     *
     * The Tag, and its child classes, governs and adds formality the interactions between Entities, Tiles, Items, and Projectiles.
     *
     * Tags are powerful, partly because they are transferable. For example, the OnFireTag can be added to any TagHolder and still work properly.
     * So that means very general rules can be created with very easily. For example, the OnFireTag has a rule where if two TagHolders contact each other, fire will spread to the other.
     *
     * These generalities encourage developing universal rules.
     *
     * Coupled with this is the Tag's modularity.
     *
     * Each Tag can be viewed as a singular "behavior" being added to a TagHolder. Any combination of Tags can be added to a TagHolder.
     * This has some great benefits. For example, the ItemRegistry leverages this property to create a registry of Items that can be wildly different from each other.
     * Additionally, this makes describing the properties of any TagHolder very easy to do, as you can just form a list of the Tags is has and get a good description of how it works.
     *
     * Altogether, the Tag allows for the creation of rules that make sense and add a sense of simulation to Sourcery Text.
     */

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    private String name;
    private int id;

    public int getId() { return id; }

    public String getName() { return name; }

    public void setId(int id) { this.id = id; }

    public void setName(String name) { this.name = name; }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Tag) {
            Tag tag = (Tag) obj;
            return tag.getId() == getId() && tag.getName().equals(getName());
        }
        return false;
    }

    // E V E N T S

    //Ran when the player enters a level. Cancellation does nothing. GameInstance is null for Tiles.
    public void onLevelEnter(TagEvent e){}

    //Upon when the TagHolder Item is 'used'
    public void onItemUse(TagEvent e) {}

    //Upon dealing damage, useful for calculating the total damage of an attack
    public void onDealDamage(TagEvent e) {}

    //Ran every turn
    public void onTurn(TagEvent e){}

    //Ran whenever two TagHolders come in physical contact with each other
    public void onContact(TagEvent e){}

    //Ran on every iteration of projectile over tiles
    public void onFlyOver(TagEvent e){}

    //Ran on every movement of owner Entity
    public void onMove(TagEvent e){}

    //Ran when en Entity tries do an action that 'takes up a turn'. Target is null
    public void onEntityAction(TagEvent e){}

    //Ran when the tag holder containing this tag receives another tag. GameInstance is null
    public void onAdd(TagEvent e){}

    //Ran when this tag is being added to a tag holder, happens after onAdd() and all future actions. GameInstance is null
    public void onAddThis(TagEvent e){}

    //Ran when this tag is removed. Cannot be canceled
    public void onRemove(TagHolder owner){}

    //Useful for coloring things that are frozen, on fire, etc.
    public Color getTagColor(){ return new Color(255, 255, 255, 0); }
}

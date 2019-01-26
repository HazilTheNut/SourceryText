package Game.Tags;

import Data.SerializationVersion;
import Game.Entities.Entity;
import Game.Registries.TagRegistry;
import Game.TagEvent;
import Game.TagHolder;
import Game.Tile;

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
     * but without doubt the Tag is the central foundation of Sourcery Text game itself.
     *
     * The Tag, and its child classes, governs and adds formality to the interactions between Entities, Tiles, Items, Projectiles, and Explosions.
     *
     * Tags are powerful, partly because they are transferable. For example, the OnFireTag can be added to any TagHolder and still work properly.
     * So that means very general rules can be created with very easily. For example, the OnFireTag has a rule where if two TagHolders contact each other, fire will spread to the other.
     *
     * These generalities encourage developing universal rules, and thus a sense of world simulation.
     *
     * Coupled with this is the Tag's modularity.
     *
     * Each Tag can be viewed as a singular "behavior" being added to a TagHolder. Any combination of Tags can be added to a TagHolder.
     * This has some great benefits. For example, the ItemRegistry leverages this property to create a registry of Items that can be wildly different from each other.
     * Additionally, this makes describing the properties of any TagHolder very easy to do, as you can just form a list of the Tags is has and get a good description of how it works.
     */

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    private String name;
    private int id;

    public static final int TYPE_STANDARD = 0;
    public static final int TYPE_ENCHANTMENT = 1;
    public static final int TYPE_POTIONEFFECT = 2;

    public int getTagType(){
        return TYPE_STANDARD;
    }

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

    //For reference, "this" means the owner of this tag. All TagEvents have a tagOwner field that should be used for lack of ambiguity.

    //Ran when the player enters a level. Cancellation does nothing.
    //Source: this
    //Target: this
    public void onLevelEnter(TagEvent e){}

    //Upon when the TagHolder Item is 'used'
    //Source: the item being used
    //Target: this
    public void onItemUse(TagEvent e) {}

    //Upon dealing damage, useful for calculating the total damage of an attack
    //Source: dealer of damage, either Entity or Projectile
    //Target: receiving damage
    public void onDealDamage(TagEvent e) {}

    //Upon receiving damage, useful for calculating the total damage of an attack
    //Source: damage dealer
    //Target: damage receiver
    public void onReceiveDamage(TagEvent e){}

    //Upon an enemy swinging a weapon, useful for causing attacks to miss. Amount is % chance of success
    //Source: this
    //Target: held weapon
    public void onWeaponSwing(TagEvent e){}

    //Ran every turn
    //Source: this
    //Target: this. If this is a Tile, then it is instead the solid entity atop this Tile (can be null)
    public void onTurn(TagEvent e){}

    //Ran whenever two TagHolders come in physical contact with each other
    //Source: this
    //Target: other TagHolder being contacted
    public void onContact(TagEvent e){}

    //Ran on every iteration of projectile over tiles
    //Source: this
    //Target: other TagHolder 'below', almost always a Tile
    public void onFlyOver(TagEvent e){}

    //Ran on every movement of owner Entity
    //Source: this
    //Target: this
    public void onMove(TagEvent e){}

    //Ran when en Entity tries do an action that 'takes up a turn'. Cancel to prevent the action from happening.
    //Source: this
    //Target: null
    public void onEntityAction(TagEvent e){}

    //Ran when the tag holder containing this tag receives another tag. GameInstance is null. When this event is ran, the newly added tag is already in the tag list of the tagOwner.
    //Source: source of tag addition
    //Target: this
    public void onAdd(TagEvent e){}

    //Ran when this tag is being added to a tag holder, happens after onAdd() and all future actions. GameInstance is null
    //Source: source of tag addition
    //Target: this
    public void onAddThis(TagEvent e){}

    //Ran when this tag is removed. Tag removal cannot be canceled
    public void onRemove(TagHolder owner){}

    //Ran when the owner of this is destroyed. Entity destruction cannot be canceled
    public void onEntityDestruct(Entity owner){}

    //Ran to figure if the owning Tile can be cleaned up by the level.
    public boolean isTileRemovable(Tile tile){ return true; }

    //Useful for coloring things that are frozen, on fire, etc.
    public Color getTagColor(){ return new Color(255, 255, 255, 0); }

    //Special case for coloring tiles.
    public Color getTagTileColor(){ return new Color(0, 0, 0, 0); }

    //This should be used when copying lists of strings
    public Tag copy(){
        return TagRegistry.getTag(getId());
    }
}

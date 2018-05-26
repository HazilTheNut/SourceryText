package Game.Tags;

import Data.SerializationVersion;
import Game.TagEvent;

import java.awt.*;
import java.io.Serializable;

/**
 * Created by Jared on 3/26/2018.
 */
public class Tag implements Serializable {

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

    //Ran when the player enters a level. Cancellation does nothing.
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

    //Ran when the tag holder containing this tag receives a new tag. GameInstance is null
    public void onAdd(TagEvent e){}

    //Ran when this tag is being added to a tag holder. GameInstance is null
    public void onAddThis(TagEvent e){}

    //Useful for coloring things that are frozen, on fire, etc.
    public Color getTagColor(){ return new Color(255, 255, 255, 0); }
}

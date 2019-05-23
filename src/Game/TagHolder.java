package Game;

import Data.SerializationVersion;
import Game.Debug.DebugWindow;
import Game.Registries.TagRegistry;
import Game.Tags.DamageTag;
import Game.Tags.HealthTag;
import Game.Tags.Tag;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Jared on 3/31/2018.
 */
public class TagHolder implements Serializable {

    /**
     * TagHolder:
     *
     * The fundamental 'object' in SourceryText. As the name suggests, it contains a list of Tags.
     */

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    private ArrayList<Tag> tags = new ArrayList<>();

    public boolean tagsVisible() { return true; }

    public ArrayList<Tag> getTags() { return tags; }

    public void setTags(ArrayList<Tag> tags) {
        this.tags = tags;
    }

    public void addTag(int tagID, TagHolder source){
        Tag tag = TagRegistry.getTag(tagID);
        if (tag != null) {
            addTag(tag, source);
        }
    }

    public void addTag(Tag tag, TagHolder source) {
        if (tag == null)
            return;
        if (!hasTag(tag.getId())) {
            tags.add(tag);
            TagEvent e = new TagEvent(0, source, this, null, this);
            for (int i = 0; i < tags.size(); i++){
                tags.get(i).onAdd(e);
            }
            e.doFutureActions();
            if (hasTag(tag.getId())) { //If the tag hasn't already been removed
                tag.onAddThis(e);
                if (e.eventPassed()) {
                    e.doCancelableActions();
                } else {
                    removeTag(tag.getId());
                }
            }
        }
    }

    public void removeTag (int id) {
        for (Tag tag : tags){
            if (tag.getId() == id) {
                tags.remove(tag);
                tag.onRemove(this);
                return;
            }
        }
    }

    public Tag getTag(int id){
        for (Tag tag : getTags()){
            if (tag.getId() == id) return tag;
        }
        return null;
    }

    /**
     * Receives an array of strings that each describe how to modify the pre-existing tags of this TagHolder
     * Each string is expected to represent a number:
     *  If the number is positive, this TagHolder will add a tag whose ID is the number provided.
     *  If the number is negative, this TagHolder will remove a tag whose ID is the absolute value of the number provided.
     *
     * @param adjustments The ArrayList of Strings to parse
     */
    public void interpretTagAdjustments(ArrayList<String> adjustments){
        for (String str : adjustments) {
            try {
                int tagId = Math.abs(Integer.valueOf(str));
                if (str.charAt(0) == '-') // "-0" should indicate removing the flammable tag
                    removeTag(tagId);
                else
                    addTag(tagId, this);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
    }

    public void onLevelEnter(GameInstance gi){
        TagEvent event = new TagEvent(0, this, this, gi, this);
        for (Tag tag : getTags()) {
            tag.onLevelEnter(event);
        }
        event.doFutureActions();
    }

    /**
     * A convenience function that saves you the trouble of casting the damage Tag to a DamageTag and handling null tags.
     * @return The damage value of the Tag this TagHolder might have. Returns 0 if damage tag does not exist.
     */
    public int getDamageTagAmount(){
        Tag dmgTag = getTag(TagRegistry.DAMAGE_START);
        if (dmgTag instanceof DamageTag){
            return ((DamageTag)dmgTag).getDamageAmount();
        }
        return 0;
    }

    /**
     * A convenience function that saves you the trouble of casting the damage Tag to a HealthTag and handling null tags.
     * @return The health value of the Tag this TagHolder might have. Returns 0 if health tag does not exist.
     */
    public int getHealthTagAmount(){
        Tag healthTag = getTag(TagRegistry.HEALTH_START);
        if (healthTag instanceof HealthTag){
            return ((HealthTag)healthTag).getHealthAmount();
        }
        return 0;
    }

    public boolean hasTag(int id){ return getTag(id) != null; }

    //E V E N T S

    public void heal(int amount){}

    public void onReceiveDamage(int amount, TagHolder source, GameInstance gi){
        if (!shouldContact(source, this)) return;
        TagEvent dmgEvent = new TagEvent(amount, source, this, gi, this);
        for (Tag tag : getTags()){
            tag.onReceiveDamage(dmgEvent);
        }
        dmgEvent.doFutureActions();
        if (dmgEvent.eventPassed()){
            dmgEvent.doCancelableActions();
            receiveDamage(dmgEvent.getAmount());
        }
    }

    public boolean shouldContact(TagHolder source, TagHolder target){
        return source.hasTag(TagRegistry.ETHEREAL) || !target.hasTag(TagRegistry.ETHEREAL);
    }

    protected void receiveDamage(int amount){
        //Override this
    }

    public int getCurrentHealth(){
        return 0; //Override this
    }

    public void selfDestruct(){

    }

    /*
    * CONTACT
    *
    * Objects contacting each other is a very common occurrence in Sourcery Text, but the nature of contact can be tricky to properly implement.
    *
    * For example, water balloons would explode upon being dropped on the ground, as the loot pile that represents it had just been instantiated and must then contact the ground.
    * This is totally logical behavior, but it's important to remember how that feels to the player. The player is strategically dropping items out of their inventory (probably due to weight limits), and the reward for this behavior is...destroying the items they dropped.
    * Plus, what if "dropping" an item is interpreted as "gently setting on the ground?"
    *
    * Therefore, "contact" must be more complex.
    *
    * Thus, there is such a thing as contact "strength." Getting the water balloon tag to work in all cases except the very specific case where it is dropped on the ground is hard to properly accomplish.
    * Therefore, when calling the onContact() method, one must specify how "strong" that contact is. So when an item is dropped on the ground, it is treated as "light" and the water balloon can then check for "heavy" contact and then conditionally do its behavior.
    *
    * The "older" contact system works fairly well, but fails spectacularly when dealing with Loot Piles.
    * The main problem of Loot Piles is that it's one TagHolder trying to behave as a group of other TagHolders (the items it contains).
    * When you contact a Loot Pile, you should instead be contacting every item in its inventory and bypass the Loot Pile entirely. That way, the contact events have the proper source and targets to behave correctly.
    *
    * The solution I found is to give the contact receiving end the full responsibility instead of the "sending" side.
    * */

    public void onContact(TagHolder other, GameInstance gi, int contactStrength){
        if (other == null || !shouldContact(this, other))
            return;
        other.receiveContact(this, gi, contactStrength);
    }

    //This method can be overwritten to allow Loot Piles to behave differently regarding contact, and perhaps others too.
    protected void receiveContact(TagHolder source, GameInstance gi, int contactStrength){
        performTwoWayContact(source, this, gi, contactStrength);
    }

    protected void performTwoWayContact(TagHolder obj1, TagHolder obj2, GameInstance gi, int contactStrength){
        contactEvent(obj1, obj2, gi, contactStrength);
        contactEvent(obj2, obj1, gi, contactStrength);
        DebugWindow.reportf(DebugWindow.TAGS, "TagHolder.performTwoWayContact", "\'" + obj1.getClass().getSimpleName() + "\' & \'" + obj2.getClass().getSimpleName() + "\'");
        DebugWindow.reportf(DebugWindow.TAGS, "TagHolder.performTwoWayContact OBJ 1", "Tags: " + obj1.getTagList());
        DebugWindow.reportf(DebugWindow.TAGS, "TagHolder.performTwoWayContact OBJ 2", "Tags: " + obj2.getTagList());
    }

    private void contactEvent(TagHolder source, TagHolder target, GameInstance gi, int strength){
        TagEvent e = new TagEvent(strength, source, target, gi, source);
        for (Tag tag : source.getTags()){
            tag.onContact(e);
        }
        e.doFutureActions();
        if (e.eventPassed()){
            e.doCancelableActions();
        }
    }

    protected String getTagList(){
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < getTags().size(); i++) {
            output.append(getTags().get(i).getName());
            if (i < getTags().size() - 1) output.append(", ");
        }
        return output.toString();
    }

    public Color colorateWithTags(Color baseColor){
        int baseAlpha = baseColor.getAlpha();
        int[] colorTotals = {baseColor.getRed() * baseAlpha, baseColor.getGreen() * baseAlpha, baseColor.getBlue() * baseAlpha, baseAlpha}; //Elements refer to r,g,b,a in order
        for (Tag tag : getTags()){
            Color color = tag.getTagColor();
            colorTotals[0] += color.getRed()   * color.getAlpha();
            colorTotals[1] += color.getGreen() * color.getAlpha();
            colorTotals[2] += color.getBlue()  * color.getAlpha();
            colorTotals[3] += color.getAlpha();
        }
        return new Color(colorTotals[0] / colorTotals[3], colorTotals[1] / colorTotals[3], colorTotals[2] / colorTotals[3], Math.min(colorTotals[3], 255)); //Perform weighted average
    }
}

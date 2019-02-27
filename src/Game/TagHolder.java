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
            TagEvent e = new TagEvent(0, true, source, this, null, this);
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

    public void onLevelEnter(GameInstance gi){
        TagEvent event = new TagEvent(0, true, this, this, gi, this);
        for (Tag tag : tags) {
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
        TagEvent dmgEvent = new TagEvent(amount, true, source, this, gi, this);
        for (Tag tag : tags){
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

    public void onContact(TagHolder other, GameInstance gi, int contactStrength){
        if (other == null || !shouldContact(this, other))
            return;
        contactEvent(this, other, gi);
        contactEvent(other, this, gi);
        DebugWindow.reportf(DebugWindow.TAGS, "TagHolder.onContact", "TagHolder \'" + this.getClass().getSimpleName() + "\'");
        DebugWindow.reportf(DebugWindow.TAGS, "TagHolder.onContact  SELF", "Tags: " + getTagList());
        DebugWindow.reportf(DebugWindow.TAGS, "TagHolder.onContact OTHER", "Tags: " + other.getTagList());
    }

    private void contactEvent(TagHolder source, TagHolder target, GameInstance gi){
        TagEvent e = new TagEvent(0, true, source, target, gi, source);
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
        for (int i = 0; i < tags.size(); i++) {
            output.append(tags.get(i).getName());
            if (i < tags.size() - 1) output.append(", ");
        }
        return output.toString();
    }

    public Color colorateWithTags(Color baseColor){
        int baseAlpha = baseColor.getAlpha();
        int[] colorTotals = {baseColor.getRed() * baseAlpha, baseColor.getGreen() * baseAlpha, baseColor.getBlue() * baseAlpha, baseAlpha}; //Elements refer to r,g,b,a in order
        for (Tag tag : tags){
            Color color = tag.getTagColor();
            colorTotals[0] += color.getRed()   * color.getAlpha();
            colorTotals[1] += color.getGreen() * color.getAlpha();
            colorTotals[2] += color.getBlue()  * color.getAlpha();
            colorTotals[3] += color.getAlpha();
        }
        return new Color(colorTotals[0] / colorTotals[3], colorTotals[1] / colorTotals[3], colorTotals[2] / colorTotals[3], Math.min(colorTotals[3], 255)); //Perform weighted average
    }
}

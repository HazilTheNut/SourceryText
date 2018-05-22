package Game;

import Game.Debug.DebugWindow;
import Game.Registries.TagRegistry;
import Game.Tags.DamageTag;
import Game.Tags.HealthTag;
import Game.Tags.Tag;

import java.awt.*;
import java.util.ArrayList;

/**
 * Created by Jared on 3/31/2018.
 */
public class TagHolder {

    private ArrayList<Tag> tags = new ArrayList<>();

    public ArrayList<Tag> getTags() { return tags; }

    public void addTag(int tagID, TagHolder source){
        Tag tag = TagRegistry.getTag(tagID);
        if (tag != null) {
            addTag(tag, source);
        }
    }

    public void addTag(Tag tag, TagHolder source) {
        if (!hasTag(tag.getId())) {
            tags.add(tag);
            TagEvent e = new TagEvent(0, true, source, this, null);
            for (Tag currentTag : tags){
                currentTag.onAdd(e);
            }
            tag.onAddThis(e);
            if (e.eventPassed()){
                e.enactEvent();
            } else {
                tags.remove(tag);
            }
        }
    }

    public void removeTag (int id) {
        for (Tag tag : tags){
            if (tag.getId() == id) {
                tags.remove(tag);
                return;
            }
        }
    }

    public Tag getTag(int id){
        for (Tag tag : tags){
            if (tag.getId() == id) return tag;
        }
        return null;
    }

    /**
     * A convenience function that saves you the trouble of casting the damage Tag to a DamageTag and handling null tags.
     * @return The damage value of the Tag this TagHolder might have. Returns 0 if damage tag does not exist.
     */
    public int getDamageTagAmount(){
        Tag dmgTag = getTag(TagRegistry.DAMAGE_START);
        if (dmgTag != null && dmgTag instanceof DamageTag){
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
        if (healthTag != null && healthTag instanceof HealthTag){
            return ((HealthTag)healthTag).getHealthAmount();
        }
        return 0;
    }

    public boolean hasTag(int id){ return getTag(id) != null; }

    //E V E N T S

    public void heal(int amount){}

    public void receiveDamage(int amount){}

    public void onContact(TagHolder other, GameInstance gi){
        contactEvent(this, other, gi);
        contactEvent(other, this, gi);
        DebugWindow.reportf(DebugWindow.TAGS, "TagHolder.onContact", "TagHolder \'" + this.getClass().getSimpleName() + "\'");
        DebugWindow.reportf(DebugWindow.TAGS, "TagHolder.onContact  SELF", "Tags: " + getTagList());
        DebugWindow.reportf(DebugWindow.TAGS, "TagHolder.onContact OTHER", "Tags: " + other.getTagList());
    }

    private void contactEvent(TagHolder source, TagHolder target, GameInstance gi){
        TagEvent e = new TagEvent(0, true, source, target, gi);
        for (Tag tag : source.getTags()){
            tag.onContact(e);
        }
        if (e.eventPassed()){
            e.enactEvent();
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

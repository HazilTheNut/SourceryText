package Game;

import Game.Tags.Tag;

import java.util.ArrayList;

/**
 * Created by Jared on 3/31/2018.
 */
public class TagHolder {

    private ArrayList<Tag> tags = new ArrayList<>();

    public ArrayList<Tag> getTags() { return tags; }

    public void addTag(Tag tag, TagHolder source) {
        if (!hasTag(tag.getId())) {
            TagEvent e = new TagEvent(0, true, source, this, null);
            for (Tag currentTag : tags){
                currentTag.onAdd(e);
            }
            tag.onAddThis(e);
            if (e.eventPassed()){
                e.enactEvent();
                tags.add(tag);
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

    public boolean hasTag(int id){ return getTag(id) != null; }

    //E V E N T S

    public void heal(int amount){}

    public void receiveDamage(int amount){}

    public void onContact(TagHolder other, GameInstance gi){
        contactEvent(this, other, gi);
        contactEvent(other, this, gi);
        DebugWindow.reportf(DebugWindow.TAGS, "[TagHolder.onContact] TagHolder \'" + this.getClass().getSimpleName() + "\'");
        DebugWindow.reportf(DebugWindow.TAGS, "[TagHolder.onContact] Tags of me: " + getTagList());
        DebugWindow.reportf(DebugWindow.TAGS, "[TagHolder.onContact] Tags of other: " + other.getTagList());
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
        String output = "";
        for (Tag tag : getTags()){
            output += "\n> " + tag.getName();
        }
        return output;
    }
}

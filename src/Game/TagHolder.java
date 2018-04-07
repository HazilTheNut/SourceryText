package Game;

import Game.Tags.Tag;

import java.util.ArrayList;

/**
 * Created by Jared on 3/31/2018.
 */
public class TagHolder {

    private ArrayList<Tag> tags = new ArrayList<>();

    public ArrayList<Tag> getTags() { return tags; }

    public void addTag(Tag tag) { tags.add(tag); }

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

    public void heal(int amount){}

    public void receiveDamage(int amount){}
}

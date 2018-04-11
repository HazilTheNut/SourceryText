package Game.Tags;

import Game.TagEvent;
import Game.TagHolder;

/**
 * Created by Jared on 3/26/2018.
 */
public class Tag {

    private String name;
    private int id;

    public int getId() { return id; }

    public String getName() { return name; }

    public void setId(int id) { this.id = id; }

    public void setName(String name) { this.name = name; }

    // E V E N T S

    public void onItemUse(TagEvent e) {}

    public void onDealDamage(TagEvent e) {}

    public void onTurn(TagEvent e){}
}

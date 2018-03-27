package Game.Entities;

import Engine.Layer;
import Engine.LayerManager;
import Engine.SpecialText;
import Game.Coordinate;
import Game.Tags.Tag;

import java.awt.*;
import java.util.ArrayList;

/**
 * Created by Jared on 3/27/2018.
 */
public class Entity {

    private ArrayList<Tag> tags;

    private Coordinate location;
    private Layer sprite; //Not to be mistaken with 7-up

    public Entity(Coordinate pos, LayerManager lm, String name){
        location = pos;
        sprite = new Layer(new SpecialText[1][1], name, getLocation().getX(), getLocation().getY());
        sprite.editLayer(0, 0, new SpecialText('F', new Color(255, 200, 200)));
        lm.addLayer(sprite);
    }

    Coordinate getLocation(){ return location; }

    Layer getSprite() { return sprite; }

    //Ran when it is their turn to do something
    public void onTurn(){}

}

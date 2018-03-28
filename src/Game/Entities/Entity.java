package Game.Entities;

import Data.EntityStruct;
import Engine.Layer;
import Engine.LayerManager;
import Engine.SpecialText;
import Game.Coordinate;
import Game.GameInstance;
import Game.LayerImportances;
import Game.Tags.Tag;
import java.util.ArrayList;

/**
 * Created by Jared on 3/27/2018.
 */
public class Entity {

    private ArrayList<Tag> tags;

    private GameInstance gi;
    protected LayerManager lm;

    private Coordinate location;
    private Layer sprite; //Not to be mistaken with 7-up

    public void initialize(Coordinate pos, LayerManager lm, EntityStruct entityStruct, GameInstance gameInstance){
        location = pos;
        sprite = new Layer(new SpecialText[1][1], createEntityLayerName(entityStruct, pos), getLocation().getX(), getLocation().getY(), LayerImportances.ENTITY);
        sprite.editLayer(0, 0, entityStruct.getDisplayChar());
        lm.addLayer(sprite);
        gi = gameInstance;
        this.lm = lm;
    }

    protected String createEntityLayerName(EntityStruct struct, Coordinate coordinate){
        return String.format("%1$s [%2$d,%3$d]", struct.getEntityName(), coordinate.getX(), coordinate.getY());
    }

    Coordinate getLocation(){ return location; }

    Layer getSprite() { return sprite; }

    void move(int relativeX, int relativeY){
        location.movePos(relativeX, relativeY);
        //sprite.movePos(relativeX, relativeY);
        lm.getLayer(sprite.getName()).movePos(relativeX, relativeY);
    }

    void turnSleep(int time){
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //Ran when it is their turn to do something
    public void onTurn(){}

}

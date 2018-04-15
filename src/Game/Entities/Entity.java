package Game.Entities;

import Data.EntityStruct;
import Engine.Layer;
import Engine.LayerManager;
import Engine.SpecialText;
import Data.Coordinate;
import Game.GameInstance;
import Data.LayerImportances;
import Game.Registries.EntityRegistry;
import Game.Registries.TagRegistry;
import Game.TagEvent;
import Game.TagHolder;
import Game.Tags.Tag;

/**
 * Created by Jared on 3/27/2018.
 */
public class Entity extends TagHolder{

    protected GameInstance gi;
    protected LayerManager lm;

    private Coordinate location;
    private Layer sprite; //Not to be mistaken with 7-up

    private String name;

    public void initialize(Coordinate pos, LayerManager lm, EntityStruct entityStruct, GameInstance gameInstance){
        location = pos;
        sprite = new Layer(new SpecialText[1][1], createEntityLayerName(entityStruct, pos), getLocation().getX(), getLocation().getY(), LayerImportances.ENTITY);
        sprite.editLayer(0, 0, entityStruct.getDisplayChar());
        gi = gameInstance;
        this.lm = lm;
        EntityRegistry er = new EntityRegistry();
        name = er.getEntityStruct(entityStruct.getEntityId()).getEntityName();
        TagRegistry tagRegistry = new TagRegistry();
        for (int id : entityStruct.getTagIDs()){
            addTag(tagRegistry.getTag(id), this);
        }
    }

    public void onLevelEnter(){
        lm.addLayer(sprite);
    }

    public void onLevelExit(){
        lm.removeLayer(sprite);
    }

    protected String createEntityLayerName(EntityStruct struct, Coordinate coordinate){
        return String.format("%1$s [%2$d,%3$d]", struct.getEntityName(), coordinate.getX(), coordinate.getY());
    }

    public Coordinate getLocation(){ return location; }

    public Layer getSprite() { return sprite; }

    protected void setSprite(Layer sprite) { this.sprite = sprite; }

    public GameInstance getGameInstance() { return gi; }

    protected void setLocation(Coordinate pos) { location = pos; }

    protected void move(int relativeX, int relativeY){
        if (getGameInstance().isSpaceAvailable(getLocation().add(new Coordinate(relativeX, relativeY)))) {
            location.movePos(relativeX, relativeY);
            lm.getLayer(sprite.getName()).movePos(relativeX, relativeY);
            //System.out.printf("[Entity.move] \'%1$s\':\n", getName());
            onContact(gi.getTileAt(location), gi);
        }
    }

    public void teleport(Coordinate pos){
        move(pos.getX() - location.getX(), pos.getY() - location.getY());
    }

    void selfDestruct(){
        gi.removeEntity(this);
        lm.removeLayer(sprite);
    }

    void turnSleep(int time){
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public String getName() {
        return name;
    }

    protected void setName(String str){ name = str; }

    //Ran when it is their turn to do something
    public void onTurn(){
        TagEvent turnEvent = new TagEvent(0, true, this, this, getGameInstance());
        for (Tag tag : getTags()){
            tag.onTurn(turnEvent);
        }
        if (turnEvent.eventPassed()){
            turnEvent.enactEvent();
        }
    }

}

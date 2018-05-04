package Game.Entities;

import Data.*;
import Engine.Layer;
import Engine.LayerManager;
import Engine.SpecialText;
import Game.*;
import Game.Registries.EntityRegistry;
import Game.Registries.ItemRegistry;
import Game.Registries.TagRegistry;
import Game.Tags.Tag;

import java.util.ArrayList;

/**
 * Created by Jared on 3/27/2018.
 */
public class Entity extends TagHolder{

    protected GameInstance gi;
    protected LayerManager lm;

    private ArrayList<Item> items = new ArrayList<>();

    private Coordinate location;
    private Layer sprite; //Not to be mistaken with 7-up

    private String name;
    private int id;

    public void initialize(Coordinate pos, LayerManager lm, EntityStruct entityStruct, GameInstance gameInstance){
        location = pos;
        id = entityStruct.getEntityId();
        sprite = new Layer(new SpecialText[1][1], createEntityLayerName(entityStruct, pos), getLocation().getX(), getLocation().getY(), LayerImportances.ENTITY);
        sprite.editLayer(0, 0, entityStruct.getDisplayChar());
        gi = gameInstance;
        this.lm = lm;
        name = EntityRegistry.getEntityStruct(entityStruct.getEntityId()).getEntityName();
        for (int id : entityStruct.getTagIDs()){
            addTag(id, this);
        }
        for (ItemStruct struct : entityStruct.getItems()){
            Item item = ItemRegistry.generateItem(struct.getItemId()).setQty(struct.getQty());
            addItem(item);
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
        TagEvent moveEvent = new TagEvent(0, true, this, gi.getTileAt(getLocation().add(new Coordinate(relativeX, relativeY))), gi);
        for (Tag tag : getTags()) tag.onMove(moveEvent);
        if (moveEvent.eventPassed() && shouldDoAction() && getGameInstance().isSpaceAvailable(getLocation().add(new Coordinate(relativeX, relativeY)), TagRegistry.NO_PATHING)) {
            location.movePos(relativeX, relativeY);
            sprite.movePos(relativeX, relativeY);
            DebugWindow.reportf(DebugWindow.GAME, "[Entity.move] \'%1$s\'", getName());
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

    public void addItem(Item item) {
        if (item.isStackable())
            for (Item i : items){
                if (i.getItemData().getItemId() == item.getItemData().getItemId()){
                    int total = i.getItemData().getQty() + item.getItemData().getQty();
                    if (total < 100){
                        i.setQty(total);
                        return;
                    } else {
                        i.setQty(99);
                        items.add(item.setQty(total - 99));
                        return;
                    }
                }
            }
        items.add(item);
    }

    public void removeItem(Item item) { items.remove(item); }

    public ArrayList<Item> getItems() {
        return items;
    }

    public void updateInventory(){
        for (int ii = 0; ii < items.size();){
            if (items.get(ii).getItemData().getQty() <= 0){
                items.remove(items.get(ii));
            } else {
                TagEvent updateEvent = new TagEvent(0, true, items.get(ii), items.get(ii), getGameInstance());
                for (Tag tag : items.get(ii).getTags()) tag.onTurn(updateEvent);
                if (updateEvent.eventPassed()) updateEvent.enactEvent();
                ii++;
            }
        }
    }

    public void scanInventory(){
        for (int ii = 0; ii < items.size();){
            if (items.get(ii).getItemData().getQty() <= 0){
                items.remove(items.get(ii));
            } else {
                ii++;
            }
        }
    }

    public ArrayList<EntityArg> generateArgs(){
        ArrayList<EntityArg> args = new ArrayList<>();
        args.add(new EntityArg("interactText", ""));
        return args;
    }

    //Ran when it is their turn to do something
    public void onTurn(){
        TagEvent turnEvent = new TagEvent(0, true, this, this, getGameInstance());
        for (Tag tag : getTags()){
            tag.onTurn(turnEvent);
        }
        if (turnEvent.eventPassed()){
            turnEvent.enactEvent();
        }
        updateInventory();
    }

    @Override
    public void addTag(int tagID, TagHolder source) {
        super.addTag(tagID, source);
        updateSprite();
    }

    @Override
    public void removeTag(int id) {
        super.removeTag(id);
        updateSprite();
    }

    boolean shouldDoAction(){
        TagEvent actionEvent = new TagEvent(0, true, this, null, gi);
        for (Tag tag : getTags()){
            tag.onEntityAction(actionEvent);
        }
        if (actionEvent.eventPassed()){
            actionEvent.enactEvent();
            return true;
        }
        return false;
    }

    protected void updateSprite(){
        SpecialText originalSprite = EntityRegistry.getEntityStruct(id).getDisplayChar();
        DebugWindow.reportf(DebugWindow.MISC, "[Entity.updateSprite] Original sprite for %1$s: %2$s", getClass().getSimpleName(), originalSprite);
        sprite.editLayer(0, 0, new SpecialText(originalSprite.getCharacter(), colorateWithTags(originalSprite.getFgColor()), originalSprite.getBkgColor()));
    }

    public void onInteract(Player player){}

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Entity) {
            Entity entity = (Entity) obj;
            return entity.getSprite().getName().equals(getSprite().getName());
        }
        return false;
    }
}

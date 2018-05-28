package Game.Entities;

import Data.*;
import Engine.Layer;
import Engine.LayerManager;
import Engine.SpecialText;
import Game.*;
import Game.Debug.DebugWindow;
import Game.Registries.EntityRegistry;
import Game.Registries.ItemRegistry;
import Game.Registries.TagRegistry;
import Game.Tags.Tag;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by Jared on 3/27/2018.
 */
public class Entity extends TagHolder implements Serializable {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    protected GameInstance gi;

    private ArrayList<Item> items = new ArrayList<>();

    private Coordinate location;
    private Layer sprite; //Not to be mistaken with 7-up
    private SpecialText icon;

    private String name;
    private long uniqueID;

    public void initialize(Coordinate pos, LayerManager lm, EntityStruct entityStruct, GameInstance gameInstance){
        gi = gameInstance;

        simpleInit(entityStruct, pos);
        String defName = EntityRegistry.getEntityStruct(entityStruct.getEntityId()).getEntityName(); //Default name
        name = readStrArg(searchForArg(entityStruct.getArgs(), "name"), defName); //Searches for name in args list

        icon = readSpecTxtArg(searchForArg(entityStruct.getArgs(), "icon"), icon);
        int layerPriority = (isSolid()) ? LayerImportances.ENTITY_SOLID : LayerImportances.ENTITY;
        sprite = new Layer(new SpecialText[1][1], createEntityLayerName(entityStruct, pos), getLocation().getX(), getLocation().getY(), layerPriority);
        sprite.editLayer(0, 0, icon);

        for (int id : entityStruct.getTagIDs()){
            addTag(id, this);
        }
        for (ItemStruct struct : entityStruct.getItems()){
            Item item = ItemRegistry.generateItem(struct.getItemId(), gameInstance).setQty(struct.getQty());
            addItem(item);
        }
        uniqueID = gi.issueUID();
    }

    public boolean isSolid() { return true; }

    public void simpleInit(EntityStruct entityStruct, Coordinate pos){
        location = pos;
        name = entityStruct.getEntityName();
        icon = entityStruct.getDisplayChar();
        DebugWindow.reportf(DebugWindow.MISC, "Entity.simpleInit"," Original: %1$s After conversions: %2$s", icon, SpecialText.fromString(icon.toString()));
    }

    public void assignGameInstance(GameInstance gi){
        this.gi = gi;
    }

    public void onLevelEnter(){
        gi.getLayerManager().addLayer(sprite);
        onLevelEnter(gi);
    }

    public void onLevelExit(){
        gi.getLayerManager().removeLayer(sprite);
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
            onContact(gi.getTileAt(location), gi);
        }
    }

    /**
     * Basically runs move(), but does the subtractive math for you so you can't get it wrong.
     * @param pos
     */
    public void teleport(Coordinate pos){
        move(pos.getX() - location.getX(), pos.getY() - location.getY());
    }

    /**
     * A stronger version of teleport, which does not run any checks before placing the Entity at a location, regardless of any solid objects or tiles that exist at the new location.
     * @param pos
     */
    public void setPos(Coordinate pos){
        location.setPos(pos.getX(), pos.getY());
        sprite.setPos(pos.getX(), pos.getY());
        onContact(gi.getTileAt(location), gi);
    }

    void selfDestruct(){
        gi.removeEntity(this);
        gi.getLayerManager().removeLayer(sprite);
        DebugWindow.reportf(DebugWindow.GAME, "Entity.selfDestruct","\'%1$s\' at %2$s", getName(), getLocation());
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

    public void setName(String str){ name = str; }

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
                        items.add(0, item.setQty(total - 99));
                        return;
                    }
                }
            }
        items.add(0, item);
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
                if (updateEvent.eventPassed()) updateEvent.doCancelableActions();
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
        args.add(new EntityArg("name", name));
        args.add(new EntityArg("icon", icon.toString()));
        return args;
    }

    //Ran when it is their turn to do something
    public void onTurn(){
        TagEvent turnEvent = new TagEvent(0, true, this, this, getGameInstance());
        for (Tag tag : getTags()){
            tag.onTurn(turnEvent);
        }
        if (turnEvent.eventPassed()){
            turnEvent.doCancelableActions();
        }
        updateInventory();
        DebugWindow.reportf(DebugWindow.ENTITY, String.format("Entity#%1$05d.onTurn", getUniqueID()), "Name: \'%1$-20s\' Pos: %2$s", getName(), location);
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
            actionEvent.doCancelableActions();
            return true;
        }
        return false;
    }

    protected void updateSprite(){
        SpecialText originalSprite = icon;
        DebugWindow.reportf(DebugWindow.MISC, "Entity.updateSprite","Original sprite for %1$s: %2$s", getClass().getSimpleName(), originalSprite);
        sprite.editLayer(0, 0, new SpecialText(originalSprite.getCharacter(), colorateWithTags(originalSprite.getFgColor()), originalSprite.getBkgColor()));
    }

    public void onInteract(Player player){}

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Entity) {
            Entity entity = (Entity) obj;
            return entity.getUniqueID() == uniqueID;
        }
        return false;
    }

    public long getUniqueID() {
        return uniqueID;
    }

    public int getPathingSize(){
        return 0;
    }

    /**
     * Reads an EntityArg and returns an integer
     * @param arg EntityArg to read
     * @param def Default number if contents of arg are not integer-formatted or does not exist
     * @return Resulting integer
     */
    protected int readIntArg(EntityArg arg, int def){
        if (arg != null) {
            Scanner sc = new Scanner(arg.getArgValue());
            if (sc.hasNextInt()) {
                return sc.nextInt();
            }
        }
        return def;
    }

    /**
     * Reads an EntityArg and returns a string
     * @param arg EntityArg to read
     * @param def Default string if arg does not exist
     * @return Resulting String
     */
    protected String readStrArg(EntityArg arg, String def){
        if (arg != null) {
            return arg.getArgValue();
        }
        return def;
    }

    /**
     * Reads an EntityArg and returns a string
     * @param arg EntityArg to read
     * @param def Deafult string if arg does not exist or an error occurred in trying to read the value
     * @return Resulting SpecialText
     */
    protected SpecialText readSpecTxtArg(EntityArg arg, SpecialText def){
        if (arg != null){
            SpecialText txt = SpecialText.fromString(arg.getArgValue());
            if (txt != null) return txt;
        }
        return def;
    }

    protected boolean readBoolArg(EntityArg arg, boolean def){
        if (arg != null) {
            if (arg.getArgValue().toLowerCase().equals("true")) return true;
            if (arg.getArgValue().toLowerCase().equals("false")) return false;
        }
        return def;
    }

    protected EntityArg searchForArg(ArrayList<EntityArg> providedArgs, String name){
        for (EntityArg arg : providedArgs){
            if (arg.getArgName().equals(name))
                return arg;
        }
        return null;
    }

}

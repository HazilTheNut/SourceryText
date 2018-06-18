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

    /**
     * Entity:
     *
     * One of the essential components to Sourcery Text.
     * The Backdrop, Tiles, WarpZones, and LevelScripts are all very static and unmoving.
     * In contrast to this, Entities are designed to change.
     *
     * Each Entity has its own Layer and Coordinate location.
     * All Entities are able to move.
     * All Entities can be instantiated, and they can be destroyed.
     * All Entities can be divert from the pre-fabricated ones in the EntityRegistry through Entity Arguments and their inventory of items.
     */

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    protected GameInstance gi;

    private ArrayList<Item> items = new ArrayList<>();

    private Coordinate location;
    private Layer sprite; //Not to be mistaken with 7-up
    private SpecialText icon;

    private String name;
    private long uniqueID;

    boolean isAlive = true;

    /**
     * Initializes an Entity, configuring it such that it can be used by the GameInstance.
     *
     * It is important to NOT use the constructor of Entity or its inheritors for anything of significance.
     * This is because Class.newInstance() runs only the nullary constructor of an Entity, and that is what is used to instantiate Entities.
     *
     * So as a work-around, initialize() exists to fill in the purpose of the constructor
     *
     * @param pos Starting position of the Entity
     * @param lm The current LayerManager
     * @param entityStruct The EntityStruct to unpack
     * @param gameInstance The current GameInstance
     */
    public void initialize(Coordinate pos, LayerManager lm, EntityStruct entityStruct, GameInstance gameInstance){
        gi = gameInstance;
        uniqueID = gi.issueUID();

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
    }

    public boolean isSolid() { return true; }

    public boolean isAlive() {
        return isAlive;
    }

    /**
     * Runs a simplified version of initialize().
     *
     * It does not prepare the Entity for usage in SourceryText, but it is useful if one needs to access other functions of an Entity without instantiating it into the game.
     *
     * @param entityStruct The EntityStruct to construct from
     * @param pos The starting location of the Entity
     */
    public void simpleInit(EntityStruct entityStruct, Coordinate pos){
        location = pos;
        name = entityStruct.getEntityName();
        icon = entityStruct.getDisplayChar();
    }

    public void assignGameInstance(GameInstance gi){
        this.gi = gi;
    }

    @Override
    public void onLevelEnter(GameInstance gameInstance){
        gi.getLayerManager().addLayer(sprite);
        super.onLevelEnter(gi);
    }

    public void onLevelEnter(){
        onLevelEnter(gi);
    }

    public void onLevelExit(){
        gi.getLayerManager().removeLayer(sprite);
        DebugWindow.reportf(DebugWindow.ENTITY, String.format("Entity#%1$05d.onTurn", getUniqueID()), "Name: \'%1$-20s\' - - -", getName());
    }

    private String createEntityLayerName(EntityStruct struct, Coordinate coordinate){
        return String.format("%1$s:%4$d [%2$d,%3$d]", struct.getEntityName(), coordinate.getX(), coordinate.getY(), uniqueID);
    }

    public Coordinate getLocation(){ return location; }

    public Layer getSprite() { return sprite; }

    protected void setSprite(Layer sprite) { this.sprite = sprite; }

    public GameInstance getGameInstance() { return gi; }

    protected void setLocation(Coordinate pos) { location = pos; }

    protected void move(int relativeX, int relativeY){
        TagEvent moveEvent = new TagEvent(0, true, this, gi.getTileAt(getLocation().add(new Coordinate(relativeX, relativeY))), gi);
        for (Tag tag : getTags()) tag.onMove(moveEvent);
        moveEvent.doFutureActions();
        if (moveEvent.eventPassed() && shouldDoAction() && getGameInstance().isSpaceAvailable(getLocation().add(new Coordinate(relativeX, relativeY)), TagRegistry.NO_PATHING)) {
            location.movePos(relativeX, relativeY);
            sprite.movePos(relativeX, relativeY);
            onContact(gi.getTileAt(location), gi);
        }
    }

    /**
     * Basically runs move(), but does the subtractive math for you so you can't get it wrong.
     *
     * @param pos The position to teleport to
     */
    public void teleport(Coordinate pos){
        move(pos.getX() - location.getX(), pos.getY() - location.getY());
    }

    /**
     * A stronger version of teleport, which does not run any checks before placing the Entity at a location, regardless of any solid objects or tiles that exist at the new location.
     *
     * @param pos The position to relocate to
     */
    public void setPos(Coordinate pos){
        location.setPos(pos.getX(), pos.getY());
        sprite.setPos(pos.getX(), pos.getY());
        onContact(gi.getTileAt(location), gi);
    }

    public void selfDestruct(){
        isAlive = false;
        gi.removeEntity(this);
        gi.getLayerManager().removeLayer(sprite);
        DebugWindow.reportf(DebugWindow.ENTITY, String.format("Entity#%1$05d.onTurn", getUniqueID()), "Name: \'%1$-20s\' - - -", getName());
    }

    /**
     * A nice convenience function for adding pauses wherever necessary.
     *
     * @param time The time (in ms) to wait.
     */
    protected void turnSleep(int time){
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

    /**
     * Adds an item to the inventory.
     *
     * If there already is an item of the same ID in the inventory and is stackable, then stack them on top of each other.
     *
     * @param item The item to add
     */
    public void addItem(Item item) {
        if (item.isStackable())
            for (Item i : items){
                if (i.getItemData().getItemId() == item.getItemData().getItemId() && item.getItemData().getQty() < 99){
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

    /**
     * Performs the actions of scanInventory(), but also performs onTurn() for each item as well
     */
    public void updateInventory(){
        for (int ii = 0; ii < items.size();){
            if (items.get(ii).getItemData().getQty() <= 0){
                items.remove(items.get(ii));
            } else {
                TagEvent updateEvent = new TagEvent(0, true, items.get(ii), items.get(ii), getGameInstance());
                for (Tag tag : items.get(ii).getTags()) tag.onTurn(updateEvent);
                updateEvent.doFutureActions();
                if (updateEvent.eventPassed()) updateEvent.doCancelableActions();
                ii++;
            }
        }
    }

    /**
     * Checks the inventory for items with < 0 quantity and erases them from the inventory.
     */
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
        turnEvent.doFutureActions();
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

    protected boolean shouldDoAction(){
        TagEvent actionEvent = new TagEvent(0, true, this, null, gi);
        for (Tag tag : getTags()){
            tag.onEntityAction(actionEvent);
        }
        actionEvent.doFutureActions();
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

    //Override this with stuff to do with interacted with the player
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

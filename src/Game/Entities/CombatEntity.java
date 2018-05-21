package Game.Entities;

import Data.*;
import Engine.Layer;
import Engine.LayerManager;
import Engine.SpecialText;
import Game.Debug.DebugWindow;
import Game.GameInstance;
import Game.Item;
import Game.Registries.EntityRegistry;
import Game.Registries.TagRegistry;
import Game.TagEvent;
import Game.Tags.Tag;

import java.awt.*;
import java.util.ArrayList;

/**
 * Created by Jared on 3/28/2018.
 */
public class CombatEntity extends Entity{

    private int health;
    private int maxHealth = 10;
    private int strength = 0;

    protected int defaultMaxHealth = 10;
    protected int defaultStrength  = 1;

    private static final int RIGHT = 0;
    private static final int UP_RIGHT = 45;
    private static final int UP = 90;
    private static final int UP_LEFT = 135;
    private static final int LEFT = 180;
    private static final int DOWN_LEFT = 225;
    private static final int DOWN = 270;
    private static final int DOWN_RIGHT = 315;
    private static final int RIGHT_360 = 360;

    private static final int[] directions = {RIGHT, UP_RIGHT, UP, UP_LEFT, LEFT, DOWN_LEFT, DOWN, DOWN_RIGHT, RIGHT_360};

    Item weapon;
    private Layer swooshLayer;

    protected void setMaxHealth(int maxHP){
        maxHealth = maxHP;
        health = maxHP;
    }

    public void setStrength(int strength) {
        this.strength = strength;
    }

    public int getStrength() {
        return strength;
    }

    @Override
    public ArrayList<EntityArg> generateArgs() {
        ArrayList<EntityArg> args = super.generateArgs();
        args.add(new EntityArg("maxHealth", String.valueOf(defaultMaxHealth)));
        args.add(new EntityArg("strength",  String.valueOf(defaultStrength)));
        return args;
    }

    @Override
    public void initialize(Coordinate pos, LayerManager lm, EntityStruct entityStruct, GameInstance gameInstance) {
        super.initialize(pos, lm, entityStruct, gameInstance);
        EntityArg hpArg = searchForArg(entityStruct.getArgs(), "maxHealth");
        setMaxHealth(readIntArg(hpArg, defaultMaxHealth));
        EntityArg strArg = searchForArg(entityStruct.getArgs(), "strength");
        setStrength(readIntArg(strArg, defaultStrength));
        initSwwoshLayer();
    }

    protected void initSwwoshLayer(){
        swooshLayer = new Layer(new SpecialText[1][1], getSprite().getName().concat("_attack"), 0, 0, LayerImportances.ANIMATION);
        swooshLayer.editLayer(0, 0, new SpecialText(' ', Color.WHITE, new Color(255, 255, 255, 150)));
        swooshLayer.setVisible(false);
    }

    @Override
    public void receiveDamage(int amount) {
        if (amount > 0 ) {
            health -= amount;
            double percentage = Math.sqrt(Math.max(Math.min((double) amount / maxHealth, 1), 0.1));
            SpecialText originalSprite = getSprite().getSpecialText(0, 0);
            getSprite().editLayer(0, 0, new SpecialText(originalSprite.getCharacter(), originalSprite.getFgColor(), new Color(255, 0, 0, (int) (255 * percentage))));
            turnSleep(100 + (int) (500 * percentage));
            getSprite().editLayer(0, 0, originalSprite);
            if (health <= 0) selfDestruct();
        }
    }

    private void attack(Coordinate loc){
        if (shouldDoAction()){
            swooshLayer.setVisible(true);
            swooshLayer.setPos(loc);
            turnSleep(75);
            swooshLayer.setVisible(false);
            Entity entity = getGameInstance().getCurrentLevel().getSolidEntityAt(loc);
            if (entity != null && entity instanceof CombatEntity) {
                doAttackEvent((CombatEntity) entity);
            } else {
                if (getWeapon() != null)
                    getWeapon().onContact(getGameInstance().getTileAt(loc), getGameInstance());
                else
                    onContact(getGameInstance().getTileAt(loc), getGameInstance());
            }
        }
    }

    protected void doAttackEvent(CombatEntity ce){
        if (getWeapon() != null) {
            TagEvent event = new TagEvent(strength, true, this, ce, getGameInstance());
            for (Tag tag : getWeapon().getTags())
                tag.onDealDamage(event);
            if (event.eventPassed()) {
                event.enactEvent();
                ce.receiveDamage(event.getAmount());
                getWeapon().decrementQty();
                if (getWeapon().getItemData().getQty() <= 0) {
                    setWeapon(null);
                }
                if (getWeapon() != null)
                    getWeapon().onContact(ce, getGameInstance());
                else
                    onContact(ce, getGameInstance());
            }
        }
    }

    protected int calculateMeleeDirection(Coordinate target){
        int dy = getLocation().getY() - target.getY();
        int dx =  target.getX() - getLocation().getX();
        double angle = (180 / Math.PI) * Math.atan2(dy, dx);
        if (angle < 0) angle += 360;
        DebugWindow.reportf(DebugWindow.GAME, "CombatEntity.calculateMeleeDirection","Angle: %1$f dx: %2$d dy: %3$d", angle, dx, dy);
        for (int dir : directions){
            if (Math.abs(angle - dir) <= 22.5)
                return dir;
        }
        return -1;
    }

    protected void doWeaponAttack(Coordinate loc){
        for (int i = 0; i < getWeapon().getTags().size(); i++) {
            Tag tag = getWeapon().getTags().get(i);
            switch (tag.getId()) {
                case TagRegistry.WEAPON_STRIKE:
                    doStrikeWeaponAttack(calculateMeleeDirection(loc));
                    break;
                case TagRegistry.WEAPON_THRUST:
                    doThrustWeaponAttack(calculateMeleeDirection(loc));
                    break;
                case TagRegistry.WEAPON_SWEEP:
                    doSweepWeaponAttack(calculateMeleeDirection(loc));
                    break;
            }
        }
    }

    private void doStrikeWeaponAttack(int direction){
        switch (direction){
            case UP:
                attack(getLocation().add(new Coordinate(0, -1)));
                break;
            case UP_RIGHT:
                attack(getLocation().add(new Coordinate(1, -1)));
                break;
            case RIGHT:
            case RIGHT_360:
                attack(getLocation().add(new Coordinate(1, 0)));
                break;
            case DOWN_RIGHT:
                attack(getLocation().add(new Coordinate(1, 1)));
                break;
            case DOWN:
                attack(getLocation().add(new Coordinate(0, 1)));
                break;
            case DOWN_LEFT:
                attack(getLocation().add(new Coordinate(-1, 1)));
                break;
            case LEFT:
                attack(getLocation().add(new Coordinate(-1, 0)));
                break;
            case UP_LEFT:
                attack(getLocation().add(new Coordinate(-1, -1)));
                break;
        }
    }

    private void doThrustWeaponAttack(int direction){
        switch (direction){
            case UP:
                attack(getLocation().add(new Coordinate(0, -1)));
                attack(getLocation().add(new Coordinate(0, -2)));
                break;
            case UP_RIGHT:
                attack(getLocation().add(new Coordinate(1, -1)));
                attack(getLocation().add(new Coordinate(2, -2)));
                break;
            case RIGHT:
            case RIGHT_360:
                attack(getLocation().add(new Coordinate(1, 0)));
                attack(getLocation().add(new Coordinate(2, 0)));
                break;
            case DOWN_RIGHT:
                attack(getLocation().add(new Coordinate(1, 1)));
                attack(getLocation().add(new Coordinate(2, 2)));
                break;
            case DOWN:
                attack(getLocation().add(new Coordinate(0, 1)));
                attack(getLocation().add(new Coordinate(0, 2)));
                break;
            case DOWN_LEFT:
                attack(getLocation().add(new Coordinate(-1, 1)));
                attack(getLocation().add(new Coordinate(-2, 2)));
                break;
            case LEFT:
                attack(getLocation().add(new Coordinate(-1, 0)));
                attack(getLocation().add(new Coordinate(-2, 0)));
                break;
            case UP_LEFT:
                attack(getLocation().add(new Coordinate(-1, -1)));
                attack(getLocation().add(new Coordinate(-2, -2)));
                break;
        }
    }

    private void doSweepWeaponAttack(int direction){
        int dir = direction - 45;
        if (dir < 0 ) dir = 315;
        for (int ii = 0; ii < 3; ii++){
            doStrikeWeaponAttack(dir);
            dir += 45;
            if (dir >= 360) dir -= 360;
        }
    }

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public void setWeapon(Item weapon) {
        this.weapon = weapon;
    }

    public Item getWeapon() {
        if (weapon == null){
            Item item = new Item(new ItemStruct(-1, 1, "no_weapon", 0), gi);
            item.addTag(TagRegistry.WEAPON_STRIKE, item);
            return item;
        }
        return weapon;
    }

    @Override
    public void heal(int amount){
        health += Math.max(amount, 0);
        if (health > maxHealth) health = maxHealth;
    }

    @Override
    public void onLevelEnter() {
        super.onLevelEnter();
        lm.addLayer(swooshLayer);
    }

    @Override
    public void onLevelExit() {
        super.onLevelExit();
        lm.removeLayer(swooshLayer);
    }

    @Override
    void selfDestruct() {
        EntityStruct lootPileStruct = new EntityStruct(EntityRegistry.LOOT_PILE, "Loot Pile", null); //EntityRegistry.getEntityStruct(EntityRegistry.LOOT_PILE).getDisplayChar()
        for (Item item : getItems()){
            lootPileStruct.addItem(item.getItemData());
        }
        Entity pile = gi.instantiateEntity(lootPileStruct, getLocation(), gi.getCurrentLevel());
        pile.onLevelEnter();
        super.selfDestruct();
    }

    @Override
    public void scanInventory() {
        super.scanInventory();
        if (!getItems().contains(weapon)) weapon = null;
    }

    @Override
    public void updateInventory() {
        super.updateInventory();
        if (!getItems().contains(weapon)) weapon = null;
    }
}

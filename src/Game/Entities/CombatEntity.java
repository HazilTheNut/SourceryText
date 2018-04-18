package Game.Entities;

import Data.*;
import Engine.Layer;
import Engine.LayerManager;
import Engine.SpecialText;
import Game.GameInstance;
import Game.Item;
import Game.Registries.TagRegistry;
import Game.TagEvent;
import Game.Tags.Tag;

import java.awt.*;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Scanner;

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

    private Item weapon;
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
        ArrayList<EntityArg> args = new ArrayList<>();
        args.add(new EntityArg("interactText", "Having a conversation right now probably isn't a good idea."));
        args.add(new EntityArg("maxHealth", String.valueOf(defaultMaxHealth)));
        args.add(new EntityArg("strength",  String.valueOf(defaultStrength)));
        return args;
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

    protected EntityArg searchForArg(ArrayList<EntityArg> providedArgs, String name){
        for (EntityArg arg : providedArgs){
            if (arg.getArgName().equals(name))
                return arg;
        }
        return null;
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
        lm.addLayer(swooshLayer);
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

    private boolean attack(Coordinate loc){
        swooshLayer.setVisible(true);
        swooshLayer.setPos(loc);
        turnSleep(75);
        swooshLayer.setVisible(false);
        Entity entity = getGameInstance().getEntityAt(loc);
        if (entity != null && entity instanceof CombatEntity){
            doAttackEvent((CombatEntity)entity);
            return true;
        } else {
            if (getWeapon() != null)
                getWeapon().onContact(getGameInstance().getTileAt(loc), getGameInstance());
            else
                onContact(getGameInstance().getTileAt(loc), getGameInstance());
        }
        return false;
    }

    protected void doAttackEvent(CombatEntity ce){
        if (getWeapon() != null) {
            TagEvent event = new TagEvent(strength, true, this, ce, getGameInstance());
            for (Tag tag : getWeapon().getTags())
                tag.onDealDamage(event);
            if (event.eventPassed()) {
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
        System.out.printf("[CombatEntity.calculateMeleeDirection] Angle: %1$f dx: %2$d dy: %3$d\n", angle, dx, dy);
        for (int dir : directions){
            if (Math.abs(angle - dir) <= 22.5)
                return dir;
        }
        return -1;
    }

    protected void doWeaponAttack(Coordinate loc){
        for (Tag tag : getWeapon().getTags()) {
            switch (tag.getId()) {
                case TagRegistry.WEAPON_STRIKE:
                    doStrikeWeaponAttack(calculateMeleeDirection(loc));
                    return;
                case TagRegistry.WEAPON_THRUST:
                    doThrustWeaponAttack(calculateMeleeDirection(loc));
                    return;
                case TagRegistry.WEAPON_SWEEP:
                    doSweepWeaponAttack(calculateMeleeDirection(loc));
                    return;
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
            Item item = new Item(new ItemStruct(-1, 1, "no_weapon"));
            TagRegistry tr = new TagRegistry();
            item.addTag(tr.getTag(TagRegistry.WEAPON_STRIKE), item);
            return item;
        }
        return weapon;
    }

    @Override
    public void heal(int amount){
        health += Math.max(amount, 0);
        if (health > maxHealth) health = maxHealth;
    }

    protected void pathToPosition(Coordinate target, int range){
        currentPoints.clear();
        futurePoints.clear();
        futurePoints.add(new SpreadPoint(target.getX(), target.getY(), 0));
        doPathing(1, range);
    }

    private ArrayList<SpreadPoint> currentPoints = new ArrayList<>();
    private ArrayList<SpreadPoint> futurePoints = new ArrayList<>();

    private void doPathing(int n, int detectRange){
        if (n > detectRange) return;
        moveFutureToPresentPoints();
        for (SpreadPoint point : currentPoints){
            attemptFuturePoint(point.x+1, point.y, n);
            attemptFuturePoint(point.x-1, point.y, n);
            attemptFuturePoint(point.x, point.y+1, n);
            attemptFuturePoint(point.x, point.y-1, n);
        }
        for (SpreadPoint pt : futurePoints){
            if (pt.x == getLocation().getX() && pt.y == getLocation().getY()){
                for (SpreadPoint cp : currentPoints){
                    if (getLocation().stepDistance(new Coordinate(cp.x, cp.y)) <= 1){
                        teleport(new Coordinate(cp.x, cp.y));
                        return;
                    }
                }
            }
        }
        doPathing(n+1, detectRange);
    }

    private void moveFutureToPresentPoints(){
        for (SpreadPoint point : futurePoints) if (!currentPoints.contains(point)) {
            currentPoints.add(point);
        }
        futurePoints.clear();
    }

    private void attemptFuturePoint(int col, int row, int generation){
        if (getGameInstance().isSpaceAvailable(new Coordinate(col, row)) || getLocation().equals(new Coordinate(col, row))) futurePoints.add(new SpreadPoint(col, row, generation));
    }

    private class SpreadPoint{
        int x;
        int y;
        int g; //Shorthand for 'generation'
        private SpreadPoint(int x, int y, int g){
            this.x = x;
            this.y = y;
            this.g = g;
        }
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof SpreadPoint){
                SpreadPoint other = (SpreadPoint)obj;
                return x == other.x && y == other.y;
            }
            return false;
        }

        @Override
        public String toString() {
            return String.format("PathPoint:[%1$d,%2$d,%3$d]", x, y, g);
        }
    }
}

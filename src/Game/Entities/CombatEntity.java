package Game.Entities;

import Data.LayerImportances;
import Engine.Layer;
import Engine.SpecialText;
import Game.Coordinate;
import Game.Item;
import Game.Registries.TagRegistry;
import Game.TagEvent;
import Game.Tags.Tag;

import java.awt.*;

/**
 * Created by Jared on 3/28/2018.
 */
public class CombatEntity extends Entity{

    private int health;
    private int maxHealth;

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

    protected void setMaxHealth(int maxHP){
        maxHealth = maxHP;
        health = maxHP;
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
        Layer swooshLayer = new Layer(new SpecialText[1][1], getSprite().getName().concat("_attack"), loc.getX(), loc.getY(), LayerImportances.ANIMATION);
        swooshLayer.editLayer(0, 0, new SpecialText(' ', Color.WHITE, new Color(255, 255, 255, 150)));
        lm.addLayer(swooshLayer);
        turnSleep(75);
        lm.removeLayer(swooshLayer);
        Entity entity = getGameInstance().getEntityAt(loc);
        if (entity != null && entity instanceof CombatEntity){
            doAttackEvent((CombatEntity)entity);
            return true;
        }
        return false;
    }

    protected void doAttackEvent(CombatEntity ce){
        if (getWeapon() != null) {
            TagEvent event = new TagEvent(0, true, this, ce, getGameInstance());
            for (Tag tag : getWeapon().getTags())
                tag.onDealDamage(event);
            if (event.eventPassed()) {
                ce.receiveDamage(event.getAmount());
                getWeapon().decrementQty();
                if (getWeapon().getItemData().getQty() <= 0) {
                    setWeapon(null);
                }
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
        if (weapon != null) {
            for (Tag tag : weapon.getTags()) {
                switch (tag.getId()) {
                    case TagRegistry.WEAPON_STRIKE:
                        doStrikeWeaponAttack(calculateMeleeDirection(loc));
                        break;
                    case TagRegistry.WEAPON_THRUST:
                        doThrustWeaponAttack(calculateMeleeDirection(loc));
                        break;
                    case TagRegistry.WEAPON_SWEEP:
                        doSweepWeaponAttack(calculateMeleeDirection(loc));
                    default:
                        System.out.printf("[CombatEntity.doWeaponAttack] Tag: \'%1$s\' id: %2$d\n", tag.getName(), tag.getId());
                }
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
        return weapon;
    }

    @Override
    public void heal(int amount){
        health += Math.max(amount, 0);
        if (health > maxHealth) health = maxHealth;
    }
}

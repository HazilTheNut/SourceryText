package Game.Entities;

import Engine.SpecialText;

import java.awt.*;

/**
 * Created by Jared on 3/28/2018.
 */
public class CombatEntity extends Entity{

    protected int health;
    protected int maxHealth;

    protected void setMaxHealth(int maxHP){
        maxHealth = maxHP;
        health = maxHP;
    }

    public void receiveDamage(int amount) {
        health -= amount;
        double percentage = Math.max(Math.min(amount / maxHealth, 1),0);
        SpecialText originalSprite = getSprite().getSpecialText(0, 0);
        getSprite().editLayer(0, 0, new SpecialText(originalSprite.getCharacter(), originalSprite.getFgColor(), new Color(255, 0, 0, (int)(255*percentage))));
        turnSleep(500);
        getSprite().editLayer(0, 0, originalSprite);
        System.out.printf("Health remaining: %1$d\n", health);
    }

    public void heal(int amount){
        health += amount;
    }
}

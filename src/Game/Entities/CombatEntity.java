package Game.Entities;

import Engine.SpecialText;

import java.awt.*;

/**
 * Created by Jared on 3/28/2018.
 */
public class CombatEntity extends Entity{

    protected int health;
    protected int maxHealth;

    void setMaxHealth(int maxHP){
        maxHealth = maxHP;
        health = maxHP;
    }

    public void receiveDamage(int amount) {
        health -= amount;
        double percentage = Math.sqrt(Math.max(Math.min((double)amount / maxHealth, 1),0.1));
        SpecialText originalSprite = getSprite().getSpecialText(0, 0);
        getSprite().editLayer(0, 0, new SpecialText(originalSprite.getCharacter(), originalSprite.getFgColor(), new Color(255, 0, 0, (int)(255*percentage))));
        turnSleep(250);
        getSprite().editLayer(0, 0, originalSprite);
        if (health <= 0) selfDestruct();
    }

    public void heal(int amount){
        health += amount;
    }
}

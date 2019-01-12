package Game;

import Data.Coordinate;
import Engine.SpecialText;
import Game.Entities.CombatEntity;
import Game.Entities.Entity;

public class ArrowProjectile extends ItemProjectile {

    public ArrowProjectile(Entity creator, Coordinate target, SpecialText icon, Item item, int damage) {
        super(creator, target, icon, item, damage);
    }

    @Override
    protected SpecialText getIcon(SpecialText baseIcon) {
        double angle = 180 * Math.atan2(normalizedVelocityY, normalizedVelocityX) / Math.PI;
        if (angle < 0) angle += 360;
        if (Math.abs(angle) <= 22.5 || Math.abs(angle - 360) <= 22.5 || Math.abs(angle - 180) <= 22.5){
            return makeIcon('-', baseIcon);
        }
        if (Math.abs(angle - 45) <= 22.5 || Math.abs(angle - 225) <= 22.5){
            return makeIcon('\\', baseIcon); //This looks wrong, but somehow it came out like this. And it works, too.
        }
        if (Math.abs(angle - 90) <= 22.5 || Math.abs(angle - 270) <= 22.5){
            return makeIcon('|', baseIcon);
        }
        if (Math.abs(angle - 135) <= 22.5 || Math.abs(angle - 315) <= 22.5){
            return makeIcon('/', baseIcon);
        }
        return baseIcon;
    }

    private SpecialText makeIcon(char c, SpecialText baseIcon){
        return new SpecialText(c, colorateWithTags(baseIcon.getFgColor()), baseIcon.getBkgColor());
    }
}

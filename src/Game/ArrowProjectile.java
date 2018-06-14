package Game;

import Data.Coordinate;
import Engine.LayerManager;
import Engine.SpecialText;
import Game.Entities.Entity;

public class ArrowProjectile extends Projectile {

    public ArrowProjectile(Entity creator, Coordinate target, SpecialText icon, LayerManager lm) {
        super(creator, target, icon, lm);
    }

    @Override
    protected SpecialText getIcon(SpecialText baseIcon) {
        double angle = 180 * Math.atan2(yvelocity, xvelocity) / Math.PI;
        if (angle < 0) angle += 360;
        if (Math.abs(angle) <= 22.5 || Math.abs(angle - 360) <= 22.5 || Math.abs(angle - 180) <= 22.5){
            return new SpecialText('-', baseIcon.getFgColor(), baseIcon.getBkgColor());
        }
        if (Math.abs(angle - 45) <= 22.5 || Math.abs(angle - 225) <= 22.5){
            return new SpecialText('\\', baseIcon.getFgColor(), baseIcon.getBkgColor()); //This looks wrong, but somehow it came out like this. And it works, too.
        }
        if (Math.abs(angle - 90) <= 22.5 || Math.abs(angle - 270) <= 22.5){
            return new SpecialText('|', baseIcon.getFgColor(), baseIcon.getBkgColor());
        }
        if (Math.abs(angle - 135) <= 22.5 || Math.abs(angle - 315) <= 22.5){
            return new SpecialText('/',baseIcon.getFgColor(), baseIcon.getBkgColor());
        }
        return baseIcon;
    }
}

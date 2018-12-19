package Game.Entities.PuzzleElements;

import Data.Coordinate;
import Data.EntityArg;
import Data.EntityStruct;
import Engine.LayerManager;
import Engine.SpecialText;
import Game.Entities.Entity;
import Game.FrameDrawListener;
import Game.GameInstance;
import Game.Projectile;
import Game.ProjectileListener;

import java.util.ArrayList;

public class Fan extends Entity implements Powerable, FrameDrawListener, ProjectileListener {

    private int fanStrength = 0;
    private boolean active = false;

    @Override
    public void initialize(Coordinate pos, LayerManager lm, EntityStruct entityStruct, GameInstance gameInstance) {
        super.initialize(pos, lm, entityStruct, gameInstance);
        fanStrength = readIntArg(searchForArg(entityStruct.getArgs(), "Strength"), 1);
        animationFrame = (byte)readIntArg(searchForArg(entityStruct.getArgs(), "startFrame"), 0);
        active = readBoolArg(searchForArg(entityStruct.getArgs(), "defaultOn"), false);
        generateIcon();
        gi.getCurrentLevel().addFrameDrawListener(this);
        gi.getCurrentLevel().addProjectileListener(this);
    }

    @Override
    public ArrayList<EntityArg> generateArgs() {
        ArrayList<EntityArg> args = super.generateArgs();
        args.add(new EntityArg("Strength", "1"));
        args.add(new EntityArg("startFrame", "0"));
        args.add(new EntityArg("defaultOn", "false"));
        args.add(new EntityArg("direction", "NORTH, SOUTH, EAST, WEST"));
        return args;
    }

    @Override
    public void onPowerOff() {
        active = !active;
    }

    @Override
    public void onPowerOn() {
        active = !active;
    }

    private int getFanStrength(){
        if (active) return fanStrength;
        else return 0;
    }

    private byte animationFrame = 0;
    private int subFrame = 0; //Represents the time spent in between frames, as the fan should not spin at 5rps (instead it spins at variable speed based on fan strength
    private char[] animation = {'/', '-', '\\', '|'};

    @Override
    public void onFrameDraw() {
        if (getFanStrength() <= 0) return;
        subFrame++;
        if (subFrame >= 5 - fanStrength) {
            animationFrame++;
            generateIcon();
            subFrame = 0;
        }
    }

    private void generateIcon(){
        if (animationFrame >= animation.length) animationFrame -= animation.length;
        SpecialText icon = getSprite().getSpecialText(0, 0);
        setIcon(new SpecialText(animation[animationFrame], icon.getFgColor(), icon.getBkgColor()));
        updateSprite();
    }

    @Override
    public void onProjectileFly(Projectile projectile) {

    }
}

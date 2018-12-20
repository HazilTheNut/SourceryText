package Game.Entities.PuzzleElements;

import Data.Coordinate;
import Data.EntityStruct;
import Data.SerializationVersion;
import Engine.LayerManager;
import Engine.SpecialText;
import Game.GameInstance;
import Game.Projectile;
import Game.ProjectileListener;

import java.awt.*;

public class Photogate extends GenericPowerSource implements ProjectileListener {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    private boolean currentlyActive = false;
    private SpecialText originalIcon;

    @Override
    public void initialize(Coordinate pos, LayerManager lm, EntityStruct entityStruct, GameInstance gameInstance) {
        super.initialize(pos, lm, entityStruct, gameInstance);
        originalIcon = getSprite().getSpecialText(0, 0).copy();
        gi.getCurrentLevel().addProjectileListener(this::onProjectileFly);
    }

    @Override
    public boolean isSolid() {
        return false;
    }

    @Override
    public void onProjectileFly(Projectile projectile) {
        if (projectile.getRoundedPos().equals(getLocation())){
            powerOn();
            setIcon(new SpecialText(originalIcon.getCharacter(), new Color(255, 255, originalIcon.getFgColor().getBlue()), originalIcon.getBkgColor()));
            updateSprite();
            currentlyActive = true;
        }
    }

    @Override
    public void onTurn() {
        if (currentlyActive) {
            powerOff();
            setIcon(originalIcon);
            updateSprite();
        }
        currentlyActive = false;
    }
}
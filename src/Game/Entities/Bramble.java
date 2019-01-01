package Game.Entities;

import Data.Coordinate;
import Data.EntityStruct;
import Data.SerializationVersion;
import Engine.LayerManager;
import Engine.SpecialText;
import Game.GameInstance;

import java.awt.*;

public class Bramble extends CombatEntity {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    private int lifetime;
    private final float LIFETIME_START = 15;
    private SpecialText originalIcon;

    @Override
    public void initialize(Coordinate pos, LayerManager lm, EntityStruct entityStruct, GameInstance gameInstance) {
        super.initialize(pos, lm, entityStruct, gameInstance);
        originalIcon = getSprite().getSpecialText(0,0);
        lifetime = -1;
    }

    public void resetLifetime(){
        lifetime = (int)LIFETIME_START;
        generateIcon();
    }

    @Override
    public void onTurn() {
        if (lifetime > 0) {
            lifetime--;
            generateIcon();
        }
        if (lifetime == 0) selfDestruct();
        super.onTurn();
    }

    private void generateIcon(){
        float scalar = (lifetime > 0) ? 0.2f + (0.75f * (lifetime / LIFETIME_START)) : 1;
        setIcon(new SpecialText(originalIcon.getCharacter(), scaleColor(originalIcon.getFgColor(), scalar), scaleColor(originalIcon.getBkgColor(), scalar)));
        updateSprite();
    }

    private Color scaleColor(Color color, float scalar){
        return new Color((int)(color.getRed() * scalar), (int)(color.getGreen() * scalar), (int)(color.getBlue() * scalar));
    }
}

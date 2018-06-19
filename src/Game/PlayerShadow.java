package Game;

import Data.Coordinate;
import Data.EntityStruct;
import Data.SerializationVersion;
import Engine.LayerManager;
import Engine.SpecialText;
import Game.Entities.CombatEntity;
import Game.Registries.TagRegistry;

import java.awt.*;

public class PlayerShadow extends CombatEntity {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    private Coordinate offset;

    @Override
    public void initialize(Coordinate pos, LayerManager lm, EntityStruct entityStruct, GameInstance gameInstance) {
        super.initialize(pos, lm, entityStruct, gameInstance);
        setMaxHealth(1);
        getSprite().editLayer(0, 0, new SpecialText('@', new Color(65, 75, 65), new Color(0, 0, 0, 25)));
    }

    @Override
    public void onTurn() {
        if (offset != null) {
            super.onTurn();
            Player player = gi.getPlayer();
            Coordinate pos = player.getLocation().add(offset);
            if (gi.isSpaceAvailable(pos, TagRegistry.TILE_WALL)) {
                setPos(pos);
                if (player.getWeapon().getItemData().getItemId() > 0) setWeapon(player.getWeapon());
                else setWeapon(noWeapon);
            } else {
                selfDestruct();
            }
        }
    }

    @Override
    public boolean isSolid() {
        return false;
    }

    public void setOffset(Coordinate offset) {
        this.offset = offset;
    }
}

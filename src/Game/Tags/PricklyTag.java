package Game.Tags;

import Data.Coordinate;
import Data.SerializationVersion;
import Game.*;
import Game.Entities.Entity;
import Game.Registries.TagRegistry;

public class PricklyTag extends Tag {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    @Override
    public void onTurn(TagEvent e) {
        if (e.getTagOwner() instanceof Entity) {
            Entity tagOwner = (Entity) e.getTagOwner();
            bePrickly(tagOwner.getLocation(), tagOwner.getGameInstance().getCurrentLevel(), tagOwner, e.getGameInstance());
        } else if (e.getTagOwner() instanceof Tile) {
            Tile tagOwner = (Tile) e.getTagOwner();
            bePrickly(tagOwner.getLocation(), tagOwner.getLevel(), tagOwner, e.getGameInstance());
        }
    }

    private void bePrickly(Coordinate pos, Level level, TagHolder source, GameInstance gi){
        damageAt(pos, level, source, gi);
        damageAt(pos.add(new Coordinate(1, 0)), level, source, gi);
        damageAt(pos.add(new Coordinate(-1, 0)), level, source, gi);
        damageAt(pos.add(new Coordinate(0, -1)), level, source, gi);
        damageAt(pos.add(new Coordinate(0, 1)), level, source, gi);
    }

    private void damageAt(Coordinate pos, Level level, TagHolder source, GameInstance gi){
        for (Entity e : level.getEntitiesAt(pos))
            if (!e.hasTag(TagRegistry.PRICKLY))
                e.onReceiveDamage(2, source, gi);
    }

    /*

        A small discussion:

        The code above makes it look like the prickly tag owner should be damaging anything that gets close it every turn - and you would be right.
        However, for Tiles, the Tile usually does not exist (due to levels being memory efficient).
        Entities have to specifically run into the prickly Tile for the Level to spontaneously generate the Tile, causing this Tag to get a turn update, and consequently damaging the entities.
        It makes sense for the player, but all enemies will never attempt to path into walls, so prickly tiles can't be used strategically against them.

        The code below makes everything above work in the best manner.
    */

    @Override
    public boolean isTileRemovable(Tile tile) {
        return false;
    }
}

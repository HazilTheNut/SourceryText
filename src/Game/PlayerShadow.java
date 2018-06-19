package Game;

import Data.Coordinate;
import Data.EntityStruct;
import Data.ItemStruct;
import Data.SerializationVersion;
import Engine.LayerManager;
import Engine.SpecialText;
import Game.Entities.CombatEntity;
import Game.Entities.Entity;
import Game.Registries.ItemRegistry;
import Game.Registries.TagRegistry;
import Game.Spells.Spell;
import Game.Tags.RangeTag;
import Game.Tags.Tag;

import java.awt.*;

public class PlayerShadow extends CombatEntity implements PlayerActionCollector {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    private Coordinate offset;
    private Player player;

    private Projectile toFire;
    private int fireTimer = 0;

    private Spell copiedSpell;

    @Override
    public void initialize(Coordinate pos, LayerManager lm, EntityStruct entityStruct, GameInstance gameInstance) {
        super.initialize(pos, lm, entityStruct, gameInstance);
        setMaxHealth(1);
        getSprite().editLayer(0, 0, new SpecialText('@', new Color(65, 75, 65), new Color(0, 0, 0, 25)));
        player = gameInstance.getPlayer();
        player.addPlayerActionCollector(this);
    }

    @Override
    public void onTurn() {
        if (offset != null) {
            super.onTurn();
            if (toFire != null){
                if (fireTimer == 0) {
                    RangeTag rangeTag = (RangeTag) getWeapon().getTag(TagRegistry.RANGE_START);
                    if (rangeTag == null)
                        toFire.launchProjectile(RangeTag.RANGE_DEFAULT);
                    else
                        toFire.launchProjectile(rangeTag.getRange());
                    getWeapon().decrementQty();
                    toFire = null;
                } else {
                    fireTimer--;
                }
            }
            Coordinate pos = player.getLocation().add(offset);
            if (gi.isSpaceAvailable(pos, TagRegistry.TILE_WALL)) {
                setPos(pos);
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

    @Override
    protected void fireArrowProjectile(Projectile arrow) {
        toFire = arrow;
        fireTimer = 1;
    }

    @Override
    public void selfDestruct() {
        super.selfDestruct();
        player.removePlayerActionCollector(this);
    }

    @Override
    public void onLevelExit() {
        super.onLevelExit();
        selfDestruct();
    }

    @Override
    public void onPlayerAttack(Coordinate loc, Item weapon) {
        ItemStruct copyStruct = new ItemStruct(weapon.getItemData().getItemId(), 1, weapon.getItemData().getName(), weapon.calculateWeight());
        Item copy = new Item(copyStruct, gi);
        for (Tag tag : weapon.getTags()) copy.addTag(tag.getId(), copy);
        setWeapon(copy);
        if (copy.hasTag(TagRegistry.WEAPON_BOW)) addItem(ItemRegistry.generateItem(ItemRegistry.ID_ARROW, gi));
        doWeaponAttack(loc.add(offset));
    }

    @Override
    public void onPlayerMove(Coordinate loc) {

    }

    @Override
    public void onPlayerReadySpell(Coordinate loc, Spell spell) {
        copiedSpell = spell.copy();
        copiedSpell.readySpell(loc.add(offset), this, gi, player.getMagicPower());
    }

    @Override
    public void onPlayerCastSpell(Coordinate loc, Spell spell) {
        if (copiedSpell != null)
            copiedSpell.castSpell(loc.add(offset), this, gi, player.getMagicPower());
    }

    @Override
    public void onPlayerInteract(Coordinate loc) {
        Entity e = gi.getCurrentLevel().getEntitiesAt(loc.add(offset)).get(0);
        if (e != null)
            e.onInteract(player);
    }
}

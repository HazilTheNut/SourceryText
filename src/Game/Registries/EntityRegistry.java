package Game.Registries;

import Data.EntityStruct;
import Engine.SpecialText;
import Game.Entities.*;
import Game.Entities.PuzzleElements.*;
import Game.PlayerShadow;

import java.awt.*;
import java.util.Set;
import java.util.TreeMap;

/**
 * Created by Jared on 3/3/2018.
 */
public class EntityRegistry {

    private static TreeMap<Integer, EntityStruct> entityStructMap = new TreeMap<>();
    private static TreeMap<Integer, Class> entityObjMap = new TreeMap<>();

    public static final int LOOT_PILE = 7;
    public static final int PLAYER_SHADOW = 20;
    public static final int MAGIC_BOMB = 17;
    public static final int RAFT = 18;
    public static final int BRAMBLE = 24;

    /*
    * Some Notes:
    *
    * Adding the NO_PATHING tag to an entity effectively does nothing. It's only there to communicate whether or not the object is solid.
    *
    * */

    static {
        //Registering stuff starts here

        registerEntity(0,  "Dummy",        new SpecialText('D'),                                                         Dummy.class, TagRegistry.FLAMMABLE, TagRegistry.NO_PATHING, TagRegistry.LIVING);
        registerEntity(1,  "Save Point",   new SpecialText('S', new Color(40, 225, 115), new Color(20, 100, 80, 40)),    SavePoint.class, TagRegistry.NO_PATHING, TagRegistry.IMMOVABLE);
        registerEntity(2,  "Basic Enemy",  new SpecialText('E', new Color(255, 130, 130), new Color(255, 180, 180, 15)), BasicEnemy.class, TagRegistry.FLAMMABLE, TagRegistry.LIVING);
        registerEntity(3,  "Sign",         new SpecialText('S', new Color(110, 100, 250), new Color(55, 50, 125, 30)),   Sign.class, TagRegistry.NO_PATHING, TagRegistry.FLAMMABLE);
        registerEntity(4,  "Chest",        new SpecialText('C', new Color(245, 245, 175), new Color(175, 100,  35, 45)), Chest.class, TagRegistry.NO_PATHING);
        registerEntity(5,  "Door",         new SpecialText('-', new Color(143, 74, 17),   new Color(75, 45, 10, 50)),    Door.class, TagRegistry.NO_PATHING, TagRegistry.IMMOVABLE);
        registerEntity(6,  "Locked Door",  new SpecialText('-', new Color(143, 123, 107), new Color(74, 65, 55, 50)),    LockedDoor.class, TagRegistry.NO_PATHING, TagRegistry.IMMOVABLE);
        registerEntity(LOOT_PILE, "Loot",  new SpecialText('%', new Color(191, 191, 75),  new Color(155, 155, 60, 15)),  LootPile.class);
        registerEntity(8,  "One-Way Door", new SpecialText('x', new Color(143, 123, 107), new Color(74, 65, 55, 50)),    OneWayDoor.class, TagRegistry.NO_PATHING, TagRegistry.IMMOVABLE);
        registerEntity(9,  "Magnet",       new SpecialText('M', new Color(145, 145, 145), new Color(45, 45, 45, 45)),    Magnet.class, TagRegistry.METALLIC);
        registerEntity(10, "Character",    new SpecialText('C', new Color(130, 255, 225), new Color(180, 255, 225, 15)), GameCharacter.class, TagRegistry.FLAMMABLE, TagRegistry.LIVING, TagRegistry.NO_PATHING);

        registerEntity(11, "Floor Switch", new SpecialText('o', new Color(165, 165, 135), new Color(70, 70, 50, 75)),    FloorSwitch.class, TagRegistry.IMMOVABLE);
        registerEntity(12, "Powered Door", new SpecialText('-', new Color(143, 123, 80),  new Color(74, 65, 55, 75)),    PoweredDoor.class, TagRegistry.IMMOVABLE);
        registerEntity(13, "Large Crate",  new SpecialText('#', new Color(120, 75, 34),   new Color(56, 32, 12, 100)),   MovableCrate.class, TagRegistry.FLAMMABLE, TagRegistry.NO_PATHING);
        registerEntity(14, "Toggle Switch",new SpecialText('*', new Color(42,  42,  86),  new Color(20, 20, 51, 100)),   ToggleSwitch.class, TagRegistry.IMMOVABLE, TagRegistry.NO_PATHING);
        registerEntity(15, "Room Cover",   new SpecialText(' ', new Color(42,  42,  86),  Color.BLACK),                  RoomCover.class,   TagRegistry.IMMOVABLE);
        registerEntity(16, "Wall Torch",   new SpecialText('*', new Color(212, 195, 140), new Color(142, 120, 50, 50)),  WallTorch.class, TagRegistry.FLAMMABLE, TagRegistry.BURN_FOREVER, TagRegistry.BURN_NOSPREAD, TagRegistry.ON_FIRE);
        registerEntity(MAGIC_BOMB, "MagicBomb", new SpecialText('b'),                                                    MagicBomb.class, TagRegistry.LIVING, TagRegistry.NO_PATHING);
        registerEntity(RAFT, "Raft",       new SpecialText('=', new Color(142, 94, 60),   new Color(71, 47, 30, 240)),   Marker.class);
        registerEntity(19, "Store Item",   new SpecialText('$', new Color(171, 201, 75),  new Color(145, 165, 60, 15)),  StoreItem.class);
        registerEntity(PLAYER_SHADOW, "Player Shadow", new SpecialText('@', new Color(65, 75, 65), new Color(0, 0, 0, 25)), PlayerShadow.class, TagRegistry.ETHEREAL);

        registerEntity(21, "Patrolling Character", new SpecialText('G', new Color(130, 255, 225), new Color(180, 255, 225, 15)), PatrollingCharacter.class, TagRegistry.FLAMMABLE, TagRegistry.LIVING, TagRegistry.NO_PATHING);
        registerEntity(22, "Fan",          new SpecialText('/', new Color(150, 150, 200), new Color(100, 100, 125, 50)), Fan.class, TagRegistry.NO_PATHING, TagRegistry.METALLIC, TagRegistry.IMMOVABLE);
        registerEntity(23, "Photogate",    new SpecialText('!', new Color(42,  42,  86),  new Color(20, 20, 51, 100)),   Photogate.class, TagRegistry.IMMOVABLE);
        registerEntity(BRAMBLE, "Bramble", new SpecialText('#', new Color(126, 255, 109), new Color(82, 230, 91, 75)),   Bramble.class, TagRegistry.FLAMMABLE, TagRegistry.PRICKLY, TagRegistry.NO_PATHING, TagRegistry.BURN_SPREAD);

        //Registering stuff ends here
    }

    public static int[] getMapKeys() {
        Set<Integer> ints = entityStructMap.keySet();
        int[] output = new int[ints.size()];
        int index = 0;
        for (int i : ints){
            output[index] = i;
            index++;
        }
        return output;
    }

    public static EntityStruct getEntityStruct (int id) { return entityStructMap.get(id).copy(); }

    public static Class getEntityClass (int id) { return entityObjMap.get(id); }

    private static void registerEntity(int id, String name, SpecialText text, Class entityClass, int... tags){
        entityStructMap.put(id, new EntityStruct(id, name, text, tags));
        if (entityClass != null)
            entityObjMap.put(id, entityClass);
        else
            entityObjMap.put(id, null);
    }
}

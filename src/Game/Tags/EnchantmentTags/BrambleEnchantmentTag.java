package Game.Tags.EnchantmentTags;

import Data.Coordinate;
import Data.SerializationVersion;
import Game.Entities.Bramble;
import Game.Entities.Entity;
import Game.GameInstance;
import Game.Registries.EntityRegistry;
import Game.Registries.TagRegistry;
import Game.TagEvent;
import Game.Tags.Tag;
import Game.Tile;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

public class BrambleEnchantmentTag extends Tag {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    private int cooldown;
    private final int COOLDOWN_START = 10;

    @Override
    public void onContact(TagEvent e) {
        if (cooldown <= 0) {
            Coordinate seed = null;
            if (e.getTarget() instanceof Tile) {
                seed = ((Tile)e.getTarget()).getLocation();
            } else if (e.getTarget() instanceof Entity) {
                seed = ((Entity)e.getTarget()).getLocation();
            }
            if (seed != null) {
                placeBramble(seed, e.getGameInstance());
                growBrambles(seed, e.getGameInstance());
                cooldown = COOLDOWN_START;
            }
        }
    }

    private void growBrambles(Coordinate origin, GameInstance gi){
        //Constants
        int GROWTH_TOTAL_CYCLES = 3;
        int BRAMBLES_PER_GROWTH = 2;
        int TOTAL_BRAMBLES = 10;

        ArrayList<Coordinate> growPoints = new ArrayList<>(); //List of points of brambles to create
        ArrayList<Coordinate> nextPoints = new ArrayList<>(); //List of points to be used in the following growth cycle
        Coordinate[] spreadVectors = { new Coordinate(1, 0), new Coordinate(-1, 0), new Coordinate(0, 1), new Coordinate(0, -1)};
        growPoints.add(origin);
        Random random = new Random();
        int totalBushes = 0;
        for (int i = 0; i < GROWTH_TOTAL_CYCLES; i++) {
            nextPoints.clear();
            try {
                Thread.sleep(100); //Makes it animated
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            for (Coordinate pt : growPoints){
                //I want two brambles per growth point, so it is repeated twice
                int numTries = 0;
                for (int j = 0; j < BRAMBLES_PER_GROWTH; j++) {
                    numTries++;
                    Coordinate placePoint = pt.add(spreadVectors[random.nextInt(spreadVectors.length)]);
                    if (placeBramble(placePoint, gi)) nextPoints.add(placePoint);
                    else if (numTries < 5) j--; //I want brambles to grow more predictably, so it has some patience before moving on
                }
            }
            growPoints.clear();
            growPoints.addAll(nextPoints);
            totalBushes += nextPoints.size();
            if (totalBushes > TOTAL_BRAMBLES)
                return;
        }
    }

    @Override
    public void onTurn(TagEvent e) {
        if (cooldown > 0) cooldown--;
    }

    /**
     * Instantiates a Bramble at a specified location
     *
     * @param loc The location to instantiate the bramble
     * @param gi The GameInstance running the game
     * @return If a bramble was successfully placed
     */
    private boolean placeBramble(Coordinate loc, GameInstance gi){
        if (gi.isSpaceAvailable(loc, TagRegistry.NO_PATHING)) {
            Bramble bramble = (Bramble) gi.instantiateEntity(EntityRegistry.getEntityStruct(EntityRegistry.BRAMBLE), loc, gi.getCurrentLevel());
            bramble.resetLifetime();
            bramble.setMaxHealth(10);
            return true;
        }
        return false;
    }

    @Override
    public Color getTagColor() {
        if (cooldown <= 0)
            return EnchantmentColors.BRAMBLE;
        else {
            return new Color(EnchantmentColors.BRAMBLE.getRed(), EnchantmentColors.BRAMBLE.getGreen(), EnchantmentColors.BRAMBLE.getBlue(), 20);
        }
    }

    @Override
    public String getName() {
        if (cooldown <= 0)
            return super.getName();
        else {
            return "Recharging [" + cooldown + ']';
        }
    }
}

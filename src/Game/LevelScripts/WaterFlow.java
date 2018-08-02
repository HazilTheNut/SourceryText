package Game.LevelScripts;

import Data.Coordinate;

import java.util.ArrayList;

public class WaterFlow extends LevelScript {

    @Override
    public String[] getMaskNames() {
        return new String[]{"North", "South", "East", "West"};
    }

    public ArrayList<Coordinate> getBannedRaftDirections(Coordinate pos){
        ArrayList<Coordinate> bannedVectors = new ArrayList<>();
        if (getMaskDataAt("North", pos))
            bannedVectors.add(new Coordinate(0, 1));
        if (getMaskDataAt("South", pos))
            bannedVectors.add(new Coordinate(0, -1));
        if (getMaskDataAt("East", pos))
            bannedVectors.add(new Coordinate(-1, 0));
        if (getMaskDataAt("West", pos))
            bannedVectors.add(new Coordinate(1, 0));
        return bannedVectors;
    }

    private Coordinate playerPrevPos;

    @Override
    public void onTurnStart() {
        if (gi.getPlayer().getLocation().equals(playerPrevPos) && gi.getPlayer().isOnRaft()){
            for (Coordinate vector : getBannedRaftDirections(gi.getPlayer().getLocation()))
                gi.getPlayer().teleport(gi.getPlayer().getLocation().add(vector.multiply(-1)));
        }
        playerPrevPos = gi.getPlayer().getLocation().copy();
    }
}

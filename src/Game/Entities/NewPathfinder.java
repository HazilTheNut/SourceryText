package Game.Entities;

import Data.Coordinate;
import Data.SerializationVersion;
import Engine.SpecialText;
import Game.Debug.DebugWindow;
import Game.Registries.TagRegistry;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

public class NewPathfinder extends BasicEnemy implements Serializable {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    @Override
    protected void pathToPlayer() {
        boolean movementFound = false;
        long startTime = System.nanoTime();
        processedPoints.clear();
        openPoints.clear();
        nextPoints.clear();
        nextPoints.add(new PathPoint(target.getLocation(), target.getLocation().stepDistance(getLocation()), 0));
        ArrayList<Coordinate> movementOptions = new ArrayList<>();
        while(!movementFound){
            //Rebuild the set of open points to process
            updateOpenPoints();
            //Start from end of list (with smallest distances)
            int lowestDistance = openPoints.get(openPoints.size()-1).distanceToTarget; //The list of 'open' points is already sorted by distance to the target position.
            for (int i = 0; i < openPoints.size();) {
                PathPoint pt = openPoints.get(i);
                if (pt.distanceToTarget == 1){
                    movementFound = true;
                    movementOptions.add(pt.pos);
                    gi.getPathTestLayer().editLayer(pt.pos, new SpecialText(' ', Color.WHITE, new Color(55, 255, 255, 150)));
                    i++;
                } else if (pt.distanceToTarget == lowestDistance){ //Prioritize the points that are closest to the target position.
                    processedPoints.add(pt);
                    attemptNextPoint(pt.pos.add(new Coordinate(1, 0)), getLocation(), pt);
                    attemptNextPoint(pt.pos.add(new Coordinate(0, 1)), getLocation(), pt);
                    attemptNextPoint(pt.pos.add(new Coordinate(-1, 0)), getLocation(), pt);
                    attemptNextPoint(pt.pos.add(new Coordinate(0, -1)), getLocation(), pt);
                    openPoints.remove(i);
                } else {
                    i++;
                }
            }
        }
        if (movementOptions.size() == 1) teleport(movementOptions.get(0));
        if (movementOptions.size() > 1) {
            Random random = new Random();
            teleport(movementOptions.get(random.nextInt(movementOptions.size())));
        }
        DebugWindow.reportf(DebugWindow.STAGE, "NewPathfinder.pathToPlayer", "Time to solve: %1$fms", (System.nanoTime() - startTime) / 1000000f);
    }

    private void updateOpenPoints(){
        for (PathPoint nextPoint : nextPoints){
            boolean pointInserted = false;
            for (int i = 0; i < openPoints.size(); i++) {
                if (openPoints.get(i).distanceToTarget < nextPoint.distanceToTarget){
                    openPoints.add(i, nextPoint);
                    pointInserted = true;
                    i = openPoints.size(); //Ends this loop
                }
            }
            if (!pointInserted) openPoints.add(nextPoint);
        }
        nextPoints.clear();
    }

    private void attemptNextPoint(Coordinate pos, Coordinate endPos, PathPoint source){
        PathPoint nextPoint = new PathPoint(pos, endPos.stepDistance(pos), source.generation + 1);
        if (gi.isSpaceAvailable(pos, TagRegistry.NO_PATHING) && !nextPoints.contains(nextPoint) && !processedPoints.contains(nextPoint) && !openPoints.contains(nextPoint)){
            nextPoints.add(nextPoint);
            gi.getPathTestLayer().editLayer(pos, new SpecialText(' ', Color.WHITE, testColors[nextPoint.generation % 9]));
        }
    }

    private transient ArrayList<PathPoint> processedPoints = new ArrayList<>(); //Points not to check.
    private transient ArrayList<PathPoint> openPoints = new ArrayList<>(); //Points to check
    private transient ArrayList<PathPoint> nextPoints = new ArrayList<>(); //The next set of points to check.

    private Color[] testColors = {
            new Color(100, 50, 50, 150),
            new Color(99, 80, 50, 150),
            new Color(87, 97, 49, 150),
            new Color(59, 97, 49, 150),
            new Color(49, 97, 68, 150),
            new Color(49, 97, 97, 150),
            new Color(49, 68, 97, 150),
            new Color(60, 49, 97, 150),
            new Color(87, 49, 97, 150)
    };

    private class PathPoint {

        Coordinate pos;
        int distanceToTarget;
        int generation;

        private PathPoint(Coordinate loc, int dist, int g){
            pos = loc;
            distanceToTarget = dist;
            generation = g;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof PathPoint) {
                PathPoint pathPoint = (PathPoint) obj;
                return pathPoint.pos.equals(pos);
            }
            return false;
        }
    }
}

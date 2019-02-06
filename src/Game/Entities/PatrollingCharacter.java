package Game.Entities;

import Data.Coordinate;
import Data.EntityArg;
import Data.EntityStruct;
import Data.SerializationVersion;
import Engine.LayerManager;
import Game.Debug.DebugWindow;
import Game.GameInstance;
import Game.LevelScripts.LightingEffects;

import java.util.ArrayList;

public class PatrollingCharacter extends GameCharacter {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    private ArrayList<Coordinate> patrolPath;
    private int patrolPathPointer;

    private double direction;
    private final double NORTH = Math.PI * -0.5;
    private final double SOUTH = Math.PI * 0.5;
    private final double EAST  = Math.PI * 0;
    private final double WEST  = Math.PI * 1;

    @Override
    public ArrayList<EntityArg> generateArgs() {
        ArrayList<EntityArg> args = super.generateArgs();
        args.add(new EntityArg("patrolPath","[0,0],[0,0],[0,0],..."));
        return args;
    }

    @Override
    public void initialize(Coordinate pos, LayerManager lm, EntityStruct entityStruct, GameInstance gameInstance) {
        super.initialize(pos, lm, entityStruct, gameInstance);
        patrolPath = readCoordListArg(searchForArg(entityStruct.getArgs(), "patrolPath"));
        patrolPath.add(0, getLocation().copy());
        patrolPathPointer = 0;
    }

    private boolean shouldMove = false;

    @Override
    protected void doIdleBehavior() {
        super.doIdleBehavior();
        if (getLocation().equals(patrolPath.get(patrolPathPointer))){
            patrolPathPointer++;
            if (patrolPathPointer >= patrolPath.size()) patrolPathPointer = 0;
        } else {
            if (shouldMove) //Moves every other turn
                pathToPosition(patrolPath.get(patrolPathPointer));
            shouldMove = !shouldMove;
        }
    }

    @Override
    protected void wanderTo(Coordinate pos) {
        if (mentalState == STATE_SEARCHING) super.wanderTo(pos);
    }

    @Override
    protected void move(int relativeX, int relativeY) {
        super.move(relativeX, relativeY);
        //Obtain target direction to point the light
        Coordinate vector = new Coordinate(relativeX, relativeY);
        double targetDir = 0;
        if (vector.equals(new Coordinate(0, -1)))
            targetDir = NORTH;
        else if (vector.equals(new Coordinate(0, 1)))
            targetDir = SOUTH;
        else if (vector.equals(new Coordinate(1, 0)))
            targetDir = EAST;
        else if (vector.equals(new Coordinate(-1, 0)))
            targetDir = WEST;
        //Clean up the current and target directions so that the math works better
        if (direction >= Math.PI * 2) direction -= Math.PI * 2;
        if (Math.abs(direction - WEST) < 0.1) direction *= Math.signum(targetDir);
        if (Math.abs(targetDir - WEST) < 0.1) targetDir *= Math.signum(direction);
        //Calculate turn direction and turn
        double diff = targetDir - direction;
        double dTheta = Math.PI / 4;
        if (Math.abs(diff) > 0.1){
            direction += dTheta * Math.signum(diff);
        }
    }

    @Override
    public void onTurn() {
        if (target != null) {
            int range = getDetectRangeAt(target.getLocation(), detectRange);
            if (!isWithinDetectRange(target.getLocation(), range * 2)){
                target = null;
            }
        }
        super.onTurn();
    }

    private double getDirection(){
        if (target != null){
            Coordinate diff = target.getLocation().subtract(getLocation());
            return Math.atan2(diff.getY(), diff.getX());
        }
        else
            return direction;
    }

    @Override
    public ArrayList<LightingEffects.LightNode> provideLightNodes(LightingEffects lightingEffects) {
        ArrayList<LightingEffects.LightNode> nodes = super.provideLightNodes(lightingEffects);
        nodes.add(lightingEffects.createLightNode(getLocation(), 15, getDirection(), Math.PI / 2.5));
        return nodes;
    }
}

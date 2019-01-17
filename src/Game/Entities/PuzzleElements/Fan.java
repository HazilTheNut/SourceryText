package Game.Entities.PuzzleElements;

import Data.Coordinate;
import Data.EntityArg;
import Data.EntityStruct;
import Data.LayerImportances;
import Engine.Layer;
import Engine.LayerManager;
import Engine.SpecialText;
import Game.Entities.Entity;
import Game.FrameDrawListener;
import Game.GameInstance;
import Game.Projectile;
import Game.ProjectileListener;
import Game.Registries.TagRegistry;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

public class Fan extends Entity implements Powerable, FrameDrawListener, ProjectileListener {

    private int fanStrength = 0;
    private int calculatedRange = 0;
    private boolean active = false;
    private boolean defaultActiveState = false;
    private Layer windDisplayLayer;

    private final Coordinate NORTH    = new Coordinate(0, -1);
    private final Coordinate EAST     = new Coordinate(1, 0);
    private final Coordinate SOUTH    = new Coordinate(0, 1);
    private final Coordinate WEST     = new Coordinate(-1, 0);
    private Coordinate direction;

    @Override
    public void initialize(Coordinate pos, LayerManager lm, EntityStruct entityStruct, GameInstance gameInstance) {
        super.initialize(pos, lm, entityStruct, gameInstance);
        fanStrength = readIntArg(searchForArg(entityStruct.getArgs(), "Strength"), 1);
        animationFrame = (byte)readIntArg(searchForArg(entityStruct.getArgs(), "startFrame"), 0);
        defaultActiveState = readBoolArg(searchForArg(entityStruct.getArgs(), "defaultOn"), false);
        active = defaultActiveState;
        switch (readStrArg(searchForArg(entityStruct.getArgs(), "direction"), "SOUTH")){
            case "NORTH": direction = NORTH; break;
            case "SOUTH": direction = SOUTH; break;
            case "WEST": direction = WEST; break;
            case "EAST":
            default: direction = EAST; break;
        }
        windParticles = new ArrayList<>();
        calculatedRange = raycastFanRange();
        generateIcon();
        generateWindLayer();
        onPowerUpdate();
        gi.getCurrentLevel().addFrameDrawListener(this);
        gi.getCurrentLevel().addProjectileListener(this);
    }

    @Override
    public ArrayList<EntityArg> generateArgs() {
        ArrayList<EntityArg> args = super.generateArgs();
        args.add(new EntityArg("Strength", "1"));
        args.add(new EntityArg("startFrame", "0"));
        args.add(new EntityArg("defaultOn", "false"));
        args.add(new EntityArg("direction", "NORTH, SOUTH, EAST, WEST"));
        return args;
    }

    private void generateWindLayer(){
        int range = getFanRange();
        //Coordinate vector math ahead
        Coordinate absDir = new Coordinate(Math.abs(direction.getX()), Math.abs(direction.getY())); //Doing vector math with Coordinates betrays its own name, but changing the name would be problematic
        Coordinate dim = absDir.multiply(range).add(absDir).floor(new Coordinate(1, 1)); //The dimensions of the layer. Layer must be as long as (range + 1) in any of the four directions, and not be zero width.
        Coordinate offset = direction.ceil(new Coordinate(0, 0)).multiply(range).add(direction); //Relative location of layer. Effect is more pronounced for negative values.
        //Build layer
        windDisplayLayer = new Layer(dim.getX(), dim.getY(), "fan_wind:" + getUniqueID(), getLocation().add(offset).getX(), getLocation().add(offset).getY(), LayerImportances.ENTITY_SOLID - 1);
        windDisplayLayer.setVisible(defaultActiveState);
        gi.getLayerManager().addLayer(windDisplayLayer);
    }

    @Override
    public void selfDestruct() {
        super.selfDestruct();
        gi.getLayerManager().removeLayer(windDisplayLayer);
    }

    @Override
    public void onPowerOff() {
        active = defaultActiveState;
        onPowerUpdate();
    }

    @Override
    public void onPowerOn() {
        active = !defaultActiveState;
        onPowerUpdate();
    }

    private void onPowerUpdate(){
        if (!active)
            windParticles.clear();
        else
            runWindParticleSimulation();
        windDisplayLayer.setVisible(active);
    }

    private int getFanRange(){
        return (fanStrength * 2) + 2;
    }

    private int raycastFanRange(){
        Coordinate checkLoc = getLocation().copy();
        /**/
        for (int i = 0; i < getFanRange(); i++) {
            checkLoc.movePos(direction.getX(), direction.getY());
            if (!gi.isSpaceAvailable(checkLoc, TagRegistry.TILE_WALL))
                return i + 1;
        }
        /**/
        return getFanRange();
    }

    @Override
    public void onTurn() {
        super.onTurn();
        calculatedRange = raycastFanRange();
    }

    private int getFanStrength(){
        if (active) return fanStrength;
        else return 0;
    }

    private boolean isWithinWindDomain(Coordinate loc){
        Coordinate layerPos = loc.subtract(windDisplayLayer.getPos());
        return !windDisplayLayer.isLayerLocInvalid(layerPos) && getLocation().stepDistance(loc) < calculatedRange;
    }

    private byte animationFrame = 0;
    private int subFrame = 0; //Represents the time spent in between frames, as the fan should not spin at 5rps (instead it spins at variable speed based on fan strength
    private char[] animation = {'/', '-', '\\', '|'};

    @Override
    public void onFrameDraw() {
        if (getFanStrength() <= 0) return;
        subFrame++;
        if (subFrame >= 5 - fanStrength) {
            runWindParticleSimulation();
            animationFrame++;
            generateIcon();
            subFrame = 0;
        }
    }

    private void generateIcon(){
        if (animationFrame >= animation.length) animationFrame -= animation.length;
        SpecialText icon = getSprite().getSpecialText(0, 0);
        setIcon(new SpecialText(animation[animationFrame], icon.getFgColor(), icon.getBkgColor()));
        updateSprite();
    }

    @Override
    public void onProjectileFly(Projectile projectile) {
        if (windDisplayLayer.getVisible()) {
            if (isWithinWindDomain(projectile.getRoundedPos())) {
                double dotProduct = Math.abs((direction.getX() * projectile.getNormalizedVelocityX()) + (direction.getY() * projectile.getNormalizedVelocityY())); //In this case the dot product is only used to determine how perpendicular the vectors are
                double windTranslationScalar = fanStrength * (1 - dotProduct) * 0.25f; //Projectiles should not ever move two spaces in one direction in a single movement cycle
                double windRotationScalar = fanStrength * 0.03f;
                projectile.adjust(direction.getX() * windTranslationScalar, direction.getY() * windTranslationScalar, direction.getX() * windRotationScalar, direction.getY() * windRotationScalar);
            }
        }
    }

    private ArrayList<Coordinate> windParticles;
    private int timeSinceLastParticle = 0;

    private void runWindParticleSimulation(){
        Random random = new Random();
        windDisplayLayer.clearLayer();
        timeSinceLastParticle += 2;
        if (random.nextInt(10) < timeSinceLastParticle) {
            windParticles.add(getLocation().copy());
            timeSinceLastParticle = -2;
        }
        for (int i = 0; i < windParticles.size();) {
            windParticles.get(i).movePos(direction.getX(), direction.getY());
            Coordinate layerPos = windParticles.get(i).subtract(windDisplayLayer.getPos());
            if (!isWithinWindDomain(windParticles.get(i)))
                windParticles.remove(i);
            else {
                windDisplayLayer.editLayer(layerPos, new SpecialText(getWindDisplayChar(), new Color(200, 200, 200)));
                i++;
            }
        }
    }

    private char getWindDisplayChar(){
        if (direction.equals(NORTH) || direction.equals(SOUTH))
            return '|';
        else if (direction.equals(EAST) || direction.equals(WEST))
            return '-';
        return '*';
    }
}

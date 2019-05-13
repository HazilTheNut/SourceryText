package Game.LevelScripts;

import Data.Coordinate;
import Data.LayerImportances;
import Data.SerializationVersion;
import Engine.Layer;
import Engine.SpecialText;
import Game.Debug.DebugWindow;
import Game.Registries.TagRegistry;

import java.awt.*;
import java.util.ArrayList;

public class WaterFlow extends LevelScript {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    private ArrayList<Coordinate> particles;
    private int particleSpawnCountdown;
    private Layer particleLayer;

    //Optimization stuff
    private Coordinate[][] movementVectorField;
    private ArrayList<Integer> wateryTileIDs;

    @Override
    public void onLevelLoad() {
        particles = new ArrayList<>();
        particleLayer = new Layer(getWidth(), getHeight(), "WaterFlow particles: " + level.getName(), 0, 0, LayerImportances.TILE_ANIM + 1);
        computeVectorField();
        for (int i = 0; i < 100; i++) {
            updateParticles();
        }
    }

    @Override
    public void onLevelEnter() {
        particleLayer = new Layer(getWidth(), getHeight(), "WaterFlow particles: " + level.getName(), 0, 0, LayerImportances.TILE_ANIM + 1);
        gi.getLayerManager().addLayer(particleLayer);
        computeVectorField();
    }

    protected void computeVectorField(){
        movementVectorField = new Coordinate[getWidth()+1][getHeight()+1];
        for (int col = 0; col < movementVectorField.length; col++) {
            for (int row = 0; row < movementVectorField[0].length; row++) {
                movementVectorField[col][row] = computeFlowDirection(new Coordinate(col, row));
            }
        }
    }

    @Override
    public String[] getMaskNames() {
        return new String[]{"North", "South", "East", "West", "ParticleStart"};
    }

    private Coordinate computeFlowDirection(Coordinate pos){
        Coordinate vector = new Coordinate(0, 0);
        if (getMaskDataAt("North", pos))
            vector = vector.add(new Coordinate(0, -1));
        if (getMaskDataAt("South", pos))
            vector = vector.add(new Coordinate(0, 1));
        if (getMaskDataAt("East", pos))
            vector = vector.add(new Coordinate(1, 0));
        if (getMaskDataAt("West", pos))
            vector = vector.add(new Coordinate(-1, 0));
        return vector;
    }

    public Coordinate getFlowDirection(Coordinate pos){
        return movementVectorField[pos.getX()][pos.getY()];
    }

    private int updateTimer = 3;

    @Override
    public void onAnimatedTileUpdate() {
        if (particleLayer != null && particleLayer.getVisible()) {
            updateTimer--;
            if (updateTimer < 1) {
                updateParticles();
                updateTimer = 3;
            }
        }
    }

    @Override
    public void onLevelExit() {
        gi.getLayerManager().removeLayer(particleLayer);
    }

    public ArrayList<Coordinate> getParticles() {
        return particles;
    }

    public void resetParticles(){
        particles = new ArrayList<>();
    }

    private Coordinate playerPrevPos;

    @Override
    public void onTurnStart() {
        if (gi.getPlayer().getLocation().equals(playerPrevPos) && gi.getPlayer().isOnRaft()){
            gi.getPlayer().teleport(gi.getPlayer().getLocation().add(getFlowDirection(gi.getPlayer().getLocation())));
        }
        playerPrevPos = gi.getPlayer().getLocation().copy();
    }

    public void updateParticles(){
        if (particleSpawnCountdown < 1)
            particleSpawnCountdown = 6;
        else
            particleSpawnCountdown--;
        spawnNewParticles();
        translateParticles();
        DebugWindow.reportf(DebugWindow.STAGE, "WaterFlow.updateAParticles", "count: %1$d", particles.size());
    }

    private void spawnNewParticles(){
        for (int col = 0; col < getWidth(); col++) {
            for (int row = 0; row < getHeight(); row++) {
                if (getMaskDataAt("ParticleStart", new Coordinate(col, row)) && hashLoc(col, row) == particleSpawnCountdown)
                    particles.add(new Coordinate(col, row));
            }
        }
    }

    public int getWidth(){
        return level.getWidth();
    }

    public int getHeight(){
        return level.getHeight();
    }

    private int hashLoc(int col, int row){
        return (71 * 119 * col + row) % 6;
    }

    private void translateParticles(){
        if (particleLayer != null) //The level editor can run a simulation of this level script and the layer does not exist in this case.
            particleLayer.clearLayer();
        for (int i = 0; i < particles.size(); i++) {
            Coordinate particle = particles.get(i);
            Coordinate totalVector = getFlowDirection(particle);
            if (totalVector.equals(new Coordinate(0, 0))){
                particles.remove(i);
                i--; //To account for removing a particle
            } else {
                particles.set(i, particle.add(totalVector));
                if (isWaterAt(particle))
                    drawParticle(particle, totalVector);
            }
        }
    }

    public boolean isWaterAt(Coordinate loc){
        return level.checkTileTag(loc, TagRegistry.DEEP_WATER, true) || level.checkTileTag(loc, TagRegistry.SHALLOW_WATER, true);
    }

    public void drawParticle(Coordinate particle, Coordinate vector){
        char c = ' ';
        if (vector.equals(new Coordinate(1, 0)) || vector.equals(new Coordinate(-1, 0)))
            c = '-';
        if (vector.equals(new Coordinate(0, 1)) || vector.equals(new Coordinate(0, -1)))
            c = '|';
        if (vector.equals(new Coordinate(1, 1)) || vector.equals(new Coordinate(-1, -1)))
            c = '\\';
        if (vector.equals(new Coordinate(-1, 1)) || vector.equals(new Coordinate(1, -1)))
            c = '/';
        particleLayer.editLayer(particle, new SpecialText(c, getParticleColorAt(particle)));
    }

    private Color getParticleColorAt(Coordinate loc){
        SpecialText text;
        if (level.getOverlayTileLayer().getSpecialText(loc) == null)
            text = level.getBackdrop().getSpecialText(loc);
        else
            text = level.getOverlayTileLayer().getSpecialText(loc);
        float[] hsb = new float[3];
        hsb = Color.RGBtoHSB(text.getBkgColor().getRed(), text.getBkgColor().getGreen(), text.getBkgColor().getBlue(), hsb);
        return Color.getHSBColor(hsb[0], Math.max(hsb[1] - 0.15f, 0), Math.min(hsb[2] + 0.35f, 1));
    }
}

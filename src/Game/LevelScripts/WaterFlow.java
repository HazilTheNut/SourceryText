package Game.LevelScripts;

import Data.Coordinate;
import Data.LayerImportances;
import Engine.Layer;
import Engine.SpecialText;
import Game.Debug.DebugWindow;
import Game.Registries.TagRegistry;
import Game.Tile;

import java.awt.*;
import java.util.ArrayList;

public class WaterFlow extends LevelScript {

    private ArrayList<Coordinate> particles;
    private int particleSpawnCountdown;
    private Layer particleLayer;

    @Override
    public void onLevelLoad() {
        particles = new ArrayList<>();
        particleLayer = new Layer(level.getWidth(), level.getHeight(), "WaterFlow particles: " + level.getName(), 0, 0, LayerImportances.TILE_ANIM + 1);
        for (int i = 0; i < 100; i++) {
            updateParticles();
        }
    }

    @Override
    public void onLevelEnter() {
        particleLayer = new Layer(level.getWidth(), level.getHeight(), "WaterFlow particles: " + level.getName(), 0, 0, LayerImportances.TILE_ANIM + 1);
        gi.getLayerManager().addLayer(particleLayer);
    }

    private int updateTimer = 3;

    @Override
    public void onAnimatedTileUpdate() {
        updateTimer--;
        if (updateTimer < 1){
            updateParticles();
            updateTimer = 3;
        }
    }

    @Override
    public void onLevelExit() {
        gi.getLayerManager().removeLayer(particleLayer);
    }

    @Override
    public String[] getMaskNames() {
        return new String[]{"North", "South", "East", "West", "ParticleStart"};
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

    private void updateParticles(){
        if (particleSpawnCountdown < 1)
            particleSpawnCountdown = 6;
        else
            particleSpawnCountdown--;
        spawnNewParticles();
        translateParticles();
        DebugWindow.reportf(DebugWindow.STAGE, "WaterFlow.updateAParticles", "count: %1$d", particles.size());
    }

    private void spawnNewParticles(){
        for (int col = 0; col < level.getWidth(); col++) {
            for (int row = 0; row < level.getHeight(); row++) {
                if (getMaskDataAt("ParticleStart", new Coordinate(col, row)) && hashLoc(col, row) == particleSpawnCountdown)
                    particles.add(new Coordinate(col, row));
            }
        }
    }

    private int hashLoc(int col, int row){
        return (71 * 119 * col + row) % 6;
    }

    private void translateParticles(){
        particleLayer.clearLayer();
        for (int i = 0; i < particles.size(); i++) {
            Coordinate particle = particles.get(i);

            Coordinate totalVector = new Coordinate(0, 0);
            for (Coordinate vector : getBannedRaftDirections(particle)) {
                totalVector = totalVector.add(vector.multiply(-1));
            }
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

    private boolean isWaterAt(Coordinate loc){
        Tile tile = gi.getTileAt(loc);
        return tile.hasTag(TagRegistry.SHALLOW_WATER) || tile.hasTag(TagRegistry.DEEP_WATER);
    }

    private void drawParticle(Coordinate particle, Coordinate vector){
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

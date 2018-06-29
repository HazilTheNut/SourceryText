package Game.Entities.PuzzleElements;

import Data.*;
import Engine.Layer;
import Engine.LayerManager;
import Engine.SpecialText;
import Game.Entities.Entity;
import Game.GameInstance;
import Game.Registries.TagRegistry;

import java.awt.*;
import java.util.ArrayList;

public class RoomCover extends Entity implements Powerable {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    private Layer coverLayer;
    private boolean includeWalls;

    @Override
    public ArrayList<EntityArg> generateArgs() {
        ArrayList<EntityArg> args = super.generateArgs();
        args.add(new EntityArg("bounds","[0,0]-[999,999]"));
        args.add(new EntityArg("includeWalls","true"));
        return args;
    }

    @Override
    public boolean isSolid() {
        return false;
    }

    @Override
    public void initialize(Coordinate pos, LayerManager lm, EntityStruct entityStruct, GameInstance gameInstance) {
        super.initialize(pos, lm, entityStruct, gameInstance);
        ArrayList<Coordinate> bounds = readCoordListArg(searchForArg(entityStruct.getArgs(), "bounds"));
        if (bounds.size() >= 2){
            buildCoverLayer(bounds.get(0), bounds.get(1));
            coverLayer.setVisible(true);
            getSprite().editLayer(0, 0, null);
        }
        includeWalls = readBoolArg(searchForArg(entityStruct.getArgs(), "includeWalls"), true);
    }

    private void buildCoverLayer(Coordinate bound1, Coordinate bound2){
        int left   = Math.min(bound1.getX(), bound2.getX());
        int top    = Math.min(bound1.getY(), bound2.getY());
        int right  = Math.max(bound1.getX(), bound2.getX());
        int bottom = Math.max(bound1.getY(), bound2.getY());
        coverLayer = new Layer(right - left + 1, bottom - top + 1, "cover" + getUniqueID(), left, top, LayerImportances.VFX-1);
    }

    private void createCover(){
        ArrayList<Coordinate> locs = new ArrayList<>();
        locs.add(getLocation());
        for (int i = 0; i < locs.size();) {
            Coordinate loc = locs.get(i);
            attemptPoint(loc, loc.add(new Coordinate(1, 0)), locs);
            attemptPoint(loc, loc.add(new Coordinate(1, 1)), locs);
            attemptPoint(loc, loc.add(new Coordinate(0, 1)), locs);
            attemptPoint(loc, loc.add(new Coordinate(-1, 1)), locs);
            attemptPoint(loc, loc.add(new Coordinate(-1, 0)), locs);
            attemptPoint(loc, loc.add(new Coordinate(-1, -1)), locs);
            attemptPoint(loc, loc.add(new Coordinate(0, -1)), locs);
            attemptPoint(loc, loc.add(new Coordinate(1, -1)), locs);
            i++;
        }
        coverLayer.clearLayer();
        for (Coordinate loc : locs){
            coverLayer.editLayer(loc.subtract(coverLayer.getPos()), new SpecialText(' ', Color.WHITE, Color.BLACK));
        }
    }

    private void attemptPoint(Coordinate origin, Coordinate attempt, ArrayList<Coordinate> locs){
        boolean isSpace = (includeWalls && gi.isSpaceAvailable(origin, TagRegistry.TILE_WALL)) || (!includeWalls && gi.isSpaceAvailable(attempt, TagRegistry.TILE_WALL));
        boolean alreadyThere = !locs.contains(attempt);
        boolean isBounded = !coverLayer.isLayerLocInvalid(attempt.subtract(coverLayer.getPos()));
        if (isSpace && alreadyThere && isBounded){
            locs.add(attempt);
        }
    }

    @Override
    public void onLevelEnter() {
        super.onLevelEnter();
        if (coverLayer != null) {
            createCover();
            gi.getLayerManager().addLayer(coverLayer);
        }
    }

    @Override
    public void onLevelExit() {
        super.onLevelExit();
        if (coverLayer != null) {
            gi.getLayerManager().removeLayer(coverLayer);
        }
    }

    @Override
    public void onPowerOff() {
        coverLayer.setVisible(true);
    }

    @Override
    public void onPowerOn() {
        coverLayer.setVisible(false);
    }
}

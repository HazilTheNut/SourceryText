package Game.Spells;

import Data.Coordinate;
import Data.LayerImportances;
import Data.SerializationVersion;
import Engine.Layer;
import Engine.LayerManager;
import Engine.SpecialText;
import Game.Entities.CombatEntity;
import Game.Entities.Entity;
import Game.GameInstance;
import Game.Registries.TagRegistry;

import java.awt.*;
import java.util.ArrayList;

public class AquamancySpell extends Spell {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    @Override
    public String getName() {
        return "Aquamancy";
    }

    @Override
    public Color getColor() {
        return new Color(181, 255, 255);
    }

    @Override
    public Spell copy() {
        return new AquamancySpell();
    }

    private boolean[][] allowedSpaces;
    private Layer previewLayer;

    @Override
    public void readySpell(Coordinate targetLoc, Entity spellCaster, GameInstance gi, int magicPower) {
        allowedSpaces = new boolean[gi.getCurrentLevel().getWidth()][gi.getCurrentLevel().getHeight()];
        previewLayer = new Layer(gi.getCurrentLevel().getWidth(), gi.getCurrentLevel().getHeight(), "aquamancy", 0, 0, LayerImportances.ANIMATION);
        gi.getLayerManager().addLayer(previewLayer);
        ArrayList<Coordinate> placesToCheck = new ArrayList<>();
        placesToCheck.add(spellCaster.getLocation().copy());
        while (placesToCheck.size() > 0){
            placesToCheck = spreadPoints(placesToCheck, gi);
        }
        //Draw accessible areas
        for (int col = 0; col < previewLayer.getCols(); col++) {
            for (int row = 0; row < previewLayer.getRows(); row++) {
                if (canTeleportTo(new Coordinate(col, row), gi)) previewLayer.editLayer(col, row, new SpecialText(' ', Color.WHITE, new Color(255, 255, 255, 86))); //Draws outline
            }
        }
        //previewLayer.setVisible(!allowedSpaces[targetLoc.getX()][targetLoc.getY()]);
    }
    
    private boolean canTeleportTo(Coordinate loc, GameInstance gameInstance){
        return gameInstance.getTileAt(loc).hasTag(TagRegistry.WET) && gameInstance.isSpaceAvailable(loc, TagRegistry.NO_PATHING) && allowedSpaces[loc.getX()][loc.getY()];
    }

    private ArrayList<Coordinate> spreadPoints(ArrayList<Coordinate> placesToCheck, GameInstance gi){
        ArrayList<Coordinate> nextPoints = new ArrayList<>();
        for (Coordinate pt : placesToCheck) {
            attemptPoint(pt.add(new Coordinate(0, 1)), gi, nextPoints);
            attemptPoint(pt.add(new Coordinate(1, 0)), gi, nextPoints);
            attemptPoint(pt.add(new Coordinate(-1, 0)), gi, nextPoints);
            attemptPoint(pt.add(new Coordinate(0, -1)), gi, nextPoints);
        }
        return nextPoints;
    }

    private void attemptPoint(Coordinate loc, GameInstance gi, ArrayList<Coordinate> nextPoints){
        boolean spaceOpen = gi.isSpaceAvailable(loc, TagRegistry.TILE_WALL);
        if (!allowedSpaces[loc.getX()][loc.getY()] && spaceOpen){
            allowedSpaces[loc.getX()][loc.getY()] = true;
            nextPoints.add(loc);
        }
    }

    @Override
    public void spellDrag(Coordinate targetLoc, Entity spellCaster, GameInstance gi, int magicPower) {
        //previewLayer.setVisible(!allowedSpaces[targetLoc.getX()][targetLoc.getY()]);
    }

    @Override
    public int castSpell(Coordinate targetLoc, Entity spellCaster, GameInstance gi, int magicPower) {
        gi.getLayerManager().removeLayer(previewLayer);
        if (canTeleportTo(targetLoc, gi)) {
            spellCaster.getSprite().setVisible(false);
            playTeleportAnimation(targetLoc, gi.getLayerManager());
            spellCaster.getSprite().setVisible(true);
            spellCaster.teleport(targetLoc);
            return calculateCooldown(25, magicPower);
        }
        return 0;
    }

    private void playTeleportAnimation(Coordinate targetLoc, LayerManager lm){
        Layer animLayer = new Layer(1, 1, "aquamncy", targetLoc.getX(), targetLoc.getY(), LayerImportances.ANIMATION);
        Color fgColor = new Color(170, 210, 255);
        Color bgColor = new Color(61, 74, 176, 30);
        lm.addLayer(animLayer);
        while (lm.isDrawingFrame())
            sleep(20);
        char[] animSet = {'.','o','O'};
        for (char c : animSet) {
            animLayer.editLayer(0, 0, new SpecialText(c, fgColor, bgColor));
            sleep(75);
        }
        lm.removeLayer(animLayer);
    }

    private void sleep(long millis){
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

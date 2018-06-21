package Game.LevelScripts;

import Data.Coordinate;
import Data.LayerImportances;
import Engine.Layer;
import Engine.SpecialText;
import Game.Debug.DebugWindow;
import Game.Entities.CombatEntity;
import Game.Entities.Entity;
import Game.Player;
import Game.Registries.TagRegistry;
import Game.TagHolder;

import java.awt.*;
import java.util.ArrayList;

public class LightingEffects extends LevelScript {

    private ArrayList<LightNode> lightNodes;
    private Layer shadingLayer;

    private int[] lightIDs = {
            TagRegistry.ON_FIRE,
            TagRegistry.FLAME_ENCHANT
    };

    private Color lightingCold = new Color(10, 10, 20);
    private Color lightingWarm = new Color(255, 181, 107);

    private double[][] lightMap;

    @Override
    public void onLevelLoad() {
        super.onLevelLoad();
        shadingLayer = new Layer(level.getBackdrop().getCols(), level.getBackdrop().getRows(), "ls_lighting: " + level.getName(), 0, 0, LayerImportances.VFX);
        lightNodes = new ArrayList<>();
        lightMap = new double[level.getBackdrop().getCols()][level.getBackdrop().getRows()];
        identifyLightNodes();
        drawShadingLayer();
    }

    @Override
    public void onLevelEnter() {
        gi.getLayerManager().addLayer(shadingLayer);
    }

    @Override
    public void onLevelExit() {
        gi.getLayerManager().removeLayer(shadingLayer);
    }

    @Override
    public void onTurnEnd() {
        identifyLightNodes();
        compileLightmaps();
        drawShadingLayer();
    }

    public double[][] getLightMap() {
        return lightMap;
    }

    /**
     * Clears the list of LightNodes and recalculates them.
     */
    private void identifyLightNodes(){
        lightNodes.clear();
        for (int col = 0; col < level.getBackdrop().getCols(); col++) {
            for (int row = 0; row < level.getBackdrop().getRows(); row++) {
                //Test tile
                if (testForLightTag(level.getTileAt(new Coordinate(col, row))))
                    addLightNode(new LightNode(col, row, 5));
                //Test entities
                ArrayList<Entity> atLoc = level.getEntitiesAt(new Coordinate(col, row));
                for (Entity e : atLoc){
                    //Test entity's own tags
                    if (testForLightTag(e))
                        addLightNode(new LightNode(col, row, 5));
                    //Test if Entity is CombatEntity and is holding a light-emitting Item.
                    if (e instanceof CombatEntity) {
                        CombatEntity ce = (CombatEntity) e;
                        if (testForLightTag(ce.getWeapon()))
                            addLightNode(new LightNode(col, row, 5));
                    }
                }
            }
        }
        Player player = gi.getPlayer();
        if (level.getEntities().contains(player)){
            addLightNode(new LightNode(player.getLocation().getX(), player.getLocation().getY(), 0.125));
        }
        reportLightNodes();
    }

    /**
     * Adds a LightNode to the list of LightNodes.
     *
     * If a LightNode happens to be nearby, they get merged together.
     * @param lightNode The LightNode to add.
     */
    private void addLightNode(LightNode lightNode){
        for (LightNode other : lightNodes){
            double dist = dist(other, lightNode);
            if (dist <= other.gravity){
                other.luminance += lightNode.luminance;
                other.gravity += 0.25;
                return;
            }
        }
        lightNodes.add(lightNode);
    }

    private boolean testForLightTag(TagHolder holder){
        for (int id : lightIDs){
            if (holder.hasTag(id)) return true;
        }
        return false;
    }

    /**
     * Draws the vfx Layer for light and shadows.
     */
    private void drawShadingLayer(){
        Layer tempLayer = new Layer(shadingLayer.getCols(), shadingLayer.getRows(), "shandingtemp", 0, 0, 0);
        for (int col = 0; col < level.getBackdrop().getCols(); col++) { //Iterate over every part of the level. //TODO: Clip range to just the screen.
            for (int row = 0; row < level.getBackdrop().getRows(); row++) {
                lightMap = new double[level.getBackdrop().getCols()][level.getBackdrop().getRows()];
                for (LightNode node : lightNodes) lightMap[col][row] += node.lightMap[col][row];
                double lightness = lightMap[col][row];
                double warmLightingCutoff = 4;
                if (lightness < 1){ //Draw the cold colors if it is dim.
                    int opacity = (int)(255 - (225 * lightness));
                    tempLayer.editLayer(col, row, new SpecialText(' ', Color.WHITE, new Color(lightingCold.getRed(), lightingCold.getGreen(), lightingCold.getBlue(), opacity)));
                } else if (lightness > warmLightingCutoff){ //Draw the warm colors if it is bright.
                    int opacity = Math.min((int)(10 * (lightness - warmLightingCutoff)), 70);
                    tempLayer.editLayer(col, row, new SpecialText(' ', Color.WHITE, new Color(lightingWarm.getRed(), lightingWarm.getGreen(), lightingWarm.getBlue(), opacity)));
                }
            }
        }
        shadingLayer.transpose(tempLayer);
    }

    /**
     * Calculates the distance between two LightNodes
     *
     * @param node1 The first LightNode
     * @param node2 The second LightNode
     * @return The distance between the two LightNodes
     */
    private double dist(LightNode node1, LightNode node2){
        return Math.sqrt(Math.pow(node1.x - node2.x, 2) + Math.pow(node1.y - node2.y, 2));
    }

    private int largestNumberNodes = 0;

    /**
     * Dumps out data to the DebugWindow
     */
    private void reportLightNodes(){
        largestNumberNodes = Math.max(largestNumberNodes, lightNodes.size());
        for (int i = 0; i < largestNumberNodes; i++) {
            if (i < lightNodes.size())
                DebugWindow.reportf(DebugWindow.STAGE, "LightingEffects.reportLightNodes :" + i, "l = %3$.3f x = %1$d y = %2$d", lightNodes.get(i).x, lightNodes.get(i).y, lightNodes.get(i).luminance);
            else
                DebugWindow.reportf(DebugWindow.STAGE, "LightingEffects.reportLightNodes :" + i, "-");
        }
    }

    private double lightingCutoff = 0.05;

    /**
     * Performs the radial raycasts on every LightNode.
     * Effectively calculates the lighting values of all the LightNodes
     */
    private void compileLightmaps(){
        for (LightNode lightNode : lightNodes){
            //dTheta is multiplied by the Math.min(...) function in order to increase its accuracy for low-luminance LightNodes, which can appear kinda grainy.
            double dTheta = Math.atan(Math.sqrt(lightingCutoff / lightNode.luminance)) * Math.min(lightNode.luminance, 0.65);
            for (double angle = 0; angle < Math.PI * 2; angle += dTheta) {
                performRaycast(lightNode, angle);
            }
            lightNode.assignLightMapValue(lightNode.x, lightNode.y, lightNode.luminance);
        }
    }

    /**
     * Performs a raycast until it hits a wall, defining lighting values for all the points in between.
     *
     * @param node The LightNode to raycast from
     * @param angle The angle the raycast is pointed
     */
    private void performRaycast(LightNode node, double angle){
        double xPerCycle = 0.9 * Math.cos(angle);
        double yPerCycle = 0.9 * Math.sin(angle);
        float x = (float)node.getX();
        float y = (float)node.getY();
        double distPerCycle = Math.sqrt(Math.pow(xPerCycle, 2) + Math.pow(yPerCycle, 2));
        int numberCycles = (int)(Math.ceil(Math.sqrt(node.luminance / lightingCutoff)) / distPerCycle);
        for (int dist = 0; dist < numberCycles; dist++) {
            x += xPerCycle;
            y += yPerCycle;
            Coordinate toCheck = new Coordinate(Math.round(x), Math.round(y));
            double lightness = node.luminance / Math.max(Math.pow(dist, 2), 1);
            node.assignLightMapValue(toCheck.getX(), toCheck.getY(), lightness);
            if (level.getTileAt(toCheck).hasTag(TagRegistry.TILE_WALL)){
                return;
            }
        }
    }

    private class LightNode{
        int x;
        int y;
        double luminance;
        double gravity;
        double[][] lightMap;
        private LightNode(int x, int y, double luminance){
            this.x = x;
            this.y = y;
            this.luminance = luminance;
            gravity = 1.5;
            lightMap = new double[level.getBackdrop().getCols()][level.getBackdrop().getRows()];
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        private void assignLightMapValue(int col, int row, double lightness){
            if (col < 0 || col >= lightMap.length || row < 0 || row >= lightMap[0].length)
                return;
            lightMap[col][row] = Math.max(lightness, lightMap[col][row]);
        }
    }
}

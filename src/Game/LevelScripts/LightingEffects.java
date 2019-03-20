package Game.LevelScripts;

import Data.Coordinate;
import Data.LayerImportances;
import Data.SerializationVersion;
import Engine.Layer;
import Engine.SpecialText;
import Game.Debug.DebugWindow;
import Game.Entities.CombatEntity;
import Game.Entities.Entity;
import Game.Player;
import Game.Registries.TagRegistry;
import Game.TagHolder;
import Game.Tags.LuminantTag;
import Game.Tags.Tag;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;

public class LightingEffects extends LevelScript {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    private ArrayList<LightNode> lightNodes;
    private Layer shadingLayer;

    private int[] lightTagIDs = {
            TagRegistry.ON_FIRE,
            TagRegistry.FLAME_ENCHANT,
            TagRegistry.BRIGHT,
            TagRegistry.EFFECT_GLOWING
    };

    private Color lightingCold = new Color(10, 10, 20);
    private Color lightingWarm = new Color(255, 181, 107);

    private double[][] masterLightMap;

    @Override
    public void onLevelLoad() {
        super.onLevelLoad();
        shadingLayer = new Layer(level.getBackdrop().getCols(), level.getBackdrop().getRows(), "ls_lighting: " + level.getName(), 0, 0, LayerImportances.VFX + 1);
        lightNodes = new ArrayList<>();
        masterLightMap = new double[level.getBackdrop().getCols()][level.getBackdrop().getRows()];
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
        long startTime = System.nanoTime();
        double[] calcTimes = new double[3];
        identifyLightNodes();
        calcTimes[0] = (System.nanoTime() - startTime) / 1000000f;
        compileLightmaps(); //Raycasting also done here too
        calcTimes[1] = (System.nanoTime() - startTime) / 1000000f;
        drawShadingLayer();
        calcTimes[2] = (System.nanoTime() - startTime) / 1000000f;
        DebugWindow.reportf(DebugWindow.STAGE, "LightingEffects.onTurnEnd", "times: id %1$.3fms | compile %2$.3fms | draw %3$.3fms | total %4$.3f", calcTimes[0], calcTimes[1] - calcTimes[0], calcTimes[2] - calcTimes[1], calcTimes[2]);
    }

    public double[][] getMasterLightMap() {
        return masterLightMap;
    }

    public int[] getLightTagIDs() {
        return lightTagIDs;
    }

    /**
     * Clears the list of LightNodes and recalculates them.
     */
    private void identifyLightNodes(){
        lightNodes.clear();
        for (int col = 0; col < level.getBackdrop().getCols(); col++) {
            for (int row = 0; row < level.getBackdrop().getRows(); row++) {
                //Test tile
                double luminance = testForLightTag(level.getTileAt(new Coordinate(col, row)));
                if (luminance > 0)
                    addLightNode(new LightNode(col, row, luminance));
            }
        }
        for (Entity e : level.getEntities()){
            //Test entity's own tags
            ArrayList<LightNode> nodes = e.provideLightNodes(this);
            for (LightNode node : nodes) addLightNode(node);
            //Test if Entity is CombatEntity and is holding a light-emitting Item.
            if (e instanceof CombatEntity) {
                CombatEntity ce = (CombatEntity) e;
                addLightNode(new LightNode(e.getLocation().getX(), e.getLocation().getY(), testForLightTag(ce.getWeapon())));
            }
        }
        Player player = gi.getPlayer();
        if (level.getEntities().contains(player)){
            addLightNode(new LightNode(player.getLocation().getX(), player.getLocation().getY(), 0.125));
        }
        //reportLightNodes();
    }

    /**
     * Adds a LightNode to the list of LightNodes.
     *
     * If a LightNode happens to be nearby, they get merged together.
     * @param lightNode The LightNode to add.
     */
    private void addLightNode(LightNode lightNode){
        if (lightNode == null || lightNode.luminance == 0) return;
        for (LightNode other : lightNodes){
            double dist = dist(other, lightNode);
            if (dist <= 0.005) return;
            if (dist <= other.gravity){
                other.luminance += lightNode.luminance;
                other.gravity += 0.15;
                return;
            }
        }
        lightNodes.add(lightNode);
    }

    public double testForLightTag(TagHolder holder){
        double sum = 0;
        for (Tag tag : holder.getTags()){
            if (tag instanceof LuminantTag) {
                sum += ((LuminantTag)tag).getLuminance();
            }
        }
        return sum;
    }

    /**
     * Draws the vfx Layer for light and shadows.
     */
    private void drawShadingLayer(){
        int MAX_OPACITY_COLD = 240;
        int MAX_OPACITY_WARM = 50;
        Layer tempLayer = new Layer(shadingLayer.getCols(), shadingLayer.getRows(), "shandingtemp", 0, 0, 0);
        for (int col = 0; col < level.getBackdrop().getCols(); col++) { //Iterate over every part of the level.
            for (int row = 0; row < level.getBackdrop().getRows(); row++) {
                double lightness = masterLightMap[col][row];
                double warmLightingCutoff = 3.75;
                if (lightness < 1){ //Draw the cold colors if it is dim.
                    int opacity = (int)(MAX_OPACITY_COLD * (1 - lightness));
                    tempLayer.editLayer(col, row, new SpecialText(' ', Color.WHITE, new Color(lightingCold.getRed(), lightingCold.getGreen(), lightingCold.getBlue(), opacity)));
                } else if (lightness > warmLightingCutoff){ //Draw the warm colors if it is bright.
                    int opacity = Math.min((int)(6 * (lightness - warmLightingCutoff)), MAX_OPACITY_WARM);
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
        //Do raycasts
        for (LightNode lightNode : lightNodes){
            //dTheta is multiplied by the Math.min(...) function in order to increase its accuracy for low-luminance LightNodes, which can appear kinda grainy.
            double dTheta = Math.atan(Math.sqrt(lightingCutoff / lightNode.luminance)) * Math.min(lightNode.luminance, 0.65);
            double angleMin = lightNode.direction - (lightNode.coneWidth / 2);
            double angleMax = lightNode.direction + (lightNode.coneWidth / 2);
            for (double angle = angleMin; angle < angleMax; angle += dTheta) {
                performRaycast(lightNode, new Coordinate(lightNode.getX(), lightNode.getY()), angle);
            }
            lightNode.assignLightMapValue(lightNode.x, lightNode.y, lightNode.luminance);
        }
        //Assemble the lightmaps
        masterLightMap = new double[level.getBackdrop().getCols()][level.getBackdrop().getRows()];
        Coordinate start = getLightmapCalculationStart();
        Coordinate end = getLightmapCalculationEnd();
        for (int col = start.getX(); col < end.getX(); col++) {
            for (int row = start.getY(); row < end.getY(); row++) {
                for (LightNode node : lightNodes) masterLightMap[col][row] += node.lightMap[col][row];
            }
        }
        DebugWindow.reportf(DebugWindow.STAGE, "LightingEffects.compileLightmaps", "calculation window: %1$d x %2$d", end.getX() - start.getX(), end.getY() - start.getY());
        //Apply smoothing
        smoothLightMap();
        smoothLightMap(); //Run a second pass to smooth things out further.
        //curveLightmap();
    }

    private void smoothLightMap(){
        double[][] smoothedMap = new double[level.getBackdrop().getCols()][level.getBackdrop().getRows()];
        Coordinate start = getLightmapCalculationStart();
        Coordinate end = getLightmapCalculationEnd();
        for (int col = start.getX(); col < end.getX(); col++) {
            for (int row = start.getY(); row < end.getY(); row++) {
                int numPoints = 0;
                double total = 0;
                if (getLightMapAmount(new Coordinate(col - 1, row)) >= 0){
                    total += getLightMapAmount(new Coordinate(col - 1, row));
                    numPoints++;
                }
                if (getLightMapAmount(new Coordinate(col, row + 1)) >= 0){
                    total += getLightMapAmount(new Coordinate(col, row + 1));
                    numPoints++;
                }
                if (getLightMapAmount(new Coordinate(col, row - 1)) >= 0){
                    total += getLightMapAmount(new Coordinate(col, row - 1));
                    numPoints++;
                }
                if (getLightMapAmount(new Coordinate(col + 1, row)) >= 0){
                    total += getLightMapAmount(new Coordinate(col + 1, row));
                    numPoints++;
                }
                smoothedMap[col][row] = (total + (masterLightMap[col][row] * numPoints)) / (numPoints * 2);
            }
        }
        masterLightMap = smoothedMap;
    }

    private void curveLightmap(){
        double[][] curvedLightMap = new double[level.getBackdrop().getCols()][level.getBackdrop().getRows()];
        Coordinate start = getLightmapCalculationStart();
        Coordinate end = getLightmapCalculationEnd();
        for (int col = start.getX(); col < end.getX(); col++) {
            for (int row = start.getY(); row < end.getY(); row++) {
                if (masterLightMap[col][row] < 1)
                    curvedLightMap[col][row] = Math.sqrt(masterLightMap[col][row]);
                else
                    curvedLightMap[col][row] = masterLightMap[col][row];
            }
        }
        masterLightMap = curvedLightMap;
    }

    private double getLightMapAmount(Coordinate pos){
        if (level.getBackdrop().isLayerLocInvalid(pos))
            return -1;
        return masterLightMap[pos.getX()][pos.getY()];
    }

    private int getCalcMarginX(){
        int playerDiff = gi.getPlayer().getLocation().getX() - gi.getLayerManager().getCameraPos().getX();
        return Math.max(gi.getLayerManager().getWindow().RESOLUTION_WIDTH - playerDiff, playerDiff);
    }

    private int getCalcMarginY(){
        int playerDiff = gi.getPlayer().getLocation().getY() - gi.getLayerManager().getCameraPos().getY();
        return Math.max(gi.getLayerManager().getWindow().RESOLUTION_HEIGHT - playerDiff, playerDiff);
    }

    private Coordinate getCalcPlayerOffset(){
        int margin = 2;
        return new Coordinate(getCalcMarginX() + margin, getCalcMarginY() + margin);
    }

    private Coordinate getLightmapCalculationStart(){
        return gi.getPlayer().getLocation().subtract(getCalcPlayerOffset()).floor(new Coordinate(0, 0));
    }

    private Coordinate getLightmapCalculationEnd(){
        return gi.getPlayer().getLocation().add(getCalcPlayerOffset()).ceil(new Coordinate(level.getWidth(), level.getHeight()));
    }

    /**
     * Performs a raycast until it hits a wall, defining lighting values for all the points in between.
     *
     * @param node The LightNode to raycast from
     * @param angle The angle the raycast is pointed
     */
    private void performRaycast(LightNode node, Coordinate startPos, double angle){
        double distPerCycle = 0.90;
        double xPerCycle = distPerCycle * Math.cos(angle);
        double yPerCycle = distPerCycle * Math.sin(angle);
        float x = (float)startPos.getX();
        float y = (float)startPos.getY();
        int numberCycles = (int)(Math.ceil(Math.sqrt(node.luminance / lightingCutoff)) / distPerCycle);
        for (int i = 0; i < numberCycles; i++) {
            if (checkRaycastAt(node, x, y, (float)xPerCycle, 0) || checkRaycastAt(node, x, y, 0, (float)yPerCycle)) //Checks movement along both axis simultaneously, but separately.
                return;
            x += xPerCycle;
            y += yPerCycle;
        }
    }

    private boolean checkRaycastAt(LightNode node, float xPos, float yPos, float dx, float dy){
        float newX = xPos + dx;
        float newY = yPos + dy;
        Coordinate toCheck = new Coordinate(Math.round(newX), Math.round(newY));
        double dist = Math.sqrt(Math.pow(node.x - newX, 2) + Math.pow(node.y - newY, 2));
        double lightness = node.luminance / Math.max(Math.pow(dist, 2), 1);
        node.assignLightMapValue(toCheck.getX(), toCheck.getY(), lightness);
        return (level.isLocationValid(toCheck) && level.getTileAt(toCheck).hasTag(TagRegistry.TILE_WALL));
    }

    public LightNode createLightNode(Coordinate loc, double luminance, double direction, double coneWidth){
        return new LightNode(loc.getX(), loc.getY(), luminance, direction, coneWidth);
    }

    public class LightNode implements Serializable {

        private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

        int x;
        int y;
        double luminance;
        double gravity;
        double[][] lightMap;

        double direction;
        double coneWidth;

        public LightNode(int x, int y, double luminance){
            this(x, y, luminance, 0, Math.PI * 2);
        }

        public LightNode(int x, int y, double luminance, double direction, double coneWidth){
            this.x = x;
            this.y = y;
            this.luminance = luminance;
            gravity = 1.25;
            lightMap = new double[level.getBackdrop().getCols()][level.getBackdrop().getRows()];
            this.direction = direction;
            this.coneWidth = coneWidth;
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

package Editor;

import Data.*;
import Engine.Layer;
import Engine.LayerManager;
import Engine.SpecialText;
import Game.AnimatedTiles.FireAnimation;
import Game.Level;
import Game.LevelScripts.WaterFlow;
import Game.OverlayTileGenerator;
import Game.Registries.LevelScriptRegistry;
import Game.Registries.TagRegistry;
import Game.Registries.TileRegistry;
import Game.Tile;

import javax.swing.*;
import java.awt.*;

public class SimulationPanel extends JFrame {

    private EditorFrame editorFrame;
    private LayerManager lm;
    private LevelData ldata;

    SimulationPanel(EditorFrame editorFrame, LayerManager layerManager, LevelData ldata){

        this.editorFrame = editorFrame;
        this.ldata = ldata;
        lm = layerManager;

        Container c = getContentPane();

        setSize(new Dimension(300, 200));
        setTitle("Level Script Simulator");

        setLayout(new GridLayout(2, 1, 5, 8));

        JPanel overlayPanel = new JPanel(new GridLayout());
        overlayPanel.setBorder(BorderFactory.createTitledBorder("Overlay Tiles"));

        JButton overlaySimBtn = new JButton("Simulate");
        overlaySimBtn.addActionListener(e -> buildOverlayTileSimulation());
        overlayPanel.add(overlaySimBtn);

        c.add(overlayPanel);

        JPanel waterFlowPanel = new JPanel();
        waterFlowPanel.setLayout(new BoxLayout(waterFlowPanel, BoxLayout.LINE_AXIS));
        waterFlowPanel.setBorder(BorderFactory.createTitledBorder("Water Flow"));

        JSpinner waterSimCycleSpinner = new JSpinner(new SpinnerNumberModel(250, 50, 900, 25));

        JButton waterSimBtn = new JButton("Simulate");
        waterSimBtn.addActionListener(e -> buildWaterFlowSimulation((int)waterSimCycleSpinner.getModel().getValue()));

        waterFlowPanel.add(waterSimBtn);
        waterFlowPanel.add(waterSimCycleSpinner);

        c.add(waterFlowPanel);

        c.validate();
        setVisible(true);
    }

    private void buildOverlayTileSimulation(){
        //Initialize stuff
        Layer simulationLayer;
        LayerToggler toggler = editorFrame.getLayerToggler("simulation.overlay");
        if (toggler == null)
            simulationLayer = new Layer(ldata.getBackdrop().getCols(), ldata.getBackdrop().getRows(), "simulation.overlay", 0, 0, LayerImportances.EDITOR_TILE + 1);
        else
            simulationLayer = toggler.getLayer();
        simulationLayer.clearLayer();
        LevelScriptMask snowMask    = ldata.getLevelScriptMask(LevelScriptRegistry.SCRIPT_OVERLAYTILES, "snow");
        LevelScriptMask iceMask     = ldata.getLevelScriptMask(LevelScriptRegistry.SCRIPT_OVERLAYTILES, "ice");
        LevelScriptMask bridgeMask  = ldata.getLevelScriptMask(LevelScriptRegistry.SCRIPT_OVERLAYTILES, "bridge");
        LevelScriptMask ashMask     = ldata.getLevelScriptMask(LevelScriptRegistry.SCRIPT_OVERLAYTILES, "ash");
        LevelScriptMask fireMask    = ldata.getLevelScriptMask(LevelScriptRegistry.SCRIPT_OVERLAYTILES, "fire");
        OverlayTileGenerator otg = new OverlayTileGenerator();
        //Begin drawing
        for (int col = 0; col < ldata.getBackdrop().getCols(); col++) {
            for (int row = 0; row < ldata.getBackdrop().getRows(); row++) {
                if (snowMask.getMask()[col][row])
                    simulationLayer.editLayer(col, row, otg.TILE_SNOW);
                else if (iceMask.getMask()[col][row])
                    simulationLayer.editLayer(col, row, otg.getIceTileSpecTxt(new Coordinate(col, row)));
                else if (bridgeMask.getMask()[col][row])
                    simulationLayer.editLayer(col, row, otg.TILE_BRIDGE);
                else if (ashMask.getMask()[col][row])
                    simulationLayer.editLayer(col, row, otg.getAshTileSpecTxt());
                else if (fireMask.getMask()[col][row]){
                    FireAnimation fireAnimation = new FireAnimation(new Coordinate(col, row));
                    simulationLayer.editLayer(col, row, fireAnimation.onDisplayUpdate());
                }
            }
        }
        simulationLayer.setVisible(true);
        //Send to display
        if (editorFrame.addLayerToggler(new LayerToggler(simulationLayer, "Simulation: Overlay")))
            lm.addLayer(simulationLayer);
    }

    private void buildWaterFlowSimulation(int numCycles){
        //Initial stuff
        Layer simulationLayer;
        LayerToggler toggler = editorFrame.getLayerToggler("simulation.waterflow");
        if (toggler == null)
            simulationLayer = new Layer(ldata.getBackdrop().getCols(), ldata.getBackdrop().getRows(), "simulation.waterflow", 0, 0, LayerImportances.EDITOR_TILE + 1);
        else
            simulationLayer = toggler.getLayer();
        simulationLayer.clearLayer();
        SimWaterFlow simWaterFlow = new SimWaterFlow();
        simWaterFlow.ldata = ldata;
        //Begin simulation
        for (int i = 0; i < numCycles; i++) {
            simWaterFlow.updateParticles();
            for (Coordinate particle : simWaterFlow.getParticles())
                simulationLayer.editLayer(particle, new SpecialText('.'));
        }
        simulationLayer.setVisible(true);
        if (editorFrame.addLayerToggler(new LayerToggler(simulationLayer, "Simulation: Water Flow")))
            lm.addLayer(simulationLayer);
    }

    private class SimWaterFlow extends WaterFlow {

        private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

        private LevelData ldata;

        public SimWaterFlow(){
            resetParticles();
        }

        @Override
        public boolean getMaskDataAt(String name, Coordinate loc) {
            LevelScriptMask mask = ldata.getLevelScriptMask(LevelScriptRegistry.SCRIPT_WATERFLOW, name);
            if (loc.getX() < 0 || loc.getX() >= mask.getMask().length || loc.getY() < 0 || loc.getY() >= mask.getMask()[0].length)
                return false;
            return mask.getMask()[loc.getX()][loc.getY()];
        }

        @Override
        public int getWidth() {
            return ldata.getBackdrop().getCols();
        }

        public int getHeight(){
            return ldata.getBackdrop().getRows();
        }

        @Override
        public boolean isWaterAt(Coordinate loc) {
            int[] tags = TileRegistry.getTileStruct(ldata.getTileId(loc.getX(), loc.getY())).getTagIDs();
            for (int tag : tags)
                if (tag == TagRegistry.DEEP_WATER || tag == TagRegistry.SHALLOW_WATER) return true;
            return false;
        }

        @Override
        public void drawParticle(Coordinate particle, Coordinate vector) { }
    }

}

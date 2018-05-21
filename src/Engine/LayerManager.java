package Engine;

import Data.Coordinate;
import Game.Debug.DebugWindow;

import java.awt.*;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Jared on 2/18/2018.
 */
public class LayerManager {

    private ArrayList<Layer> layerStack = new ArrayList<>();

    private ArrayList<LayerOperation> operationBufferOne = new ArrayList<>(); //Double buffering to prevent concurrent modification errors
    private ArrayList<LayerOperation> operationBufferTwo = new ArrayList<>();

    private boolean bufferOneOpen = true;

    private int camX;
    private int camY;

    private Timer drawTimer;
    private ViewWindow window;

    private long previousCompileTime;

    int arbitraryNumber = 0;

    private class DrawUpdateTask extends TimerTask {
        @Override
        public void run() {
            window.drawImage(compileLayers(new Dimension(window.RESOLUTION_WIDTH, window.RESOLUTION_HEIGHT)));
            arbitraryNumber++;
        }
    }

    public LayerManager(ViewWindow viewWindow){
        window = viewWindow;
        window.manager = this;
        drawTimer = new Timer();
        drawTimer.scheduleAtFixedRate(new DrawUpdateTask(), 10, 50);
    }

    public ViewWindow getWindow() {
        return window;
    }

    public void addLayer (Layer toAdd){
        if (bufferOneOpen)
            operationBufferOne.add(() -> addLayerOperation(toAdd));
        else
            operationBufferTwo.add(() -> addLayerOperation(toAdd));
    }

    private void addLayerOperation (Layer toAdd){
        for (int ii = layerStack.size()-1; ii >= 0; ii--){
            if (layerStack.get(ii).getImportance() <= toAdd.getImportance()){
                layerStack.add(ii+1, toAdd);
                DebugWindow.addLayerView(toAdd, ii+1);
                System.out.printf("[LayerManager] Added layer \'%1$s\' at position %2$d\n", toAdd.getName(), ii+1);
                return;
            }
        }
        layerStack.add(0, toAdd);
        System.out.printf("[LayerManager] Added layer \'%1$s\' at position 0\n", toAdd.getName());
        DebugWindow.addLayerView(toAdd, 0);
    }

    public void removeLayer(String toRemove){
        if (bufferOneOpen)
            operationBufferOne.add(() -> removeLayerOperation(toRemove));
        else
            operationBufferTwo.add(() -> removeLayerOperation(toRemove));
    }

    public Layer getLayer(String name){
        for (Layer layer : layerStack){
            if (layer.getName().equals(name)) return layer;
        }
        return null;
    }

    private void removeLayerOperation(String toRemove){
        for (int i = 0; i < layerStack.size();){
            Layer layer = layerStack.get(i);
            if (layer.getName().equals(toRemove)) {
                layerStack.remove(layer);
                DebugWindow.removeLayerView(layer);
                System.out.printf("[LayerManager] Successful removal of layer \"%1$s\"\n", toRemove);
            } else {
                i++;
            }
        }
    }

    public void clearLayers(){
        if (bufferOneOpen)
            operationBufferOne.add(() -> {
                layerStack.clear();
                System.out.println("[LayerManager] Cleared Layer stack");
            });
        else
            operationBufferTwo.add(() -> {
                layerStack.clear();
                System.out.println("[LayerManager] Cleared Layer stack");
            });
    }

    public void removeLayer (Layer toRemove){
        removeLayer(toRemove.getName());
    }

    public void setCameraPos(int x, int y){
        if (bufferOneOpen)
            operationBufferOne.add(() -> {camX = x; camY = y;});
        else
            operationBufferTwo.add(() -> {camX = x; camY = y;});
    }

    public void moveCameraPos(int relativeX, int relativeY){
        if (bufferOneOpen)
            operationBufferOne.add(() -> {camX += relativeX; camY += relativeY;});
        else
            operationBufferTwo.add(() -> {camX += relativeX; camY += relativeY;});
    }

    public Coordinate getCameraPos() {return new Coordinate(camX, camY); }

    public ArrayList<Layer> getLayerStack() { return layerStack; }

    public long getPreviousCompileTime() {
        return previousCompileTime;
    }

    private Layer compileLayers(Dimension targetResolution){
        long startTime = System.nanoTime();
        try {
            bufferOneOpen = !bufferOneOpen;
            if (bufferOneOpen) {//Why it's you might ask? Well, don't we want to operate from the closed buffer?
                for (LayerOperation operation : operationBufferTwo) operation.doOperation();
                operationBufferTwo.clear();
            } else {
                for (LayerOperation operation : operationBufferOne) operation.doOperation();
                operationBufferOne.clear();
            }
        } catch (ConcurrentModificationException e){
            e.printStackTrace();
        }
        Layer finalResult = new Layer(new SpecialText[(int)targetResolution.getWidth()][(int)targetResolution.getHeight()], "final", 0, 0);
        for (int col = 0; col < finalResult.getCols(); col++){
            for (int row = 0; row < finalResult.getRows(); row++){
                SpecialText text = projectTextToScreen(col, row);
                Color[] colors = projectColorsToScreen(col, row);
                finalResult.editLayer(col, row, new SpecialText(text.getCharacter(), colors[1], colors[0]));
            }
        }
        finalResult.convertNullToOpaque();
        previousCompileTime = System.nanoTime() - startTime;
        return finalResult;
    }

    private SpecialText projectTextToScreen(int screenX, int screenY){
        for (int ii = layerStack.size()-1; ii >= 0; ii--){
            Layer layer = layerStack.get(ii);
            if (layer.visible) {
                SpecialText get = getSpecialTextAtScreenCoord(screenX, screenY, layer);
                if (isSpecialTextOpaque(get))
                    return get;
            }
        }
        return new SpecialText(' ');
    }

    //Slightly more complicated, but has to account for translucent layers
    private Color[] projectColorsToScreen(int screenX, int screenY){
        Color[] fgBkgColors = new Color[2];
        double alphaSum = 0;
        int redSum =   0;
        int blueSum =  0;
        int greenSum = 0;
        double remainingAlpha = 1;
        for (int ii = layerStack.size()-1; ii >= 0; ii--) {
            Layer layer = layerStack.get(ii);
            if (layer.getVisible()) {
                SpecialText get = getSpecialTextAtScreenCoord(screenX, screenY, layer);
                if (get != null) {
                    if (get.getCharacter() != ' ' && fgBkgColors[1] == null) { //Finds non-empty text, and therefore what to return as foreground color
                        int fgRed = Math.min(redSum + (int) (remainingAlpha * get.getFgColor().getRed()), 255);
                        int fgGreen = Math.min(greenSum + (int) (remainingAlpha * get.getFgColor().getGreen()), 255);
                        int fgBlue = Math.min(blueSum + (int) (remainingAlpha * get.getFgColor().getBlue()), 255);
                        fgBkgColors[1] = new Color(fgRed, fgGreen, fgBlue);
                    }
                    /**/
                    if (get.getBkgColor().getAlpha() > 0) {
                        double percentAlpha = (double) get.getBkgColor().getAlpha() / 255; //Alpha being reduced from range 0-255 to 0-1
                        alphaSum += percentAlpha * remainingAlpha;
                        redSum += (double) get.getBkgColor().getRed() * percentAlpha * remainingAlpha;
                        greenSum += (double) get.getBkgColor().getGreen() * percentAlpha * remainingAlpha;
                        blueSum += (double) get.getBkgColor().getBlue() * percentAlpha * remainingAlpha;
                        remainingAlpha *= (1 - percentAlpha);
                    }
                }
                if (alphaSum >= 1)
                    break;
            }
        }
        fgBkgColors[0] = new Color(redSum, greenSum, blueSum);
        return fgBkgColors;
    }

    private SpecialText getSpecialTextAtScreenCoord(int screenX, int screenY, Layer layer){
        if (layer.fixedScreenPos)
            return layer.getSpecialText(screenX - layer.getX(), screenY - layer.getY());
        return layer.getSpecialText(screenX - layer.getX() + camX, screenY - layer.getY() + camY);
    }

    private boolean isSpecialTextOpaque(SpecialText text) {return text != null && !(text.getCharacter() == ' ' && text.getBkgColor().getAlpha() != 255); }

    public void printLayerStack(){
        System.out.println("LAYERS: \n");
        for (Layer layer : layerStack){
            System.out.println(layer.getName());
        }
    }

    private interface LayerOperation{
        void doOperation();
    }

    /*
    Background layer processing example

    Colors in stack:
    (255, 0,   0,   80)
    (0,   255, 0,   80)
    (0,   0,   255, 80)
    (255, 255, 0,   30)
    (0,   255, 255, 140)

    Stops at semi-final color because sum of the opacity is > 255
    Total: 270

    Red Avg. :   (255 * 80 + 0 * 80 + 0 * 80 + 255 * 30) / (80 + 80 + 80 + 30) = 103
    Green Avg. : (0 * 80 + 255 * 80 + 0 * 80 + 255 * 30) / (270) = 103
    Blue avg. :  (0 * 80 + 0 * 80 + 255 * 80 + 0 * 30) / 270 = 75
    */

}

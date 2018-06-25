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

    /**
     * LayerManager:
     *
     * The object responsible for sorting and compiling Layers.
     *
     * For a Layer to display, it must be added to some LayerManager.
     * Layers are sorted by their importance. Layers of higher importance will always appear above ones with a lower priority.
     * This array of Layers is called a stack.
     *
     * The LayerManager also has a 'camera', which compounds on top of a Layer's position to get a final position during rendering.
     */

    private ArrayList<Layer> layerStack = new ArrayList<>();

    private ArrayList<LayerOperation> operationBufferOne = new ArrayList<>(); //Double buffering to prevent concurrent modification errors
    private ArrayList<LayerOperation> operationBufferTwo = new ArrayList<>();

    private ArrayList<FrameUpdateListener> frameUpdateListeners = new ArrayList<>();

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

    /**
     * Adds a Layer to the stack of Layers.
     * In avoidance of potential synchronization issues, expect a 0-50ms delay before the layer is finally displayed on the screen.
     *
     * @param toAdd The Layer to add
     */
    public void addLayer (Layer toAdd){
        if (bufferOneOpen)
            operationBufferOne.add(() -> addLayerOperation(toAdd));
        else
            operationBufferTwo.add(() -> addLayerOperation(toAdd));
    }

    /**
     * Adds a Layer to the Layer stack, placing in a location according to its importance.
     *
     * @param toAdd The Layer to add
     */
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

    /**
     * Removes all Layers from the Layer stack that match the input name.
     * Comparing every SpecialText in each layer is computationally expensive, so just doing a simple String comparison is much more efficient.
     * If your code is written properly, this should not be a problem.
     *
     * In avoidance of potential synchronization issues, expect a 0-50ms delay before the layer is finally removed.
     *
     * This function should only really be used when a Layer will not be in use in the future.
     * It is recommended to instead call Layer.setVisible(), due to it being more reliable and easier to manage on the client-end as well.
     *
     * @param toRemove The Layer to remove
     */
    public void removeLayer(String toRemove){
        if (bufferOneOpen)
            operationBufferOne.add(() -> removeLayerOperation(toRemove));
        else
            operationBufferTwo.add(() -> removeLayerOperation(toRemove));
    }

    /**
     * @param name The name of the Layer being searched for
     *
     * @return The matching Layer in the Layer stack.
     */
    public Layer getLayer(String name){
        for (Layer layer : layerStack){
            if (layer.getName().equals(name)) return layer;
        }
        return null;
    }

    /**
     * Removes every Layer in the stack that matches the input name.
     *
     * @param toRemove The name of the layer that needs to removed.
     */
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

    /**
     * Removes every Layer in the stack.
     */
    private void clearLayersOperation(){
        for (int i = 0; i < layerStack.size();) {
            DebugWindow.removeLayerView(layerStack.get(i));
            layerStack.remove(i);
        }
    }

    /**
     * Clears the entire stack of Layers.
     * In avoidance of potential synchronization issues, expect a 0-50ms delay before this operation is ran.
     */
    public void clearLayers(){
        if (bufferOneOpen)
            operationBufferOne.add(() -> {
                clearLayersOperation();
                System.out.println("[LayerManager] Cleared Layer stack");
            });
        else
            operationBufferTwo.add(() -> {
                clearLayersOperation();
                System.out.println("[LayerManager] Cleared Layer stack");
            });
    }

    /**
     * A convenient shortcut for the removeLayer(String) method.
     * Remember, ALL Layers with the name of the input layer will be removed!
     *
     * @param toRemove The layer to remove (and potentially others)
     */
    public void removeLayer (Layer toRemove){
        removeLayer(toRemove.getName());
    }

    /**
     * Sets the LayerManager's camera position to a desired location
     *
     * @param x The new camera x
     * @param y The new camera y
     */
    public void setCameraPos(int x, int y){
        camX = x;
        camY = y;
    }

    /**
     * Translates the LayerManager's camera position
     *
     * @param relativeX The x component of the movement vector
     * @param relativeY The y component of the movement vector
     */
    public void moveCameraPos(int relativeX, int relativeY){
        camX += relativeX;
        camY += relativeY;
    }

    public Coordinate getCameraPos() {return new Coordinate(camX, camY); }

    public ArrayList<Layer> getLayerStack() { return layerStack; }

    public long getPreviousCompileTime() {
        return previousCompileTime;
    }

    public void addFrameUpdateListener(FrameUpdateListener frameUpdateListener){
        frameUpdateListeners.add(frameUpdateListener);
    }

    public void removeFrameUpdateListener(FrameUpdateListener frameUpdateListener){
        frameUpdateListeners.remove(frameUpdateListener);
    }

    /**
     * Updates all FrameUpdateListeners prior to drawing a frame.
     *
     * This process is handled on a separate thread to provide protection against possible interruptions in the drawing process.
     * The full drawing process waits 5ms before drawing to allow for some, if not all, of the processes to finish.
     */
    private void frameStartUpdate(){
        Thread thread = new Thread(() -> {
            for (int i = 0; i < frameUpdateListeners.size();) {
                frameUpdateListeners.get(i).onFrameDrawStart();
                i++;
            }
        });
        thread.start();
    }

    /**
     * Updates all FrameUpdateListeners at the end of drawing a frame.
     *
     * This process is handled on a separate thread to provide protection against possible interruptions in the drawing process.
     */
    private void frameEndUpdate(){
        Thread thread = new Thread(() -> {
            for (int i = 0; i < frameUpdateListeners.size();) {
                frameUpdateListeners.get(i).onFrameDrawEnd();
                i++;
            }
        });
        thread.start();
    }

    /**
     * Adding a Layer, removing a Layer, and clearing the Layer stack is placed onto a buffer before operating.
     *
     * Every 50ms, the buffer is operated upon.
     */
    private void processLayerOperationBuffer(){
        try {
            bufferOneOpen = !bufferOneOpen;
            if (bufferOneOpen) {//Why it's you might ask? Well, don't we want to operate from the closed buffer?
                for (int i = 0; i < operationBufferTwo.size(); i++) {
                    LayerOperation operation = operationBufferTwo.get(i);
                    operation.doOperation();
                }
                operationBufferTwo.clear();
            } else {
                for (int i = 0; i < operationBufferOne.size(); i++) {
                    LayerOperation operation = operationBufferOne.get(i);
                    operation.doOperation();
                }
                operationBufferOne.clear();
            }
        } catch (ConcurrentModificationException e){
            e.printStackTrace();
        }
    }

    /**
     * Takes all the Layers in the Layer stack and 'compresses' them into ont Layer, which represents a fully rendered screen.
     *
     * @param targetResolution The screen resolution to draw to.
     * @return The Layer that represents a screen.
     */
    private Layer compileLayers(Dimension targetResolution){
        frameStartUpdate();
        sleep(5);
        long startTime = System.nanoTime();
        processLayerOperationBuffer();
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
        frameEndUpdate();
        return finalResult;
    }

    /**
     * Iterates through the Layer stack and returns the appropriate SpecialText.
     *
     * @param screenX The x coordinate of the screen to project to
     * @param screenY The y coordinate of the screen to project to
     * @return The front-most SpecialText
     */
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

    /**
     * Iterates through the Layer stack and returns a font and highlight color for a specific screen coordinate.
     *
     * The output is formatted as follows:
     *
     * colors[0] = Highlight color
     * colors[1] = Font color
     *
     * @param screenX The x coordinate of the screen to project to
     * @param screenY The y coordinate of the screen to project to.
     * @return The array of colors.
     */
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

    /**
     * Gets the SpecialText of a Layer corresponding to a screen coordinate.
     *
     * @param screenX The screen x coordinate to target
     * @param screenY The screen y coordinate to target
     * @param layer The layer to draw from
     * @return The SpecialText from the layer, accounting Layer location, camera location, and layer being fixed onto the screen.
     */
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

    private void sleep(long millis){
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private interface LayerOperation{
        void doOperation();
    }
}

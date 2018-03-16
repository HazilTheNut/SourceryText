package Engine;

import java.awt.*;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Jared on 2/18/2018.
 */
public class LayerManager {

    private ArrayList<Layer> layerStack = new ArrayList<>();

    private int camX;
    private int camY;

    private Timer drawTimer;
    private ViewWindow window;

    private class DrawUpdateTask extends TimerTask {
        @Override
        public void run() {
            window.drawImage(compileLayers(new Dimension(window.RESOLUTION_WIDTH, window.RESOLUTION_HEIGHT)));
        }
    }

    public LayerManager(ViewWindow viewWindow){
        window = viewWindow;
        window.manager = this;
        drawTimer = new Timer();
        drawTimer.scheduleAtFixedRate(new DrawUpdateTask(), 10, 50);
    }

    public void addLayer (Layer toAdd){
        layerStack.add(toAdd);
    }

    public void removeLayer (String toRemove){
        for (Layer layer : layerStack){
            if (layer.getName().equals(toRemove)) {
                layerStack.remove(layer);
                return;
            }
        }
    }

    public void setCameraPos(int x, int y){
        camX = x;
        camY = y;
    }

    public void moveCameraPos(int relativeX, int relativeY){
        camX += relativeX;
        camY += relativeY;
    }

    public Point getCameraPos() {return new Point(camX, camY); }

    public Layer compileLayers(Dimension targetResolution){
        Layer finalResult = new Layer(new SpecialText[(int)targetResolution.getWidth()][(int)targetResolution.getHeight()], "final", 0, 0);
        for (int col = 0; col < finalResult.getCols(); col++){
            for (int row = 0; row < finalResult.getRows(); row++){
                SpecialText text = projectTextToScreen(col, row);
                Color[] colors = projectColorsToScreen(col, row);
                finalResult.editLayer(col, row, new SpecialText(text.getCharacter(), colors[1], colors[0]));
            }
        }
        finalResult.convertNullToOpaque();
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

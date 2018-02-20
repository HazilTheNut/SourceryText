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
                SpecialText fg = projectTextToScreen(col, row);
                Color bkg = projectBkgToScreen(col, row);
                finalResult.editLayer(col, row, new SpecialText(fg.getCharacter(), fg.getFgColor(), bkg));
            }
        }
        return finalResult;
    }

    private SpecialText projectTextToScreen(int screenX, int screenY){
        for (int ii = layerStack.size()-1; ii >= 0; ii--){
            Layer layer = layerStack.get(ii);
            SpecialText get = getSpecialTextAtScreenCoord(screenX, screenY, layer);
            if (!isSpecialTextBlank(get))
                return get;
        }
        return new SpecialText(' ');
    }

    //Slightly more complicated, but has to account for translucent layers
    private Color projectBkgToScreen(int screenX, int screenY){
        int alphaSum = 0;
        int redSum =   0;
        int blueSum =  0;
        int greenSum = 0;
        for (int ii = layerStack.size()-1; ii >= 0; ii--){
            Layer layer = layerStack.get(ii);
            SpecialText get = getSpecialTextAtScreenCoord(screenX, screenY, layer);
            if (get.getBkgColor().getAlpha() > 0) {
                alphaSum += get.getBkgColor().getAlpha();
                redSum += get.getBkgColor().getRed() * get.getBkgColor().getAlpha();
                greenSum += get.getBkgColor().getGreen() * get.getBkgColor().getAlpha();
                blueSum += get.getBkgColor().getBlue() * get.getBkgColor().getAlpha();
            }
            if (alphaSum >= 255)
                break;
        }
        if (alphaSum == 0)
            return Color.BLACK;
        if (alphaSum < 255) alphaSum = 255;
        return new Color (redSum / alphaSum, greenSum / alphaSum, blueSum / alphaSum, 255);
    }

    private SpecialText getSpecialTextAtScreenCoord(int screenX, int screenY, Layer layer){
        return layer.getSpecialText(screenX - layer.getX() - camX, screenY - layer.getY() - camY);
    }

    private boolean isSpecialTextBlank(SpecialText text) {return (text.getCharacter() == ' ' || text.opaque) && text.getBkgColor().getAlpha() == 0; }

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

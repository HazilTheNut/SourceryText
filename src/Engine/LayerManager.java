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
        drawTimer = new Timer();
        drawTimer.scheduleAtFixedRate(new DrawUpdateTask(), 10, 50);
    }

    public void addLayer (Layer toAdd){
        layerStack.add(toAdd);
    }

    public Layer compileLayers(Dimension targetResolution){
        Layer finalResult = new Layer(new SpecialText[(int)targetResolution.getWidth()][(int)targetResolution.getHeight()], "final", 0, 0);
        for (int col = 0; col < finalResult.getCols(); col++){
            for (int row = 0; row < finalResult.getRows(); row++){
                SpecialText fg = projectTextToScreen(col, row);
                finalResult.editLayer(col, row, fg);
            }
        }
        return finalResult;
    }

    private SpecialText projectTextToScreen(int screenX, int screenY){
        for (Layer layer : layerStack){
            SpecialText get = layer.getSpecialText(screenX - layer.getX(), screenY - layer.getY());
            if (get != null)
                return get;
        }
        return new SpecialText(' ');
    }

}

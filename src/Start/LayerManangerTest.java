package Start;

import Engine.Layer;
import Engine.LayerManager;
import Engine.SpecialText;
import Engine.ViewWindow;

import java.util.Random;

/**
 * Created by Jared on 3/26/2018.
 */
public class LayerManangerTest {

    public static void main (String[] args) {

        LayerManager lm = new LayerManager(new ViewWindow());

        Random random = new Random();
        int totalSuccesses = 0;
        int numberOfTrials = 300;
        for (int i = 0; i < numberOfTrials; i++) {
            lm.clearLayers();
            for (int ii = 0; ii < 10; ii++) { //Create random set of layers
                int value = random.nextInt(10) + 1;
                Layer layer = new Layer(new SpecialText[1][1], String.valueOf(value), 0, 0);
                layer.setImportance(value);
                lm.addLayer(layer);
            }
            boolean isLayerStackIncorrect = false;
            for (int ii = 0; ii < lm.getLayerStack().size() - 1; ii++) { //Verify if layer set is ordered
                System.out.printf("ii:%1$d size:%2$d\n", ii, lm.getLayerStack().size());
                if (ii < lm.getLayerStack().size()-2) {
                    if (lm.getLayerStack().get(ii).getImportance() > lm.getLayerStack().get(ii + 1).getImportance()) {
                        isLayerStackIncorrect = true;
                        break;
                    }
                }
            }
            if (!isLayerStackIncorrect) totalSuccesses++;
        }

        System.out.printf("Success rate: %1$d/%2$d\n", totalSuccesses, numberOfTrials);
    }
}

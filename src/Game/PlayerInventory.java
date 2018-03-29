package Game;

import Data.LayerImportances;
import Engine.Layer;
import Engine.LayerManager;
import Engine.SpecialText;

import java.awt.*;
import java.util.ArrayList;

/**
 * Created by Jared on 3/29/2018.
 */
class PlayerInventory implements MouseInputReceiver{

    private ArrayList<String> items = new ArrayList<>();

    private Layer invLayer;
    private boolean isShowing = false;

    public void addItem(String item) { items.add(item); }

    void show(LayerManager lm){
        invLayer = new Layer(new SpecialText[20][100], "inventory", 0, 1, LayerImportances.MENU);
        invLayer.fixedScreenPos = true;
        Color bkg = new Color(35, 35, 35);
        System.out.printf("[PlayerInventory] Size: %1$d\n", items.size());
        int height = items.size() + 1;
        for (int row = 0; row < height; row++){ //Draw base inv panel
            for (int col = 0; col < invLayer.getCols(); col++){
                invLayer.editLayer(col, row, new SpecialText(' ', Color.WHITE, bkg));
            }
        }
        for (int col = 0; col < invLayer.getCols(); col++){ //Create top border
            invLayer.editLayer(col, 0,        new SpecialText('#', Color.GRAY, bkg));
            //invLayer.editLayer(col, height-1, new SpecialText('#', Color.GRAY, bkg));
        }
        for (int ii = 0; ii < items.size(); ii++){ //Inscribe inv contents
            if (ii % 2 == 1){
                for (int col = 0; col < invLayer.getCols()-2   ; col++){
                    invLayer.editLayer(col, ii+1, new SpecialText(' ', Color.WHITE, new Color(45, 45, 45)));
                }
            }
            invLayer.inscribeString(items.get(ii), 0, ii+1);
        }
        invLayer.inscribeString("Inventory", 1, 0, new Color(255, 255, 200));
        lm.addLayer(invLayer);
        isShowing = true;
    }

    void close(LayerManager lm){
        lm.removeLayer(invLayer);
        isShowing = false;
    }

    public boolean isShowing() { return isShowing; }


    @Override
    public void onMouseMove(Coordinate pos) {

    }

    @Override
    public boolean onMouseClick(Coordinate pos) {
        return false;
    }
}

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
public class PlayerInventory {

    private ArrayList<String> items = new ArrayList<>();

    private Layer invLayer;
    private boolean isShowing = false;

    public void addItem(String item) { items.add(item); }

    public void show(LayerManager lm){
        invLayer = new Layer(new SpecialText[23][100], "inventory", 0, 1, LayerImportances.MENU);
        invLayer.fixedScreenPos = true;
        Color bkg = new Color(35, 35, 35);
        int height = items.size() + 2;
        for (int row = 0; row < height; row++){ //Draw base inv panel
            for (int col = 0; col < invLayer.getCols(); col++){
                invLayer.editLayer(col, row, new SpecialText(' ', Color.WHITE, bkg));
            }
        }
        for (int ii = 1; ii < items.size(); ii++){ //Inscribe inv contents
            invLayer.inscribeString(items.get(ii), 1, ii);
        }
        for (int col = 0; col < invLayer.getCols(); col++){ //Create top-bottom borders
            invLayer.editLayer(col, 0,        new SpecialText('#', Color.GRAY, bkg));
            invLayer.editLayer(col, height-1, new SpecialText('#', Color.GRAY, bkg));
        }
        for (int row = 0; row < height; row++){ //Create left-right borders
            invLayer.editLayer(0,                  row, new SpecialText('#', Color.GRAY, bkg));
            invLayer.editLayer(invLayer.getCols()-1, row, new SpecialText('#', Color.GRAY, bkg));
        }
        invLayer.inscribeString("Inventory", 1, 0);
        lm.addLayer(invLayer);
        isShowing = true;
    }

    public void close(LayerManager lm){
        lm.removeLayer(invLayer);
        isShowing = false;
    }

    public boolean isShowing() { return isShowing; }
}

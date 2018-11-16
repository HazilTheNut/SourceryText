package Game;

import Engine.Layer;
import Engine.LayerManager;
import Engine.SpecialText;

import java.awt.*;

public class DialogueOptionsMenu extends QuickMenu {

    public DialogueOptionsMenu(LayerManager lm, Player player) {
        super(lm, player, lm.getWindow().RESOLUTION_WIDTH);
    }

    @Override
    protected void positionMenu() {
        int y = lm.getWindow().RESOLUTION_HEIGHT - Math.max(menuItems.size() + 2, 3);
        menuLayer.setPos(0, y);
    }

    @Override
    protected void drawMenu(String title) {
        Layer tempLayer = new Layer(menuLayer.getCols(), menuItems.size() + 2, "do temp", 0, 0, 0);
        tempLayer.fillLayer(new SpecialText(' ', Color.WHITE, TextBox.bkg));
        for (int col = 0; col < tempLayer.getCols(); col++) {
            tempLayer.editLayer(col, 0, new SpecialText('_', TextBox.banner, TextBox.banner.darker()));
        }
        for (int i = 0; i < menuItems.size(); i++) {
            tempLayer.inscribeString(menuItems.get(i).name, 1, i+1, menuItems.get(i).color);
        }
        tempLayer.inscribeString(title, (int)Math.floor((MENU_WIDTH - (double)title.length())/2), 0, new Color(220, 255, 255));
        menuLayer.transpose(tempLayer);
    }
}

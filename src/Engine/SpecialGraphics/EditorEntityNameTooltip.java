package Engine.SpecialGraphics;

import Data.EntityStruct;
import Data.LevelData;
import Engine.ViewWindow;

import java.awt.*;

/**
 * Created by Jared on 3/24/2018.
 */
public class EditorEntityNameTooltip implements SpecialGraphics {

    private LevelData ldata;
    private ViewWindow window;
    private int mouseX;
    private int mouseY;
    private int dataX;
    private int dataY;

    public EditorEntityNameTooltip(LevelData levelData, ViewWindow viewWindow) {
        ldata = levelData;
        window = viewWindow;
    }

    public void updateMousePosition(int x, int y, int dataX, int dataY){
        mouseX = x;
        mouseY = y;
        this.dataX = dataX;
        this.dataY = dataY;
    }

    @Override
    public void paint(Graphics g) {
        g.setFont(new Font(Font.DIALOG, Font.PLAIN, 12));
        EntityStruct entity = ldata.getEntityAt(dataX, dataY);
        if (entity != null) {
            String entityName = entity.getEntityName();
            if (entityName.length() > 0 && ldata.getEntityLayer().getVisible()) {
                g.setColor(new Color(50, 50, 50));
                int stringWidth = g.getFontMetrics().stringWidth(entityName);
                int stringHeight = g.getFontMetrics().getHeight();
                int startX = mouseX + 5;
                startX = (startX + stringWidth < window.getWidth()) ? startX : startX - stringWidth;
                int startY = mouseY + 15;
                g.drawRect(startX, startY, stringWidth + 2, stringHeight + 2);
                g.setColor(new Color(40, 40, 40));
                g.fillRect(startX + 1, startY + 1, stringWidth, stringHeight);
                g.setColor(Color.WHITE);
                g.drawString(entityName, startX + 1, startY + stringHeight - 1);
            }
        }
    }
}

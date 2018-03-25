package Engine.SpecialGraphics;

import Data.LevelData;

import java.awt.*;

/**
 * Created by Jared on 3/24/2018.
 */
public class EditorWarpZoneFilePathView implements SpecialGraphics {

    private LevelData ldata;
    private int mouseX;
    private int mouseY;

    public EditorWarpZoneFilePathView(LevelData levelData) { ldata = levelData; }

    public void updateMousePosition(int x, int y){
        mouseX = x;
        mouseY = y;
    }

    @Override
    public void paint(Graphics g) {
        if (ldata.getSelectedWarpZone() != null){
            Font font = new Font(Font.SANS_SERIF, Font.ITALIC, 12);
            g.setFont(font);
            String filePath = ldata.getSelectedWarpZone().getRoomFilePath();
            if (filePath.length() > 0) {
                int startX = mouseX + 5;
                int startY = mouseY + 15;
                g.setColor(new Color(50, 50, 50));
                FontMetrics metrics = g.getFontMetrics();
                g.drawRect(startX, startY, metrics.stringWidth(filePath) + 2, metrics.getHeight() + 2);
                g.setColor(new Color(40, 40, 40));
                g.fillRect(startX+1, startY+1, metrics.stringWidth(filePath), metrics.getHeight());
                g.setColor(Color.WHITE);
                g.drawString(filePath, startX + 1, startY + metrics.getHeight() - 1);
            }
        }
    }
}

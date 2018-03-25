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
            g.setFont(new Font(Font.DIALOG, Font.PLAIN, 12));
            String filePath = ldata.getSelectedWarpZone().getRoomFilePath();
            if (filePath.length() > 0) {
                int startX = mouseX + 5;
                int startY = mouseY + 15;
                g.setColor(new Color(50, 50, 50));
                int stringWidth = g.getFontMetrics().stringWidth(filePath);
                int stringHeight = g.getFontMetrics().getHeight();
                g.drawRect(startX, startY, stringWidth + 2, stringHeight + 2);
                g.setColor(new Color(40, 40, 40));
                g.fillRect(startX+1, startY+1, stringWidth, stringHeight);
                g.setColor(Color.WHITE);
                g.drawString(filePath, startX + 1, startY + stringHeight - 1);
            }
        }
    }
}

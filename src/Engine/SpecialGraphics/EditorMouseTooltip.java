package Engine.SpecialGraphics;

import Data.EntityStruct;
import Data.LevelData;
import Data.WarpZone;
import Engine.SpecialText;
import Engine.ViewWindow;

import java.awt.*;
import java.util.ArrayList;

/**
 * Created by Jared on 3/24/2018.
 */
public class EditorMouseTooltip implements SpecialGraphics {

    /**
     * EditorMouseTooltip:
     *
     * The SpecialGraphics responsible for displaying the mouse tooltip for the Level Editor.
     */

    private LevelData ldata;
    private ViewWindow window;
    private int mouseX;
    private int mouseY;
    private int dataX;
    private int dataY;

    public boolean showCoordinate = false;
    public boolean showAdvanced = false;

    public EditorMouseTooltip(LevelData levelData, ViewWindow viewWindow) {
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
        ArrayList<String> tooltipText = getTooltip();
        if (tooltipText.size() > 0) {
            int textWidth = computeTooltipWidth(tooltipText, g.getFontMetrics());
            int lineHeight = g.getFontMetrics().getHeight();
            int textHeight = g.getFontMetrics().getHeight() * tooltipText.size();
            g.setColor(new Color(50, 50, 50));
            int startX = mouseX + 5;
            startX = (startX + textWidth < window.getWidth()) ? startX : startX - textWidth; //Figures out if the tooltip should be moved to the other side of the cursor.
            int startY = mouseY + 15;
            g.drawRect(startX, startY, textWidth + 1, textHeight + 1); //Draw box outline
            g.setColor(new Color(40, 40, 40));
            g.fillRect(startX + 1, startY + 1, textWidth, textHeight); //Fill in box
            g.setColor(Color.WHITE);
            if (ldata.getBackdrop().isLayerLocInvalid(dataX, dataY))
                g.setColor(new Color(150, 150, 150)); //Gray out if cursor outside of level bounds
            for (int ii = 0; ii < tooltipText.size(); ii++){
                g.drawString(tooltipText.get(ii), startX + 1, startY + (lineHeight * (ii+1)) - 1); //Draw the strings
            }
        }
    }

    //Gets the array of strings to be displayed for the tooltip
    private ArrayList<String> getTooltip(){
        ArrayList<String> output = new ArrayList<>(); //Create new array
        EntityStruct e = ldata.getEntityAt(dataX, dataY);
        if (e != null && ldata.getEntityLayer().getVisible()){
            output.add(e.getEntityName()); //Add Entity name if there is one
        }
        WarpZone wz = ldata.getSelectedWarpZone();
        if (wz != null && ldata.getWarpZoneLayer().getVisible() && !wz.getRoomFilePath().equals("")){
            output.add(wz.getRoomFilePath()); //Add Warp Zone destination file path if there is one
        }
        if (showCoordinate || showAdvanced)
            output.add(String.format("[ %1$d, %2$d ]", dataX, dataY)); //Display coordinate
        if (showAdvanced && !ldata.getBackdrop().isLayerLocInvalid(dataX, dataY)){
            SpecialText backdropText = ldata.getBackdrop().getSpecialText(dataX, dataY);
            if (backdropText != null) {
                output.add(backdropText.toString()); //Display backdrop SpecialText data.
            }
            output.add(String.format("Tile id: %1$d", ldata.getTileId(dataX, dataY))); //Get tile id
        }
        return output;
    }

    private int computeTooltipWidth(ArrayList<String> strings, FontMetrics metrics){
        int max = 0;
        for (String str : strings) max = Math.max(max, metrics.stringWidth(str));
        return max;
    }
}

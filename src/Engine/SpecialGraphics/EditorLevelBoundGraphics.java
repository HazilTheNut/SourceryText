package Engine.SpecialGraphics;

import Data.LevelData;
import Engine.LayerManager;
import Engine.ViewWindow;

import java.awt.*;

/**
 * Created by Jared on 3/20/2018.
 */
public class EditorLevelBoundGraphics implements SpecialGraphics {

    /**
     * EditorLevelBoundGraphics:
     *
     * The SpecialGraphics responsible for drawing the border rectangle for the Level Editor
     */

    private ViewWindow window;
    private LayerManager manager;
    private LevelData ldata;

    public EditorLevelBoundGraphics(ViewWindow viewWindow, LayerManager layerManager, LevelData levelData){
        window = viewWindow;
        manager = layerManager;
        ldata = levelData;
    }

    @Override
    public void paint(Graphics g) {
        g.setColor(new Color(179, 176, 170));
        //Helpful pre-calculated stuff
        int leftMargin   = window.HOR_MARGIN;
        int rightMargin  = window.getWidth() - window.HOR_MARGIN;
        int topMargin    = window.VER_MARGIN;
        int bottomMargin = window.getHeight() - window.VER_MARGIN;
        //Define bounds of drawn rectangle
        int left   = -manager.getCameraPos().getX() *window.HOR_SEPARATION+window.HOR_MARGIN;
        int top    = -manager.getCameraPos().getY() *window.VER_SEPARATION+window.VER_MARGIN;
        int right  = left + ldata.getTileDataLayer().getCols()*window.HOR_SEPARATION; //left, top, right, and bottom define bounds of drawn rectangle
        int bottom = top  + ldata.getTileDataLayer().getRows()*window.VER_SEPARATION;
        //Draw!
        drawBoundedRectangle(g, left-1, top, right, bottom+1, leftMargin, topMargin, rightMargin, bottomMargin);
        drawBoundedRectangle(g, left-3, top-2, right+2, bottom+3, leftMargin, topMargin, rightMargin, bottomMargin);
    }

    //'Bounded' in this context means the rectangle obeys the margin lines of ViewWindow.
    private void drawBoundedRectangle(Graphics g, int left, int top, int right, int bottom, int leftMargin, int topMargin, int rightMargin, int bottomMargin){
        if (left > leftMargin && left < rightMargin){
            g.drawLine(left, Math.max(Math.min(topMargin, bottom), top), left, Math.max(Math.min(bottomMargin, bottom), top));
        }
        if (right > leftMargin && right < rightMargin){
            g.drawLine(right, Math.max(Math.min(topMargin, bottom), top), right, Math.max(Math.min(bottomMargin, bottom), top));
        }
        if (top > topMargin && top < bottomMargin){
            g.drawLine(Math.max(Math.min(left, rightMargin), leftMargin), top, Math.max(Math.min(right, rightMargin), leftMargin), top);
        }
        if (bottom > topMargin && bottom < bottomMargin){
            g.drawLine(Math.max(Math.min(left, rightMargin), leftMargin), bottom, Math.max(Math.min(right, rightMargin), leftMargin), bottom);
        }
    }
}

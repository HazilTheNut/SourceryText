package Data;

import java.io.Serializable;

/**
 * Created by Jared on 3/10/2018.
 */
public class WarpZone implements Serializable{

    private int xpos;
    private int ypos;
    private int width;
    private int height;
    private String roomFilePath = "";
    private int newRoomStartX;
    private int newRoomStartY;

    private boolean isSelected; //Use only in editor

    public WarpZone(int x, int y, int w, int h){
        setPos(x, y);
        setSize(w, h);
    }

    public void setPos(int x, int y) {
        xpos = x;
        ypos = y;
    }

    public int getXpos() {
        return xpos;
    }

    public int getYpos() {
        return ypos;
    }

    public void setSize(int w, int h){
        width = w;
        height = h;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void setTranslation(int newRoomX, int newRoomY){
        newRoomStartX = newRoomX;
        newRoomStartY = newRoomY;
    }

    public int getNewRoomStartX() {
        return newRoomStartX;
    }

    public int getNewRoomStartY() {
        return newRoomStartY;
    }

    public String getRoomFilePath() {
        return roomFilePath;
    }

    public void setRoomFilePath(String roomFilePath) {
        this.roomFilePath = roomFilePath;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}

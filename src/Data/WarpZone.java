package Data;

import java.io.Serializable;

/**
 * Created by Jared on 3/10/2018.
 */
public class WarpZone implements Serializable{

    /**
     * WarpZone:
     *
     * The WarpZone is a data structure that defines a Warp Zone linking to another room.
     *
     * It contains:
     *  > xpos          : The x position of upper-left corner of Warp Zone. //This should be a Coordinate, but WarpZone has some older code in it
     *  > ypos          : The y position of upper-left corner of Warp Zone. //To be honest, it works just as well. Not as neat, but whatever.
     *  > width         : The width of the WarpZone. A width of zero = infinitely thin.
     *  > height        : The height of the WarpZone. A height of zero = infinitely thin.
     *  > roomFilePath  : The relative file path to the new level (the .lda file)
     *  > newRoomStartX : The x position of the upper-left corner of the "output" of the WarpZone //Yeah, could be a Coordinate too
     *  > newRoomStartY : The y position of the upper-left corner of the "output" of the WarpZone
     */

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

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

    public WarpZone copy(){
        WarpZone wz = new WarpZone(xpos, ypos, width, height);
        wz.setTranslation(newRoomStartX, newRoomStartY);
        wz.setRoomFilePath(roomFilePath);
        return wz;
    }

    public boolean isInsideZone(Coordinate loc){
        return (loc.getX() - xpos < width && loc.getX() >= xpos && loc.getY() - ypos < height && loc.getY() >= ypos);
    }
}

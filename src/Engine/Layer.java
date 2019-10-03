package Engine;

import Data.SerializationVersion;
import Data.Coordinate;

import java.awt.*;
import java.io.Serializable;
import java.util.Random;

/**
 * Created by Jared on 2/18/2018.
 */
public class Layer implements Serializable{

    /**
     * Layer:
     *
     * A grouping of SpecialText's into a rectangular matrix.
     * If the SpecialText is a 'pixel', then the Layer is an 'image'
     *
     * It contains:
     *  > textMatrix     : A 2D array of SpecialTexts that are the contents of this layer
     *  > xpos           : The x position of the layer. World-coordinates if not fixedScreenPos, and Screen-coordinates if it is fixedScreenPos //This could be a Coordinate. *sigh* old code...
     *  > ypos           : The y position of the layer. World-coordinates if not fixedScreenPos, and Screen-coordinates if it is fixedScreenPos
     *  > name           : The name of the layer. Useful when trying to compare this layer to other ones. Layers of the same name are considered to be the same layer.
     *  > importance     : An integer that indicates the 'priority' of the layer. Layers of higher priority will display on top of those with lower priorities.
     *  > fixedScreenPos : A boolean that indicates whether the Layer should obey the LayerManager's camera position. Set to true for creating HUD and menus and whatnot.
     *  > visible        : A boolean that indicates whether the Layer should be displayed. Very useful if the layer needs to pop in and out of existence, and you don't want to add and remove this layer a bunch from the LayerManager
     */

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    private SpecialText[][] textMatrix; //In form textMatrix[col][row]
    private int xpos = 0;
    private int ypos = 0;
    private String name;

    private int importance = 0;

    public boolean fixedScreenPos = false;
    boolean visible = true;

    public Layer (SpecialText[][] layerData, String layerName, int x, int y){
        textMatrix = layerData;
        name = layerName;
        xpos = x;
        ypos = y;
    }

    public Layer (SpecialText[][] layerData, String layerName, int x, int y, int priority){
        textMatrix = layerData;
        name = layerName;
        xpos = x;
        ypos = y;
        importance = priority;
    }

    // Harder to read than the other constructors (because there are two pairs of integers), but is written more concisely. Pick your poison, I guess.
    public Layer (int w, int h, String layerName, int x, int y, int priority){
        textMatrix = new SpecialText[w][h];
        name = layerName;
        xpos = x;
        ypos = y;
        importance = priority;
    }

    public Layer (String[][] layerData, String layerName, int x, int y){
        textMatrix = new SpecialText[layerData.length][layerData[0].length];
        for (int col = 0; col < layerData.length; col ++){
            for (int row = 0; row < layerData[0].length; row++){
                textMatrix[col][row] = new SpecialText(layerData[col][row].charAt(0));
            }
        }
        name = layerName;
        xpos = x;
        ypos = y;
    }

    /**
     * Generates an exact copy of this layer, but not as a pointer to this Layer.
     * WARNING! The output layer's name matches this layer's name, and together are considered one to LayerManager
     * @return A copy of this layer
     */
    public Layer copy(){
        Layer copy = new Layer(new SpecialText[getCols()][getRows()], name, xpos, ypos);
        for (int col = 0; col < textMatrix.length; col++){
            for (int row = 0; row < textMatrix[0].length; row++){
                copy.editLayer(col, row, textMatrix[col][row]);
            }
        }
        copy.setVisible(true);
        copy.fixedScreenPos = fixedScreenPos;
        return copy;
    }

    /**
     * Copies the SpecialText matrix of another layer onto this layer's text matrix.
     * @param layer The other layer to transpose from.
     */
    public void transpose(Layer layer){
        SpecialText[][] tempMatrix = new SpecialText[layer.getCols()][layer.getRows()];
        for (int col = 0; col < tempMatrix.length; col++){
            for (int row = 0; row < tempMatrix[0].length; row++){
                tempMatrix[col][row] = layer.getSpecialText(col, row);
            }
        }
        textMatrix = tempMatrix;
    }

    /**
     * Fills layer with info.txt opaque characters in replacement of null SpecialTexts
     */
    public void convertNullToOpaque(){
        for (int col = 0; col < textMatrix.length; col++){
            for (int row = 0; row < textMatrix[0].length; row++){
                if (textMatrix[col][row] == null) textMatrix[col][row] = new SpecialText(' ', Color.WHITE, Color.BLACK);
            }
        }
    }

    /**
     * Fills layer with null SpecialTexts
     */
    public void clearLayer(){
        for (int col = 0; col < textMatrix.length; col++){
            for (int row = 0; row < textMatrix[0].length; row++){
                textMatrix[col][row] = null;
            }
        }
    }

    /**
     * Fills layer with an input SpecialText
     */
    public void fillLayer(SpecialText text){
        for (int col = 0; col < textMatrix.length; col++){
            for (int row = 0; row < textMatrix[0].length; row++){
                textMatrix[col][row] = text;
            }
        }
    }

    /**
     * Fills layer with an input SpecialText, but in a smaller rectangle defined by two corners
     * @param text The SpecialText to fill with
     * @param topLeft The top-left corner of the rectangle
     * @param bottomRight The bottom-right corner of the rectangle
     */
    public void fillLayer(SpecialText text, Coordinate topLeft, Coordinate bottomRight){
        for (int col = topLeft.getX(); col <= bottomRight.getX(); col++){
            for (int row = topLeft.getY(); row <= bottomRight.getY(); row++){
                editLayer(col, row, text);
            }
        }
    }

    /**
     * Inserts the SpecialText contents of one layer into this Layer
     * @param other The layer the insert into this one
     * @param start The starting coordinate for where the insertion takes place
     */
    public void insert(Layer other, Coordinate start){
        insert(other, start, new Coordinate(0, 0));
    }

    public void insert(Layer other, Coordinate start, Coordinate sampleOffset){
        for (int col = 0; col < other.getCols(); col++){
            for (int row = 0; row < other.getRows(); row++){
                editLayer(col + start.getX(), row + start.getY(), other.getSpecialText(col + sampleOffset.getX(), row + sampleOffset.getY()));
            }
        }
    }

    /**
     * Replaces every SpecialText in the layer that matches an input SpecialText with a different one
     * @param find What to replace
     * @param replace What to replace with
     */
    public void findAndReplace(SpecialText find, SpecialText replace){
        for (int col = 0; col < textMatrix.length; col++){
            for (int row = 0; row < textMatrix[0].length; row++){
                SpecialText get = getSpecialText(col, row);
                if (get != null && get.equals(find))
                    editLayer(col, row, replace);
                else if (get == null && find == null){
                    editLayer(col, row, replace);
                }
            }
        }
    }

    /**
     * Performs a find-and-replace function on the text matrix.
     * @param find The SpecialText to look for
     * @param replace The SpecialText to replace with
     * @param chance The 0-100% chance to perform the operation.
     */
    public void findAndReplace(SpecialText find, SpecialText replace, int chance){
        System.out.printf("[Layer.findAndReplace] Chance: %1$d\n", chance);
        if (chance <= 0) return;
        if (chance >= 100) {
            findAndReplace(find, replace);
            return;
        }
        Random random = new Random();
        int maxFailures = (int)(150f / chance);
        int numFailures = 0;
        for (int col = 0; col < textMatrix.length; col++){
            for (int row = 0; row < textMatrix[0].length; row++){
                SpecialText get = getSpecialText(col, row);
                if ((get != null && get.equals(find)) || (get == null && find == null)) {
                    if ((int) (random.nextDouble() * 100) < chance || numFailures == maxFailures) {
                        editLayer(col, row, replace);
                        numFailures = 0;
                    } else {
                        numFailures++;
                    }
                }
            }
        }
    }

    public void resizeLayer(int width, int height, int startX, int startY){
        SpecialText[][] newMatrix = new SpecialText[width][height];
        for (int col = 0; col < textMatrix.length; col++){
            for (int row = 0; row < textMatrix[0].length; row++){
                int x = col + startX;
                int y = row + startY;
                if (x >= 0 && x < newMatrix.length && y >= 0 && y < newMatrix[0].length){
                    newMatrix[x][y] = textMatrix[col][row];
                }
            }
        }
        textMatrix = newMatrix;
    }

    public SpecialText getSpecialText (int col, int row){
        //System.out.println(String.format("Layer getSpecialText: [%1$d,%2$d] of [%3$dx%4$d]", col, row, textMatrix.length, textMatrix[0].length));
        if (isLayerLocInvalid(col, row) || textMatrix[col][row] == null)
            return null;
        return textMatrix[col][row];
    }

    /**
     * A very special version of getSpecialText() concerned only with rendering.
     *
     * @param lm The LayerManager drawing the frame
     * @param layerPos The position relative to this layer
     * @param screenPos The position relative to the screen being drawn to
     * @param position The "vertical" position of this layer in the LayerManager's stack. + goes "up" and - goes "down"
     * @return The SpecialText for the LayerManager to process.
     */
    public SpecialText provideTextForDisplay(LayerManager lm, Coordinate layerPos, Coordinate screenPos, int position){
        return getSpecialText(layerPos);
    }

    public SpecialText getSpecialText (Coordinate pos){
        return getSpecialText(pos.getX(), pos.getY());
    }

    public void editLayer (int col, int row, char text){
        if (isLayerLocInvalid(col, row)) return;
        textMatrix[col][row] = new SpecialText(text);
    }

    public void editLayer (int col, int row, SpecialText text) {
        if (isLayerLocInvalid(col, row)) return;
        textMatrix[col][row] = text;
    }

    public void editLayer (Coordinate loc, SpecialText text){
        editLayer(loc.getX(), loc.getY(), text);
    }

    public boolean isLayerLocInvalid(int col, int row){
        return (col < 0 || col >= textMatrix.length || row < 0 || row >= textMatrix[0].length);
    }

    public boolean isLayerLocInvalid(Coordinate pos){
        return (pos.getX() < 0 || pos.getX() >= textMatrix.length || pos.getY() < 0 || pos.getY() >= textMatrix[0].length);
    }

    public int getCols(){ return textMatrix.length; }
    public int getRows(){ return textMatrix[0].length; }

    public int getX() { return xpos; }
    public int getY() { return ypos; }
    public Coordinate getPos() { return new Coordinate(xpos, ypos); }

    public void setPos(int x, int y) { xpos = x; ypos = y; }
    public void movePos(int x, int y) { xpos += x; ypos += y; }

    public void setPos(Coordinate loc) {xpos = loc.getX(); ypos = loc.getY(); }

    public void setVisible(boolean visible) { this.visible = visible; }

    public boolean getVisible(){ return visible; }

    public int getImportance() { return importance; }

    public void setImportance(int importance) { this.importance = importance; }

    private SpecialText[][] getTextMatrix() {
        return textMatrix;
    }

    public void printLayer(){
        String output = "";
        for (int row = 0; row < textMatrix[0].length; row++){
            for (int col = 0; col < textMatrix.length; col++){
                if (textMatrix[col][row] == null)
                    output += "Ɵ";
                else
                    output += textMatrix[col][row].getStr();
            }
            output += "|\n";
        }
        System.out.println("LAYER: " + name + ":\n" + output + "-~-~-~-~-~-~");
    }

    public void inscribeString (String str, int col, int row){
        inscribeString(str, col, row, Color.WHITE);
    }

    public void inscribeString (String str, int col, int row, Color fontColor){
        inscribeString(str, col, row, fontColor, false);
    }

    public void inscribeString (String str, int col, int row, Color fontColor, boolean wrapping){
        int drawX = col;
        int drawY = row;
        for (int index = 0; index < str.length(); index++){
            if (isLayerLocInvalid(drawX, drawY)){ //If reached past the border of the layer
                if (wrapping && drawY < getRows()) { //If wrapping, move to next line (unless you hit the bottom of the layer, which should then terminate the process
                    drawX = col; //Reset to input x coordinate
                    drawY++; //move down one
                } else
                    return;
            }
            if (wrapping && str.charAt(index) == '\n'){ //Wrap text if encounter newline character
                drawX = col;
                drawY++;
            } else {
                if (getSpecialText(drawX, drawY) != null)
                    editLayer(drawX, drawY, new SpecialText(str.charAt(index), fontColor, getSpecialText(drawX, drawY).getBkgColor()));
                else
                    editLayer(drawX, drawY, new SpecialText(str.charAt(index), fontColor));
                drawX++;
            }
        }
    }

    public String getName() { return name; }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Layer) {
            Layer layer = (Layer) obj;
            return layer.getName().equals(name);
        }
        return false;
    }
}

package Engine;

import com.sun.istack.internal.Nullable;

import java.awt.*;
import java.io.Serializable;

/**
 * Created by Jared on 2/18/2018.
 */
public class Layer implements Serializable{

    private SpecialText[][] textMatrix; //In form textMatrix[col][row]
    private int xpos = 0;
    private int ypos = 0;
    private String name;

    public boolean fixedScreenPos = false;
    boolean visible = true;

    public Layer (SpecialText[][] layerData, String layerName, int x, int y){
        textMatrix = layerData;
        name = layerName;
        xpos = x;
        ypos = y;
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

    public void transpose(Layer layer){
        textMatrix = new SpecialText[layer.getCols()][layer.getRows()];
        for (int col = 0; col < textMatrix.length; col++){
            for (int row = 0; row < textMatrix[0].length; row++){
                textMatrix[col][row] = layer.getSpecialText(col, row);
            }
        }
    }

    /**
     * Fills layer with blank opaque characters in replacement of null SpecialTexts
     */
    public void convertNullToOpaque(){
        for (int col = 0; col < textMatrix.length; col++){
            for (int row = 0; row < textMatrix[0].length; row++){
                if (textMatrix[col][row] == null) textMatrix[col][row] = new SpecialText(' ');
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
        if (isLayerLocInvalid(col, row))
            return null;
        return textMatrix[col][row];
    }

    public void editLayer (int col, int row, char text){
        if (isLayerLocInvalid(col, row)) return;
        textMatrix[col][row] = new SpecialText(text);
    }

    public void editLayer (int col, int row, SpecialText text) {
        if (isLayerLocInvalid(col, row)) return;
        textMatrix[col][row] = text;
    }

    public boolean isLayerLocInvalid(int col, int row){
        return (col < 0 || col >= textMatrix.length || row < 0 || row >= textMatrix[0].length);
    }

    public int getCols(){ return textMatrix.length; }
    public int getRows(){ return textMatrix[0].length; }

    public int getX() { return xpos; }
    public int getY() { return ypos; }
    public void setPos(int x, int y) { xpos = x; ypos = y; }
    public void movePos(int x, int y) { xpos += x; ypos += y; }

    public void setVisible(boolean visible) { this.visible = visible; }

    public boolean getVisible(){ return visible; }

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
        for (int index = 0; index < str.length(); index++){
            if (isLayerLocInvalid(col + index, row)) return;
            editLayer(col + index, row, new SpecialText(str.charAt(index), Color.WHITE, Color.BLACK));
        }
    }

    public String getName() { return name; }
}

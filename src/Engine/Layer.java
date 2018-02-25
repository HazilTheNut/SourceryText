package Engine;

import com.sun.istack.internal.Nullable;

import java.awt.*;

/**
 * Created by Jared on 2/18/2018.
 */
public class Layer {

    private SpecialText[][] textMatrix; //In form textMatrix[col][row]
    private int xpos = 0;
    private int ypos = 0;
    private String name;

    public boolean fixedScreenPos = false;

    public int getX() { return xpos; }

    public int getY() { return ypos; }


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

    /**
     * Fills layer with blank opaque characters
     */
    void blankOpacifyLayer(){
        for (int col = 0; col < textMatrix.length; col++){
            for (int row = 0; row < textMatrix[0].length; row++){
                if (textMatrix[col][row] == null) textMatrix[col][row] = new SpecialText(' ');
            }
        }
    }

    @Nullable
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

    private boolean isLayerLocInvalid(int col, int row){
        return (col < 0 || col >= textMatrix.length || row < 0 || row >= textMatrix[0].length);
    }

    public int getCols(){ return textMatrix.length; }
    public int getRows(){ return textMatrix[0].length; }

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
}

package Engine;

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
        sanitateLayer();
    }

    public Layer (String[][] layerData, String layerName, int x, int y){
        textMatrix = new SpecialText[layerData.length][layerData[0].length];
        for (int col = 0; col < layerData.length; col ++){
            for (int row = 0; row < layerData[0].length; row++){
                textMatrix[col][row] = new SpecialText(layerData[col][row].toCharArray()[0]);
            }
        }
        name = layerName;
        xpos = x;
        ypos = y;
        sanitateLayer();
    }

    private void sanitateLayer(){
        for (int col = 0; col < textMatrix.length; col++){
            for (int row = 0; row < textMatrix[0].length; row++){
                if (textMatrix[col][row] == null) textMatrix[col][row] = new SpecialText(' ');
            }
        }
    }

    public SpecialText getSpecialText (int col, int row){ return textMatrix[col][row]; }

    public void editLayer (int col, int row, char text){ textMatrix[col][row] = new SpecialText(text); }

    public void editLayer (int col, int row, SpecialText text) { textMatrix[col][row] = text; }
}

package Editor.DrawTools;

import Editor.EditorTextPanel;
import Engine.Layer;
import Engine.SpecialText;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Jared on 3/4/2018.
 */
public class ArtPick extends DrawTool {

    /**
     * ArtPick:
     *
     * A Tool that gets the SpecialText under the cursor and adds it to the Text Panel
     */

    private EditorTextPanel textPanel;

    public ArtPick(EditorTextPanel panel) { textPanel = panel; }

    @Override
    public void onActivate(JPanel panel) {
        TOOL_TYPE = TYPE_ART;
    }

    @Override
    public void onDrawStart(Layer layer, Layer highlight, int col, int row, SpecialText text) {
        SpecialText pickedText = layer.getSpecialText(col, row);
        if (pickedText != null) {
            if (pickedText.getBkgColor().getAlpha() != 255) pickedText = new SpecialText(pickedText.getCharacter(), pickedText.getFgColor(), new Color(pickedText.getBkgColor().getRGB())); //Ensures there are no transparency issues
            JButton btn = textPanel.generateNewButton(pickedText);
            btn.doClick(); //Set the new text to be drawn with
        }
    }
}

package Game;

import Data.Coordinate;
import Data.LayerImportances;
import Engine.Layer;
import Engine.LayerManager;
import Engine.SpecialText;
import Game.Debug.DebugWindow;

import java.awt.*;

/**
 * Created by Jared on 5/9/2018.
 */
public class TextBox implements MouseInputReceiver{

    /**
     * TextBox:
     *
     * The text box shown at the bottom of the screen used to deliver messages to the player.
     *
     * TextBox contains a custom text parser that handles various flags that relate to the presentation of the message.
     * The Sourcery Text Level Editor Usage Guide contains additional information regarding which flags are handled and what their purposes are.
     */

    private Layer textBoxLayer;
    private Player player;
    private int width;

    private boolean isSkimming = false;

    private final int STATE_SCROLLING = 0;
    private final int STATE_PAGE_END  = 1;
    private final int STATE_END       = 2;
    private int currentState;

    //Colors!
    public static final Color bkg        = new Color(26, 26, 26);
    public static final Color txt_white  = new Color(225, 225, 225);
    public static final Color txt_red    = new Color(255, 128, 128);
    public static final Color txt_green  = new Color(130, 255, 130);
    public static final Color txt_blue   = new Color(140, 140, 255);
    public static final Color txt_cyan   = new Color(130, 255, 224);
    public static final Color txt_yellow = new Color(230, 230, 130);
    public static final Color txt_orange = new Color(255, 191, 128);
    public static final Color txt_silver = new Color(121, 121, 128);
    public static final Color txt_purple = new Color(191, 128, 255);

    public TextBox(LayerManager lm, Player player){

        int height = 5;
        textBoxLayer = new Layer(lm.getWindow().RESOLUTION_WIDTH, height, "text_box", 0, lm.getWindow().RESOLUTION_HEIGHT - height, LayerImportances.TEXT_BOX);
        textBoxLayer.fixedScreenPos = true;
        textBoxLayer.setVisible(false);
        lm.addLayer(textBoxLayer);

        this.player = player;

        width = lm.getWindow().RESOLUTION_WIDTH;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public void showMessage(String message){
        if (!textBoxLayer.getVisible()) {
            textBoxLayer.fillLayer(new SpecialText(' ', Color.WHITE, bkg));
            textBoxLayer.setVisible(true);
            player.freeze();
            DebugWindow.reportf(DebugWindow.MISC, "TextBox.showMessage","First word: \"%1$s\"", message.substring(0, message.indexOf(' ')));
            Thread writeThread = new Thread(() -> writeMessage(message));
            writeThread.start();
        }
    }

    private int row = 1;
    private int xpos = 1;

    private void writeMessage(String message){
        //Starting values
        row = 1;
        xpos = 1;
        int index = 0;
        isSkimming = false;
        int SCROLL_SPEED_NORMAL = 28;
        int SCROLL_SPEED_FAST = 8;
        int SCROLL_SPEED_SLOW = 250;
        int SCROLL_SPEED_SKIM = 3;
        //Begin writing
        int scrollSpeed = SCROLL_SPEED_NORMAL;
        currentState = STATE_SCROLLING;
        Color textColor = txt_white;
        while (index < message.length()){
            if (message.charAt(index) == ' ') { //End of a word
                int nextIndex = getIndexOfNextSpace(message, index + 1);
                DebugWindow.reportf(DebugWindow.MISC, "TextBox.showMessage","Next Word: \"%1$s\"", message.substring(index, nextIndex));
                if (xpos + nextIndex - index > width - 1) { //If word needs to wrap
                    shiftRow();
                } else
                    xpos++;
                sleep(13); //Somehow, this improves readability
            } else if (message.charAt(index) == '\n'){ //New line character
                shiftRow();
            } else if (isFormattedElement(message, index, "<nl>")){ //New line flag
                shiftRow();
                index += 3;
            } else if (isFormattedElement(message, index, "<np>")){ //New page flag
                row = 3;
                shiftRow();
                index += 3;
            } else if (isFormattedElement(message, index, "<sf>")){ //Fast speed flag
                if (!isSkimming){
                    scrollSpeed = SCROLL_SPEED_FAST;
                }
                index += 3;
            } else if (isFormattedElement(message, index, "<sn>")){ //Normal speed flag
                if (!isSkimming){
                    scrollSpeed = SCROLL_SPEED_NORMAL;
                }
                index += 3;
            } else if (isFormattedElement(message, index, "<ss>")) { //Slow speed flag
                if (!isSkimming) {
                    scrollSpeed = SCROLL_SPEED_SLOW;
                }
                index += 3;
            } else if (isFormattedElement(message, index, "<p1>")){ //1 Second Pause Flag
                sleep(1000);
                index += 3;
            } else if (isFormattedElement(message, index, "<p3>")){ //3 Second Pause Flag
                sleep(3000);
                index += 3;
            } else if (isFormattedElement(message, index, "<cw>")){ textColor = txt_white;  index += 3; //Color Flags!
            } else if (isFormattedElement(message, index, "<cr>")){ textColor = txt_red;    index += 3;
            } else if (isFormattedElement(message, index, "<cg>")){ textColor = txt_green;  index += 3;
            } else if (isFormattedElement(message, index, "<cb>")){ textColor = txt_blue;   index += 3;
            } else if (isFormattedElement(message, index, "<cc>")){ textColor = txt_cyan;   index += 3;
            } else if (isFormattedElement(message, index, "<cy>")){ textColor = txt_yellow; index += 3;
            } else if (isFormattedElement(message, index, "<co>")){ textColor = txt_orange; index += 3;
            } else if (isFormattedElement(message, index, "<cs>")){ textColor = txt_silver; index += 3;
            } else if (isFormattedElement(message, index, "<cp>")){ textColor = txt_purple; index += 3;
            } else { //It's not a special or formatted character?
                textBoxLayer.editLayer(xpos, row, new SpecialText(message.charAt(index), textColor, bkg));
                xpos++;
            }
            index++;
            if (isSkimming)
                sleep(SCROLL_SPEED_SKIM);
            else
                sleep(scrollSpeed);
        }
        currentState = STATE_END;
    }

    private boolean isFormattedElement(String message, int index, String element){
        return index <= message.length() - element.length() && message.substring(index, index + element.length()).equals(element);
    }

    private int getIndexOfNextSpace(String message, int index){
        int min = message.length() + 1;
        min = Math.min(getCleanedIndexOf(message, index, " "), min);
        String[] flags = {"<nl>","<np","<sf>","<sn>","<ss>","<cw>","<cr>","<cg>","<cb>","<cc>","<cp>","<cy>","<co>","<cs>","<p1>","<p3>"};
        for (String flag : flags) {
            min = Math.min(getCleanedIndexOf(message, index, flag), min);
        }
        return min;
    }

    private int getCleanedIndexOf(String message, int index, String toFind){
        int i = message.indexOf(toFind, index);
        if (i > -1) return i;
        return message.length();
    }

    private void shiftRow(){
        xpos = 1;
        row++;
        if (row > 3){
            currentState = STATE_PAGE_END;
            stopUntilState(STATE_SCROLLING);
            textBoxLayer.fillLayer(new SpecialText(' ', Color.WHITE, bkg));
            row = 1;
            isSkimming = false;
        }
    }

    private void sleep(long time){
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void stopUntilState(int expectedState){
        while (currentState != expectedState){
            sleep(30);
        }
    }

    @Override
    public boolean onMouseMove(Coordinate levelPos, Coordinate screenPos) {
        return false;
    }

    @Override
    public boolean onMouseClick(Coordinate levelPos, Coordinate screenPos, int mouseButton) {
        if (textBoxLayer.getVisible()){
            switch (currentState){
                case STATE_END:
                    textBoxLayer.setVisible(false);
                    player.unfreeze();
                    return true;
                case STATE_PAGE_END:
                    currentState = STATE_SCROLLING;
                    break;
                case STATE_SCROLLING:
                    isSkimming = true;
                    break;
            }
        }
        return false;
    }

    @Override
    public boolean onMouseWheel(Coordinate levelPos, Coordinate screenPos, double wheelMovement) {
        return false;
    }

    @Override
    public boolean onInputDown(Coordinate levelPos, Coordinate screenPos, int actionID) {
        return false;
    }

    @Override
    public boolean onInputUp(Coordinate levelPos, Coordinate screenPos, int actionID) {
        return false;
    }
}

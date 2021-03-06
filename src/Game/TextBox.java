package Game;

import Data.Coordinate;
import Data.LayerImportances;
import Engine.Layer;
import Engine.LayerManager;
import Engine.SpecialText;
import Game.Debug.DebugWindow;

import java.awt.*;
import java.util.ArrayList;

/**
 * Created by Jared on 5/9/2018.
 */
public class TextBox implements GameInputReciever {

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

    private PostMessageAction postMessageAction;

    //Colors!
    public static final Color bkg        = new Color(26, 26, 26);
    public static final Color banner     = new Color(51, 51, 51);
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
        showMessage(message, "", () -> {});
    }

    public void showMessage(String message, String speaker){
        showMessage(message, speaker, () -> {});
    }

    public void showMessage(String message, PostMessageAction action){
        showMessage(message, "", action);
    }

    public void showMessage(String message, String speaker, PostMessageAction action){
        if (!textBoxLayer.getVisible()) {
            textBoxLayer.fillLayer(new SpecialText(' ', Color.WHITE, bkg));
            textBoxLayer.setVisible(true);
            player.freeze();
            player.getInv().closePlayerInventory();
            player.getInv().closeOtherInventory();
            if (message.indexOf(' ') > 0)
                DebugWindow.reportf(DebugWindow.MISC, "TextBox.showMessage","First word: \"%1$s\"", message.substring(0, message.indexOf(' ')));
            postMessageAction = action;
            Thread writeThread = new Thread(() -> writeMessage(message, speaker), "TextBox Write");
            writeThread.start();
        }
    }

    private int row = 1;
    private int xpos = 1;

    private void writeMessage(String message, String speaker){
        //Starting values
        row = 1;
        xpos = 1;
        int index = 0;
        isSkimming = false;
        int SCROLL_SPEED_NORMAL = 28;
        int SCROLL_SPEED_FAST = 8;
        int SCROLL_SPEED_SLOW = 120;
        int SCROLL_SPEED_SKIM = 3;
        //Begin writing
        int scrollSpeed = SCROLL_SPEED_NORMAL;
        drawSpeakerBanner(speaker);
        currentState = STATE_SCROLLING;
        Color textColor = txt_white;
        while (index < message.length()){
            player.freeze();
            if (message.charAt(index) == ' ') { //End of a word
                if (xpos + getLengthOfNextWord(message, index + 1) > width - 1) { //If word needs to wrap
                    shiftRow(speaker);
                } else
                    xpos++;
                sleep(20); //Somehow, this improves readability
            } else if (message.charAt(index) == '\n'){ //New line character
                shiftRow(speaker);
            } else if (isFormattedElement(message, index, "<nl>")){ //New line flag
                shiftRow(speaker);
                index += 3;
            } else if (isFormattedElement(message, index, "<np>")){ //New page flag
                row = 3;
                shiftRow(speaker);
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
                if (!isSkimming)
                    sleep(1000);
                index += 3;
            } else if (isFormattedElement(message, index, "<p3>")){ //3 Second Pause Flag
                if (!isSkimming)
                    sleep(3000);
                index += 3;
            } else if (isFormattedElement(message, index, "<cw>")){ textColor = txt_white;  index += 3; //Color Flags
            } else if (isFormattedElement(message, index, "<cr>")){ textColor = txt_red;    index += 3;
            } else if (isFormattedElement(message, index, "<cg>")){ textColor = txt_green;  index += 3;
            } else if (isFormattedElement(message, index, "<cb>")){ textColor = txt_blue;   index += 3;
            } else if (isFormattedElement(message, index, "<cc>")){ textColor = txt_cyan;   index += 3;
            } else if (isFormattedElement(message, index, "<cy>")){ textColor = txt_yellow; index += 3;
            } else if (isFormattedElement(message, index, "<co>")){ textColor = txt_orange; index += 3;
            } else if (isFormattedElement(message, index, "<cs>")){ textColor = txt_silver; index += 3;
            } else if (isFormattedElement(message, index, "<cp>")){ textColor = txt_purple; index += 3;
            } else { //It's not a special or formatted character?
                if (message.charAt(index) == '\\') index++; //Escaped characters should display normally
                textBoxLayer.editLayer(xpos, row, new SpecialText(message.charAt(index), textColor, bkg));
                xpos++;
                if (message.charAt(index) == ',')
                    sleep(40);
                else if (message.charAt(index) == '.')
                    sleep(65);
            }
            index++;
            if (isSkimming)
                sleep(SCROLL_SPEED_SKIM);
            else
                sleep(scrollSpeed);
        }
        currentState = STATE_END;
        arrowBrightness = 0;
        drawNextPageArrow();
    }

    private void drawSpeakerBanner(String speakerName){
        if (speakerName.length() > 0){
            textBoxLayer.fillLayer(new SpecialText(' ', Color.WHITE, banner), new Coordinate(0, 0), new Coordinate(speakerName.length() + 2, 0));
            textBoxLayer.inscribeString(speakerName, 1, 0, new Color(220, 220, 250));
        }
    }

    private boolean isFormattedElement(String message, int index, String element){
        return index <= message.length() - element.length() && message.substring(index, index + element.length()).equals(element);
    }

    private int getLengthOfNextWord(String message, int startIndex){
        String word = message.substring(startIndex, getIndexOfNextSpace(message, startIndex));
        String[] flags = {"<nl>","<np","<sf>","<sn>","<ss>","<cw>","<cr>","<cg>","<cb>","<cc>","<cp>","<cy>","<co>","<cs>","<p1>","<p3>"};
        int length = word.length(); //Start by having the length equal the length of the word
        for (int index = 0; index < word.length(); index++){ //Then begin subtracting out the lengths of any flags within the word
            for (String flag : flags)
                if (isFormattedElement(word, index, flag)) {
                    index += flag.length() - 1; //index++ is ran at the end, bringing total increment of index to a proper amount.
                    length -= flag.length();
                }
        }
        //System.out.printf("[TextBox.getLengthOfNextWord] Word \'%1$s\' l=%2$d\n", word, length);
        return length;
    }

    private int getIndexOfNextSpace(String message, int index){
        int min = message.length() + 1;
        min = Math.min(getCleanedIndexOf(message, index, " "), min);
        //String[] flags = {"<nl>","<np","<sf>","<sn>","<ss>","<cw>","<cr>","<cg>","<cb>","<cc>","<cp>","<cy>","<co>","<cs>","<p1>","<p3>"};
        String[] flags = {"<nl>","<np"}; //Newline and newpage flags need to treated as spaces
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

    private void shiftRow(String speaker){
        xpos = 1;
        row++;
        if (row > 3){
            currentState = STATE_PAGE_END;
            arrowBrightness = 0;
            drawNextPageArrow();
            stopUntilState(STATE_SCROLLING);
            textBoxLayer.fillLayer(new SpecialText(' ', Color.WHITE, bkg));
            drawSpeakerBanner(speaker);
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
            if (expectedState == STATE_SCROLLING)
                drawNextPageArrow();
        }
    }

    private int arrowBrightness;

    private void drawNextPageArrow(){
        if (arrowBrightness < 4)
            arrowBrightness++;
        textBoxLayer.editLayer(textBoxLayer.getCols() - 2, textBoxLayer.getRows() - 1, new SpecialText('>', new Color(63 * arrowBrightness, 63 * arrowBrightness, 30 * arrowBrightness), bkg));
    }

    private boolean doClick(){
        if (textBoxLayer.getVisible()){
            switch (currentState){
                case STATE_END:
                    textBoxLayer.setVisible(false);
                    player.unfreeze();
                    postMessageAction.uponFinish();
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
    public boolean onMouseMove(Coordinate levelPos, Coordinate screenPos) {
        return false;
    }

    @Override
    public boolean onMouseClick(Coordinate levelPos, Coordinate screenPos, int mouseButton) {
        return false;
    }

    @Override
    public boolean onMouseWheel(Coordinate levelPos, Coordinate screenPos, double wheelMovement) {
        return false;
    }

    @Override
    public boolean onInputDown(Coordinate levelPos, Coordinate screenPos, ArrayList<Integer> actions) {
        if (actions.contains(InputMap.TEXTBOX_NEXT))
            return doClick();
        return textBoxLayer.getVisible();
    }

    @Override
    public boolean onInputUp(Coordinate levelPos, Coordinate screenPos, ArrayList<Integer> actions) {
        return textBoxLayer.getVisible();
    }

    @Override
    public boolean onNumberKey(Coordinate levelPos, Coordinate screenPos, int number) {
        return textBoxLayer.getVisible();
    }

    public interface PostMessageAction{
        void uponFinish();
    }
}

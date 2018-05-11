package Game;

import Data.Coordinate;
import Data.LayerImportances;
import Engine.Layer;
import Engine.LayerManager;
import Engine.SpecialText;

import java.awt.*;

/**
 * Created by Jared on 5/9/2018.
 */
public class TextBox implements MouseInputReceiver{

    String message;

    private Layer textBoxLayer;
    private Player player;
    private final int height = 5;
    private int width;

    private int scrollSpeed;
    private final int SCROLL_SPEED_SLOW   = 250;
    private final int SCROLL_SPEED_NORMAL = 28;
    private final int SCROLL_SPEED_FAST   = 8;
    private final int SCROLL_SPEED_SKIM   = 3;

    private final int STATE_SCROLLING = 0;
    private final int STATE_PAGE_END  = 1;
    private final int STATE_END       = 2;
    private int currentState;

    public TextBox(LayerManager lm, Player player){

        textBoxLayer = new Layer(lm.getWindow().RESOLUTION_WIDTH, height, "text_box", 0, lm.getWindow().RESOLUTION_HEIGHT - height, LayerImportances.TEXT_BOX);
        textBoxLayer.fixedScreenPos = true;
        lm.addLayer(textBoxLayer);

        this.player = player;

        width = lm.getWindow().RESOLUTION_WIDTH;
    }

    public void showMessage(String message){
        textBoxLayer.fillLayer(new SpecialText(' ', Color.WHITE, Color.BLACK));
        textBoxLayer.setVisible(true);
        player.freeze();
        DebugWindow.reportf(DebugWindow.MISC, "[TextBox.showMessage] First word: \"%1$s\"", message.substring(0, message.indexOf(' ')));
        Thread writeThread = new Thread(() -> writeMessage(message));
        writeThread.start();
    }

    private int row = 1;
    private int xpos = 1;

    private void writeMessage(String message){
        row = 1;
        xpos = 1;
        int index = 0;
        scrollSpeed = SCROLL_SPEED_NORMAL;
        currentState = STATE_SCROLLING;
        while (index < message.length()){
            if (message.charAt(index) == ' ') { //End of a word
                int nextIndex = message.indexOf(' ', index + 1);
                if (nextIndex == -1) nextIndex = message.length();
                DebugWindow.reportf(DebugWindow.MISC, "[TextBox.showMessage] Next Word: \"%1$s\"", message.substring(index, nextIndex));
                if (xpos + nextIndex - index > width - 1) { //If word needs to wrap
                    shiftRow();
                } else
                    xpos++;
                sleep(13); //Somehow, this improves readability
            } else if (message.charAt(index) == '\n'){ //New line character
                shiftRow();
            } else if (isFormattedElement(message, index, "<nl>")){ //New line formatted element
                shiftRow();
                index += 3;
            } else if (isFormattedElement(message, index, "<sf>")){ //Fast speed formatted element
                if (scrollSpeed != SCROLL_SPEED_SKIM){
                    scrollSpeed = SCROLL_SPEED_FAST;
                }
                index += 3;
            } else if (isFormattedElement(message, index, "<sn>")){ //Normal speed formatted element
                if (scrollSpeed != SCROLL_SPEED_SKIM){
                    scrollSpeed = SCROLL_SPEED_NORMAL;
                }
                index += 3;
            } else if (isFormattedElement(message, index, "<ss>")){ //Slow speed formatted element
                if (scrollSpeed != SCROLL_SPEED_SKIM){
                    scrollSpeed = SCROLL_SPEED_SLOW;
                }
                index += 3;
            } else {
                textBoxLayer.editLayer(xpos, row, new SpecialText(message.charAt(index), Color.WHITE, Color.BLACK));
                xpos++;
            }
            index++;
            sleep(scrollSpeed);
        }
        currentState = STATE_END;
    }

    private boolean isFormattedElement(String message, int index, String element){
        return index < message.length() - element.length() && message.substring(index, index + element.length()).equals(element);
    }

    private void shiftRow(){
        xpos = 1;
        row++;
        if (row > 3){
            currentState = STATE_PAGE_END;
            stopUntilState(STATE_SCROLLING);
            textBoxLayer.fillLayer(new SpecialText(' ', Color.WHITE, Color.BLACK));
            row = 1;
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
                    scrollSpeed = SCROLL_SPEED_SKIM;
                    break;
            }
        }
        return false;
    }

    @Override
    public boolean onMouseWheel(Coordinate levelPos, Coordinate screenPos, double wheelMovement) {
        return false;
    }
}

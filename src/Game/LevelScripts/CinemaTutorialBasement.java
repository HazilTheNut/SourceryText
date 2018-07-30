package Game.LevelScripts;

import Data.Coordinate;
import Data.LayerImportances;
import Engine.Layer;
import Engine.SpecialText;
import Game.InputMap;
import Game.InputType;

import java.awt.*;

public class CinemaTutorialBasement extends LevelScript {

    private Layer wakeUpAnim;
    private boolean doingWakeUpAnim;

    //Interact message
    private boolean didInteractMessage = false;

    //Inventory message
    private boolean didInventoryMessage = false;

    //Danger (no weapon) message
    private boolean didDangerMessage = false;

    //Combat message
    private boolean didCombatMessage = false;

    @Override
    public void onLevelLoad() {
        wakeUpAnim = new Layer(level.getBackdrop().getCols(), level.getBackdrop().getRows(), "", 0, 0, LayerImportances.MENU);
        wakeUpAnim.fillLayer(new SpecialText(' ', Color.WHITE, Color.BLACK));
        doingWakeUpAnim = true;
    }

    @Override
    public String[] getMaskNames() {
        return new String[]{"interact","inventory","danger","combat"};
    }

    @Override
    public void onLevelEnter() {
        if (doingWakeUpAnim){
            gi.getLayerManager().addLayer(wakeUpAnim);
            Thread animationThread = new Thread(() -> {
                gi.setPlayerTurn(false);
                sleep(1000);
                for (int i = 255; i > 0; i--) {
                    wakeUpAnim.fillLayer(new SpecialText(' ', Color.WHITE, new Color(0, 0, 0, i)));
                    sleep(10);
                }
                displayOpeningMessage();
                doingWakeUpAnim = false;
                gi.setPlayerTurn(true);
            });
            animationThread.start();
        }
    }

    @Override
    public void onTurnStart() {
        Coordinate playerLoc = gi.getPlayer().getLocation();
        if (getMaskDataAt("interact", playerLoc) && !didInteractMessage){
            gi.getTextBox().showMessage("There many things that can be interacted with.<nl>Press " + getInput(InputMap.MOVE_INTERACT) + " to interact with an object.<nl>It also moves the player to the selected tile.");
            didInteractMessage = true;
        }
        if (getMaskDataAt("inventory", playerLoc) && !didInventoryMessage){
            if (gi.getPlayer().getItems().size() > 0 && gi.getPlayer().getWeapon().getItemData().getItemId() < 0) {
                gi.getTextBox().showMessage("Items are very useful.<nl>Press " + getInput(InputMap.INVENTORY) + " to open your inventory.<np>Press " + getInput(InputMap.INV_USE) + " to use an item and<nl>press " + getInput(InputMap.INV_DROP) + " to drop an item.");
                didInventoryMessage = true;
            }
        }
        if (getMaskDataAt("danger", playerLoc) && !didDangerMessage && gi.getPlayer().getWeapon().getItemData().getItemId() < 0){
            gi.getTextBox().showMessage("Ahead is a bunch of man-eating rats. It is not advisable to tackle them bare-handed.");
            didDangerMessage = true;
        }
        if (getMaskDataAt("combat", playerLoc) && !didCombatMessage && gi.getPlayer().getWeapon().getItemData().getItemId() > 0){
            gi.getTextBox().showMessage("To attack enemies, simply press " + getInput(InputMap.ATTACK) + " in their general direction.<np>When fighting enemies, you and the enemies take turns attacking, so use your time wisely.<nl>Press " + getInput(InputMap.PASS_TURN) + " to pass your turn.");
            didCombatMessage = true;
        }
    }

    private void sleep(long millis){
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void displayOpeningMessage(){
        String btns = "";
        String btn = getInput(InputMap.MOVE_NORTH);
        if (btn != null) btns += btn;
        btn = getInput(InputMap.MOVE_SOUTH);
        if (btn != null) btns += "," + btn;
        btn = getInput(InputMap.MOVE_EAST);
        if (btn != null) btns += "," + btn;
        btn = getInput(InputMap.MOVE_WEST);
        if (btn != null) btns += "," + btn;
        gi.getTextBox().showMessage("<cs>Woah<ss>....<nl><sn><cw>The world is so colorful!<nl><p1>There's color everywhere!<np>(Press " + btns + " to move around.)");
    }

    private String getInput(int actionID){
        InputMap inputMap = gi.getGameMaster().getMouseInput().getInputMap();
        for (InputType inputType : inputMap.getPrimaryInputMap().keySet()){
            if (inputMap.getPrimaryInputMap().get(inputType).contains(actionID)){
                return (String.format("<cc>%1$s<cw>", inputType.toString()));
            }
        }
        return null;
    }
}

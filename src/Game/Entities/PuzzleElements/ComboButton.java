package Game.Entities.PuzzleElements;

import Data.Coordinate;
import Data.EntityArg;
import Data.EntityStruct;
import Engine.LayerManager;
import Engine.SpecialText;
import Game.Debug.DebugWindow;
import Game.Entities.Entity;
import Game.GameInstance;
import Game.Level;
import Game.Player;

import java.util.ArrayList;

public class ComboButton extends GenericPowerSource {

    private SpecialText offSprite;
    private SpecialText onSprite;

    private int puzzleID;            //Refers to a system of ComboButtons that all share the same puzzle ID
    private int puzzleProgress;      //The "progress" through the puzzle. Used by the ComboButtons to detect if the player is punching the code in the correct order. Puzzle progress and positions are zero-based
    private int solutionPos;         //The player is doing the code correctly if puzzleProgress == solutionPos
    private boolean isActive;        //ComboButtons are one-way switches unless the puzzle fails and the player interacts with the last element in the puzzle.
    private boolean isPuzzleFailed;  //A way to communicate to the other ComboButtons that the puzzle was failed.

    private ArrayList<ComboButton> puzzleSystem = null;

    @Override
    public void initialize(Coordinate pos, LayerManager lm, EntityStruct entityStruct, GameInstance gameInstance) {
        super.initialize(pos, lm, entityStruct, gameInstance);
        offSprite = readSpecTxtArg(searchForArg(entityStruct.getArgs(), "offSprite"), getSprite().getSpecialText(0, 0));
        onSprite = readSpecTxtArg(searchForArg(entityStruct.getArgs(), "onSprite"), getSprite().getSpecialText(0, 0));
        isActive = false;
        puzzleID = readIntArg(searchForArg(entityStruct.getArgs(), "puzzleID"), 0);
        solutionPos = readIntArg(searchForArg(entityStruct.getArgs(), "solutionPos"), 0);
        deactivate(); //Start in deactivated, "reset" state
    }

    @Override
    public ArrayList<EntityArg> generateArgs() {
        ArrayList<EntityArg> args = super.generateArgs();
        args.add(new EntityArg("onSprite", getIcon().toString()));
        args.add(new EntityArg("offSprite", getIcon().toString()));
        args.add(new EntityArg("puzzleID", "0"));
        args.add(new EntityArg("solutionPos", "0"));
        return args;
    }

    private ArrayList<ComboButton> getPuzzleSystem() {
        if (puzzleSystem == null) { //If puzzle system is null, find all combo buttons in system.
            puzzleSystem = new ArrayList<>();
            for (Entity e : getGameInstance().getCurrentLevel().getEntities()){
                if (e instanceof ComboButton) {
                    ComboButton comboButton = (ComboButton) e;
                    if (comboButton.getPuzzleID() == puzzleID) //This search should also include itself into the list, which makes life easier
                        puzzleSystem.add(comboButton);
                }
            }
        }
        return puzzleSystem;
    }

    private int getPuzzleID() {
        return puzzleID;
    }

    private void incrementProgress(){
        for (ComboButton button : getPuzzleSystem())
            button.puzzleProgress++;
    }

    private void reset(){
        for (ComboButton comboButton : getPuzzleSystem()){
            comboButton.deactivate();
        }
    }

    private void deactivate(){
        isActive = false;
        setIcon(offSprite);
        updateSprite();
        puzzleProgress = 0; //The switches only deactivate upon a failed puzzle.
        isPuzzleFailed = false;
    }

    public void setPuzzleFailed(boolean puzzleFailed) {
        isPuzzleFailed = puzzleFailed;
    }

    @Override
    public void onInteract(Player player) {
        if (!isActive){
            isActive = true;
            setIcon(onSprite);
            updateSprite();
            if (puzzleProgress != solutionPos) {
                for (ComboButton button : getPuzzleSystem()) button.setPuzzleFailed(true);
                DebugWindow.reportf(DebugWindow.MISC, "ComboButton.onInteract", "Puzzle Failed! (prog = %1$d ; myPos = %2$d)", puzzleProgress, solutionPos);
            }
            incrementProgress();
            if (puzzleProgress == getPuzzleSystem().size()){ //If this was the last element interacted with in the puzzle.
                if (isPuzzleFailed) //We should check to see if the puzzle was successfully completed.
                    reset();
                else
                    powerOn();
            }
        }
    }
}

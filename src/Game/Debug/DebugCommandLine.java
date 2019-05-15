package Game.Debug;

import Data.Coordinate;
import Data.FileIO;
import Data.ItemStruct;
import Game.*;
import Game.Entities.BasicEnemy;
import Game.Entities.Entity;
import Game.Entities.GameCharacter;
import Game.Registries.ItemRegistry;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.ArrayList;

public class DebugCommandLine extends JPanel {

    DebugLogPane outputPane;
    GameMaster gameMaster;

    public DebugCommandLine(){
        setLayout(new BorderLayout());

        outputPane = new DebugLogPane(false, false);

        JPanel bottomPanel = new JPanel(new BorderLayout());

        JTextField inputField = new JTextField();
        JButton sendButton = new JButton("Send");

        inputField.addActionListener(e -> sendButton.doClick());
        inputField.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        inputField.setBackground(Color.BLACK);
        inputField.setForeground(DebugWindow.textColor);

        sendButton.addActionListener(e -> {
            outputPane.addEntry("", processCommand(divideInput(inputField.getText())));
            outputPane.update();
            inputField.setText("");
        });

        bottomPanel.add(inputField, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.LINE_END);
        bottomPanel.setBorder(BorderFactory.createEtchedBorder());

        add(outputPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.PAGE_END);
    }

    public void setGameMaster(GameMaster gameMaster) {
        this.gameMaster = gameMaster;
    }

    private ArrayList<String> divideInput(String input){
        int index = 0;
        ArrayList<String> output = new ArrayList<>();
        while (index != -1){
            int nextIndex = input.indexOf(' ', index + 1);
            String toAdd;
            if (nextIndex != -1)
                toAdd = (input.substring(index, nextIndex).trim());
            else
                toAdd = (input.substring(index).trim());
            if (!toAdd.equals("")) output.add(toAdd);
            index = nextIndex;
        }
        return output;
    }

    private String processCommand(ArrayList<String> args){
        if (args.size() <= 0) return "";
        switch (args.get(0)){
            case "item":
                return processItemCmd(args);
            case "dmg":
                return processDmgCmd(args);
            case "tp":
                return processTpCommand(args);
            case "event":
                return processEventCommand(args);
            case "load":
                return processLoadCommand(args);
            case "unload":
                return processUnloadCommand(args);
            case "wipe":
                return processWipeCmd(args);
            default:
                return "ERROR: Command mnemonic invalid";
        }
        //return "ERROR: Command did not properly return feedback";
    }

    private String processItemCmd(ArrayList<String> args){
        if (args.size() < 3) return "ERROR: Insufficient args (format: \"item <id> <qty> [<+/- tagId>...]\")";
        if (gameMaster == null || gameMaster.getCurrentGameInstance() == null) return "ERROR: Game currently not running!";
        try {
            Item item = ItemRegistry.generateItem(Integer.valueOf(args.get(1)), gameMaster.getCurrentGameInstance());
            item.setQty(Integer.valueOf(args.get(2)));
            if (args.size() > 3) {
                ArrayList<String> adjustments = new ArrayList<>(args.subList(3, args.size()));
                item.interpretTagAdjustments(adjustments);
            }
            gameMaster.getCurrentGameInstance().getPlayer().addItem(item);
            return "Item \'" + item.getItemData().toString() + "\' added to player inventory";
        } catch (NumberFormatException e){
            e.printStackTrace();
            return "ERROR: Args improper!";
        }
    }

    private String processDmgCmd(ArrayList<String> args){
        if (args.size() < 3) return "ERROR: Insufficient args (format: \"dmg <uid> <amount>\")";
        if (gameMaster == null || gameMaster.getCurrentGameInstance() == null) return "ERROR: Game currently not running!";
        try {
            ArrayList<Entity> entities = gameMaster.getCurrentGameInstance().getCurrentLevel().getEntities();
            int uid = Integer.valueOf(args.get(1));
            for (Entity e : entities)
                if (e.getUniqueID() == uid) {
                    e.onReceiveDamage(Integer.valueOf(args.get(2)), e, gameMaster.getCurrentGameInstance());
                    return "Entity \'" + e.getName() + "\' has received damage.";
                }
            return "ERROR: Entity not found.";
        } catch (NumberFormatException e){
            e.printStackTrace();
            return "ERROR: Args improper!";
        }
    }

    private String processWipeCmd(ArrayList<String> args){
        if (gameMaster == null || gameMaster.getCurrentGameInstance() == null) return "ERROR: Game currently not running!";
        try {
            ArrayList<Entity> entities = gameMaster.getCurrentGameInstance().getCurrentLevel().getEntities();
            long playerUID = gameMaster.getCurrentGameInstance().getPlayer().getUniqueID();
            for (Entity e : entities)
                if (e instanceof BasicEnemy && e.getUniqueID() != playerUID) {
                    e.selfDestruct();
                }
            return "Enemies neutralized.";
        } catch (NumberFormatException e){
            e.printStackTrace();
            return "ERROR: Args improper!";
        }
    }

    private String processTpCommand(ArrayList<String> args){
        if (args.size() < 3) return "ERROR: Insufficient args (format: \"tp <x> <y>\")";
        if (gameMaster == null || gameMaster.getCurrentGameInstance() == null) return "ERROR: Game currently not running!";
        try {
            Coordinate newLoc = new Coordinate(Integer.valueOf(args.get(1)), Integer.valueOf(args.get(2)));
            Level currentLevel = gameMaster.getCurrentGameInstance().getCurrentLevel();
            newLoc = newLoc.floor(new Coordinate(0, 0)).ceil(new Coordinate(currentLevel.getWidth() - 1, currentLevel.getHeight() - 1));
            gameMaster.getCurrentGameInstance().getPlayer().setPos(newLoc);
            return "Player successfully teleported";
        } catch (NumberFormatException e){
            e.printStackTrace();
            return "ERROR: Args improper!";
        }
    }

    private String processEventCommand(ArrayList<String> args){
        if (args.size() < 2) return "ERROR: Insufficient args (format: \"event <\"event\">\")";
        if (gameMaster == null || gameMaster.getCurrentGameInstance() == null) return "ERROR: Game currently not running!";
        try {
            gameMaster.getCurrentGameInstance().recordEvent(args.get(1));
            return "Event \"" + args.get(1) + "\" recorded!";
        } catch (NumberFormatException e){
            e.printStackTrace();
            return "ERROR: Args improper!";
        }
    }

    private String processLoadCommand(ArrayList<String> args){
        if (args.size() < 2 || args.size() == 3) return "ERROR: Improper args (format: \"load <relative path> [<x> <y> <skip scorecard>]\")";
        if (gameMaster == null || gameMaster.getCurrentGameInstance() == null) return "ERROR: Game currently not running!";
        try {
            Coordinate newPos = gameMaster.getCurrentGameInstance().getPlayer().getLocation();
            if (args.size() > 3)
                newPos = new Coordinate(Integer.valueOf(args.get(2)), Integer.valueOf(args.get(3)));
            FileIO io = new FileIO();
            String path = io.getRootFilePath().concat(args.get(1));
            File levelFile = new File(path);
            if (levelFile.exists()) {
                Coordinate finalNewPos = newPos;
                Thread loadThread = new Thread(() -> {
                    if (args.size() > 4)
                        gameMaster.getCurrentGameInstance().enterLevel(path, finalNewPos, args.get(4).equals("true"));
                    else
                        gameMaster.getCurrentGameInstance().enterLevel(path, finalNewPos, false);
                });
                loadThread.start();
                return "Level Loaded!";
            } else
                return "ERROR: Level not found! path: " + levelFile.getPath();
        } catch (NumberFormatException e){
            e.printStackTrace();
            return "ERROR: Args improper!";
        }
    }

    private String processUnloadCommand(ArrayList<String> args){
        if (args.size() < 2 || args.get(1).length() <= 2) return "ERROR: Improper args (format: \"unload <level name (use \'_\' for spaces)>\")";
        if (gameMaster == null || gameMaster.getCurrentGameInstance() == null) return "ERROR: Game currently not running!";
        try {
            String levelName = args.get(1).replace('_', ' ');
            if (gameMaster.getCurrentGameInstance().getCurrentLevel().getName().equals(levelName))
                return "ERROR: That level is the current level!";
            for (Level level : gameMaster.getCurrentGameInstance().getCurrentZone().getActiveLevels()){
                String activeLevelName = level.getName();
                if (activeLevelName.equals(levelName)) {
                    gameMaster.getCurrentGameInstance().unloadLevel(level);
                    return "Level successfully unloaded!";
                }
            }
            return "ERROR: Level not found";
        } catch (NumberFormatException e){
            e.printStackTrace();
            return "ERROR: Args improper!";
        }
    }
}

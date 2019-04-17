package Game.Debug;

import Data.Coordinate;
import Data.FileIO;
import Data.ItemStruct;
import Game.Entities.Entity;
import Game.GameInstance;
import Game.GameMaster;
import Game.Item;
import Game.Level;
import Game.Registries.ItemRegistry;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
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
            if (nextIndex != -1)
                output.add(input.substring(index, nextIndex).trim());
            else
                output.add(input.substring(index).trim());
            index = nextIndex;
        }
        return output;
    }

    private String processCommand(ArrayList<String> args){
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
            for (int i = 3; i < args.size(); i++) {
                int tagId = Integer.valueOf(args.get(i));
                if (args.get(i).charAt(0) == '-')
                    item.removeTag(Math.abs(tagId));
                else
                    item.addTag(tagId, item);
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
        if (args.size() < 2 || args.size() == 3) return "ERROR: Improper args (format: \"load <relative path> [<x> <y>]\")";
        if (gameMaster == null || gameMaster.getCurrentGameInstance() == null) return "ERROR: Game currently not running!";
        try {
            Coordinate newPos = gameMaster.getCurrentGameInstance().getPlayer().getLocation();
            if (args.size() > 3)
                newPos = new Coordinate(Integer.valueOf(args.get(2)), Integer.valueOf(args.get(3)));
            FileIO io = new FileIO();
            gameMaster.getCurrentGameInstance().enterLevel(io.getRootFilePath().concat(args.get(1)), newPos);
            return "Level Loaded!";
        } catch (NumberFormatException e){
            e.printStackTrace();
            return "ERROR: Args improper!";
        }
    }
}

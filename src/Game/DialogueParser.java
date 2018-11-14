package Game;

import Data.ItemStruct;
import Game.Entities.GameCharacter;
import Game.Registries.ItemRegistry;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class DialogueParser implements Serializable {

    private String dialogueText; //The raw input string

    private int parserPos; //Where the parser is currently reading at
    private int moduleStartPos; //Where the parser should go if it reaches the end of the string

    private GameInstance gi;

    private HashMap<Integer, Integer> gotoMapping; //Maps an integer (marker #) to string position of marker.

    public DialogueParser(GameInstance gameInstance, String dialogueText){
        this.dialogueText = dialogueText;
        gi = gameInstance;
        //Build the mapping useful for goto statements later on.
        gotoMapping = new HashMap<>();
        for (parserPos = 0; parserPos < dialogueText.length(); parserPos++) {
            if (dialogueText.charAt(parserPos) == '#'){
                String containedText = getContainedString('#', true);
                try {
                    gotoMapping.put(Integer.valueOf(containedText), parserPos);
                } catch (NumberFormatException ignored){}
            }
        }

        parserPos = 0;
        moduleStartPos = 0;
    }

    /**
     * Puts runParser onto a new thread. There were some thread sleep issues with trades, and putting all of it into a new thread seemed to do the trick.
     * For some reason, calling waitForInput() caused the game's display to freeze, and thus fail to show the message, among other weirdnesses.
     */
    public void startParser(GameCharacter speaker){
        Thread parserThread = new Thread(() -> runParser(speaker));
        parserThread.start();
    }

    private void runParser(GameCharacter speaker){
        boolean stopping = false;
        System.out.println("[DialogueParser] PARSE BEGIN:");
        System.out.printf( "[DialogueParser] FULL STRING: \"%1$s\"\n\n", dialogueText);
        while (!stopping) {
            if (parserPos >= dialogueText.length()) {
                parserPos = moduleStartPos;
                return; //Reaching the end of the string should behave similar to a ';', but also returning to the previous stopping point.
            }
            char charAt = dialogueText.charAt(parserPos);
            if (charAt == ';' || charAt == ','){
                stopping = true;
                moduleStartPos = parserPos + 1;
            }
            if (charAt == '"'){
                String containedText = getContainedString('"', true);
                System.out.printf("[DialogueParser] TEXT:        \"%1$s\"\n", containedText);
                processText(containedText, speaker);
                stopping = true; //Stops parser, processText() calls this function upon completing the text box (as these two processes are on two separate threads)
            } else if (charAt == '['){
                String containedText = getContainedString(']', true);
                System.out.printf("[DialogueParser] CONDITIONAL: \"%1$s\"\n", containedText);
                processConditional(containedText, speaker);
            } else if (charAt == '{'){
                String containedText = getContainedString('}', true);
                System.out.printf("[DialogueParser] OPTIONS:     \"%1$s\"\n", containedText);
            } else if (charAt == '<'){
                String containedText = getContainedString('>', true);
                processGoto(containedText);
                System.out.printf("[DialogueParser] GOTO:        \"%1$s\"\n", containedText);
            }
            parserPos++;
        }
    }

    /**
     * Gets the string 'contained' between two characters.
     * This method assumes the parserPos is at the starting character, so the resulting string will start at parserPos + 1
     * Any \'s will escape characters within the desired contained string
     *
     * After finding the contained string, the parserPos will automatically be placed ahead of the contained string and the containing characters, if allowed to.
     *
     * @param end The character that defines where the contained string ends.
     * @param moveParserPos Whether or not the parserPos should be changed
     * @return The contained string
     */
    private String getContainedString(char end, boolean moveParserPos){
        for (int i = parserPos + 1; i < dialogueText.length(); i++) {
            char c = dialogueText.charAt(i);
            if (c == '\\') i++;
            else if (c == end){
                String contained = dialogueText.substring(parserPos + 1, i);
                if (moveParserPos) parserPos = i;
                return contained;
            }
        }
        if (moveParserPos) parserPos = dialogueText.length();
        return dialogueText.substring(Math.min(parserPos + 1, dialogueText.length()));
    }

    private void processText(String text, GameCharacter speaker){
        gi.getTextBox().showMessage(text, speaker.getName(), () -> startParser(speaker)); //parserPos already moves to the next index, so nothing needs to be done (see startParser while loop for details)
    }

    private int getIntFromStr(String str){
        try {
            return Integer.valueOf(str);
        } catch (NumberFormatException ignored){
            return Integer.MIN_VALUE;
        }
    }

    private void processGoto(String text){
        gotoMarker(getIntFromStr(text));
    }

    private void gotoMarker(int number){
        if (gotoMapping.containsKey(number))
            parserPos = gotoMapping.get(number);
    }

    private void processConditional(String text, GameCharacter speaker){
        if (text.indexOf(':') == -1 || text.indexOf('?') == -1) return; //Contained string is improperly formatted, gives up immediately.
        String condStatement = text.substring(0, text.indexOf('?')); //Get the full conditional statement
        //Begin cutting up the conditional statement into its pieces: the keyword, the arguments, and the goto
        int separatorLoc = text.indexOf('|');
        //keyword and arguments segments
        String keyword;
        String arguments = "";
        if (separatorLoc == -1)
            keyword = condStatement;
        else {
            keyword = condStatement.substring(0, separatorLoc);
            arguments = condStatement.substring(separatorLoc + 1);
        }
        System.out.printf("[DialogueParser.processConditional] -- \n keyword: \"%1$s\"\n arguments: \"%2$s\"\n ---\n", keyword, arguments);
        //Goto segment
        if (performConditional(keyword, arguments, speaker)){
            processGoto(text.substring(text.indexOf('?') + 1, text.indexOf(':'))); //Go to marker # between '?' and ':' (If true)
        } else {
            processGoto(text.substring(text.indexOf(':') + 1)); //Go to marker # between ':' and end of string (If false)
        }
    }

    private boolean performConditional(String keyword, String arguments, GameCharacter speaker){
        switch (keyword){
            case "ifdebug":
                int amount = getIntFromStr(arguments);
                return gi.getPlayer().getItems().size() > amount;
            case "ifm": //"If member"
                for (String factionName : speaker.getFactionAlignments())
                    if (gi.getPlayer().getFactionAlignments().contains(factionName))
                        return true;
                return false;
            case "ifop": //"If opinion"
                int minOpinion = getIntFromStr(arguments);
                int opinion =  gi.getFactionManager().getOpinion(speaker, gi.getPlayer());
                return opinion >= minOpinion;
            case "trade":
                int sepIndex = arguments.indexOf('|');
                if (sepIndex == -1) return processTrade(arguments, "", speaker);
                return processTrade(arguments.substring(0, sepIndex), arguments.substring(Math.min(sepIndex + 1, arguments.length())), speaker);
            default:
                return true;
        }
    }

    private boolean processTrade(String toGive, String toReceive, GameCharacter speaker){
        //Debug stuff
        System.out.printf("[DialogueParser.processTrade] TRADE:\n toGive: \"%1$s\"\n", toGive);
        for (String s : divideStringList(toGive)) System.out.printf("ITEM: %1$s\n", s);
        System.out.printf(" toReceive: \"%1$s\"\n", toReceive);
        for (String s : divideStringList(toReceive)) System.out.printf("ITEM: %1$s\n", s);
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //Begin actual trade code
        ArrayList<ItemStruct> itemsToGive = getItemsFromStringList(divideStringList(toGive)); //Get the list of items to give away
        ArrayList<ItemStruct> itemsToGet  = getItemsFromStringList(divideStringList(toReceive)); //Get the list of items to get back
        AtomicBoolean keepWaiting = new AtomicBoolean(true);
        AtomicBoolean tradeSuccessful = new AtomicBoolean(false);
        gi.getTextBox().showMessage(buildTradePrompt(itemsToGive), "", () -> keepWaiting.set(false)); //Builds and displays message based on item list
        waitForPlayerInput(keepWaiting); //Wait until the stopWaiting becomes true (when text box finishes)
        keepWaiting.set(true); //Reset keepWaiting boolean so that we can use it again
        //Ask the player if they want to conduct the trade (after text box finishes)
        gi.getQuickMenu().clearMenu();
        gi.getQuickMenu().addMenuItem("Yes", () -> { //This whole quick menu thing could be put into the lambda expression for the text box, but I'm afraid it might be a little buggy
            tradeSuccessful.set(trade(itemsToGive, itemsToGet, speaker)); // ^ "Nested lambda expressions" just doesn't sound like responsibly developed code
            keepWaiting.set(false);
        });
        gi.getQuickMenu().addMenuItem("No", () -> {
            tradeSuccessful.set(false);
            keepWaiting.set(false);
        });
        gi.getQuickMenu().showMenu("Complete Trade?", false);
        waitForPlayerInput(keepWaiting);
        return tradeSuccessful.get();
    }

    private void waitForPlayerInput(AtomicBoolean atomicBoolean){
        while (atomicBoolean.get()){
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private ArrayList<String> divideStringList(String str){
        int i = 0;
        ArrayList<String> output = new ArrayList<>();
        while (i < str.length()){
            int nextIndex = str.indexOf(',', i);
            if (nextIndex > -1){
                output.add(str.substring(i, nextIndex));
                i = nextIndex + 1; //Adding 1 prevents indexOf() from finding the previous ','
            } else { //Did not find another ','
                output.add(str.substring(i));
                i = str.length(); //Forces while loop to end
            }
        }
        return output;
    }

    private ArrayList<ItemStruct> getItemsFromStringList(ArrayList<String> items){
        ArrayList<ItemStruct> itemStructs = new ArrayList<>();
        for (String item : items){
            int index = item.indexOf('x'); //'x' is used to separate item id from quantity
            if (index > 0 && index != item.length() - 1){ //If the string is formatted correctly, indicative of the position of the 'x' character
                int id = getIntFromStr(item.substring(0, index));
                int q  = getIntFromStr(item.substring(index + 1));
                ItemStruct struct = ItemRegistry.getItemStruct(id);
                struct.setQty(q);
                itemStructs.add(struct);
            }
        }
        return itemStructs;
    }

    private String buildTradePrompt(ArrayList<ItemStruct> structs){
        StringBuilder builder = new StringBuilder("Give ");
        for (int i = 0; i < structs.size(); i++) {
            ItemStruct struct = structs.get(i);
            builder.append(String.format("<cy>%1$s x %2$d", struct.getName(), struct.getQty()));
            if (i < structs.size() - 1) builder.append("<cw>, ");
        }
        return builder.append("<cw>?").toString();
    }

    private boolean trade(ArrayList<ItemStruct> toGive, ArrayList<ItemStruct> toReceive, GameCharacter tradingPartner){
        //Set up ArrayLists of Items to give and receive
        ArrayList<Item> givingItems = new ArrayList<>();
        for (ItemStruct struct : toGive)
            givingItems.add(ItemRegistry.generateItem(struct, gi));
        ArrayList<Item> receivingItems = new ArrayList<>();
        for (ItemStruct struct : toReceive)
            receivingItems.add(ItemRegistry.generateItem(struct, gi));
        //Test to see if trade will succeed
        for (Item item : givingItems){
            if (!gi.getPlayer().hasItem(item)) return false; //Don't do the trade if the player doesn't have the necessary items
        }
        for (Item item : givingItems){
            tradingPartner.takeItem(item, gi.getPlayer());
        }
        for (Item item : receivingItems){ //Give items back as a reward
            gi.getPlayer().addItem(item);
        }
        return true;
    }
}

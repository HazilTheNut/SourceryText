package Game;

import java.util.HashMap;

public class DialogueParser {

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

    public void startParser(String defaultSpeakerName){
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
                processText(containedText, defaultSpeakerName);
                stopping = true;
            }
            if (charAt == '['){
                String containedText = getContainedString(']', true);
                System.out.printf("[DialogueParser] CONDITIONAL: \"%1$s\"\n", containedText);
                processConditional(containedText);
            }
            if (charAt == '{'){
                String containedText = getContainedString('}', true);
                System.out.printf("[DialogueParser] OPTIONS:     \"%1$s\"\n", containedText);
            }
            if (charAt == '<'){
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

    private void processText(String text, String speaker){
        int restartPos = parserPos + 1;
        gi.getTextBox().showMessage(text, speaker, () -> {
            parserPos = restartPos; //If the parser reaches the end of the string, the parserPos will move somewhere and cause looping behaviors. This is unwanted, so the parserPos is forcibly relocated to the end of the string being displayed.
            startParser(speaker);
        });
    }

    private int getIntFromStr(String str){
        try {
            return Integer.valueOf(str);
        } catch (NumberFormatException ignored){
            return -1;
        }
    }

    private void processGoto(String text){
        gotoMarker(getIntFromStr(text));
    }

    private void gotoMarker(int number){
        if (gotoMapping.containsKey(number))
            parserPos = gotoMapping.get(number);
    }

    private void processConditional(String text){
        if (text.indexOf(':') == -1 || text.indexOf('?') == -1) return; //Contained string is improperly formatted, gives up immediately.
        String condStatement = text.substring(0, text.indexOf(':')); //Get the full conditional statement
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
        if (performConditional(keyword, arguments)){
            processGoto(text.substring(text.indexOf(':') + 1, text.indexOf('?'))); //Go to marker # between ':' and '?' (If true)
        } else {
            processGoto(text.substring(text.indexOf('?') + 1)); //Go to marker # between '?' and end of string (If false)
        }
    }

    private boolean performConditional(String keyword, String arguments){
        switch (keyword){
            case "ifdebug":
                int amount = getIntFromStr(arguments);
                return gi.getPlayer().getItems().size() > amount;
            default:
                return true;
        }
    }
}

package Data;

import java.util.ArrayList;

public class StringIntParser {

    public int[] getInts(String text){
        ArrayList<Integer> values = new ArrayList<>();
        boolean buildingNumber = false;
        int numberStartIndex = 0;
        //Begin loop
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i); //Get character at index
            if (Character.isDigit(c)){
                if (!buildingNumber) //If this is not currently building a number, then this must be the start of a new number.
                    numberStartIndex = i; //Remember where the number started.
                buildingNumber = true;
            } else {
                if (buildingNumber){ //If the character is not a digit and this was building a number, then it must be the end of a number.
                    values.add(Integer.valueOf(text.substring(numberStartIndex, Math.min(i, text.length()))));
                }
                buildingNumber = false;
            }
        }
        if (buildingNumber){ //If building a number and it hit the end of the string, dump out the last number.
            values.add(Integer.valueOf(text.substring(numberStartIndex)));
        }
        int[] output = new int[values.size()];
        for (int i = 0; i < values.size(); i++) {
            output[i] = values.get(i);
        }
        return output;
    }
}

package Game;

import Data.SerializationVersion;
import Game.Debug.DebugWindow;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Zone implements Serializable {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    private ArrayList<Level> activeLevels;
    private String parentFolder;

    private String zoneName;

    private int magicPotatoCounter;
    private long turnCounter;

    public Zone(String levelFilePath){
        magicPotatoCounter = 0;
        activeLevels = new ArrayList<>();
        parentFolder = findParentFolder(levelFilePath);
        if (levelFilePath.length() > 2) {
            zoneName = getZoneNameFromInfoFile();
            if (zoneName == null)
                zoneName = getZoneNameFromFilePath(levelFilePath);
        } else {
            parentFolder = "EMPTY";
            zoneName = "ERROR";
        }
        buildZoneInfo();
        DebugWindow.reportf(DebugWindow.STAGE, "Zone", "Name: \"%1$s\" Path: %2$s", zoneName, parentFolder);
    }

    public void incrementMagicPotatoCounter(){
        magicPotatoCounter++;
    }

    public void incrementTurnCounter(){
        turnCounter++;
        DebugWindow.reportf(DebugWindow.STAGE, "Zone.incrementTurnCounter", "Turn Counter: %1$d", turnCounter);
    }

    public int getMagicPotatoCounter() {
        return magicPotatoCounter;
    }

    public long getTurnCounter() {
        return turnCounter;
    }

    public String getZoneName() {
        return zoneName;
    }

    private String findParentFolder(String levelFilePath){
        int index = levelFilePath.lastIndexOf('/');
        char appendingChar = '/';
        if (index < 0) { //Different OS's use either forward or backward slashes, so we have to cover both use cases.
            index = levelFilePath.lastIndexOf('\\');
            appendingChar = '\\';
        }
        if (index < 0) index = levelFilePath.length();
        return levelFilePath.substring(0, index).concat(new String(new char[]{appendingChar}));
    }

    /**
     * Gets the name of the containing folder of the level (.lda) file
     *
     * @param path The file path of the file
     * @return Name of containing folder
     */
    private String getZoneNameFromFilePath(String path){
        int strEndLoc   = path.lastIndexOf('/');
        int strStartLoc = path.substring(0, strEndLoc-1).lastIndexOf('/');
        return path.substring(strStartLoc+1, strEndLoc);
    }

    boolean isLevelWithinZone(String levelFilePath){
        String folderName = findParentFolder(levelFilePath);
        return folderName.equals(parentFolder);
    }

    void addLevel(Level level){
        activeLevels.add(level);
        DebugWindow.reportf(DebugWindow.STAGE, "Zone.addLevel", "Zone \"%1$s\"; Active Levels: %2$d", zoneName, activeLevels.size());
    }

    void removeLevel(Level level){
        activeLevels.remove(level);
    }

    public ArrayList<Level> getActiveLevels() {
        return activeLevels;
    }

    File getScorecardFile(){
        return new File(parentFolder.concat("scorecard.zinfo"));
    }

    private String getZoneNameFromInfoFile(){
        File infoTxt = getScorecardFile();
        if (infoTxt.exists()){
            try {
                FileReader fr = new FileReader(infoTxt);
                BufferedReader bufferedReader = new BufferedReader(fr);
                String firstLine = bufferedReader.readLine();
                String name = firstLine.substring(firstLine.indexOf('='));
                return name.substring(2, name.length()-1);
            } catch (IOException ignored) {}
        }
        return null;
    }

    private HashMap<String, String> zoneInfoMap;

    private void buildZoneInfo(){
        if (zoneInfoMap == null)
            zoneInfoMap = new HashMap<>();
        File infoTxt = getScorecardFile();
        if (infoTxt.exists()){
            try {
                FileReader fr = new FileReader(infoTxt);
                BufferedReader bufferedReader = new BufferedReader(fr);
                String nextLine;
                do { //Loop is ran at minimum of one time so that the buffered reader can read a line once
                    nextLine = bufferedReader.readLine();
                    if (nextLine != null) { //Input sanitation - empty info files should not cause errors
                        int dividerIndex = nextLine.indexOf('=');
                        if (dividerIndex != -1){ //If an equals sign exists, add data to mapping
                            zoneInfoMap.put(nextLine.substring(0, dividerIndex), nextLine.substring(dividerIndex+1));
                        }
                    }
                } while (nextLine != null);
            } catch (IOException ignored) {}
        }
    }

    public HashMap<String, String> getZoneInfoMap() {
        return zoneInfoMap;
    }
}

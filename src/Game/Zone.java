package Game;

import Data.Coordinate;
import Data.LayerImportances;
import Data.SerializationVersion;
import Engine.Layer;
import Engine.LayerManager;
import Game.Debug.DebugWindow;

import java.io.*;
import java.util.ArrayList;

public class Zone implements Serializable, MouseInputReceiver {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    private ArrayList<Level> activeLevels;
    private String parentFolder;

    private String zoneName;

    private int magicPotatoCounter;

    private Layer scorecardLayer;

    public Zone(String levelFilePath, LayerManager manager){
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
        if (manager != null)
            scorecardLayer = new Layer(manager.getWindow().RESOLUTION_WIDTH, manager.getWindow().RESOLUTION_HEIGHT, "scorecard:" + zoneName, 0, 0, LayerImportances.MAIN_MENU);
        else
            scorecardLayer = new Layer(0, 0, "scorecard:" + zoneName, 0, 0, LayerImportances.MAIN_MENU);
        scorecardLayer.setVisible(false);
        DebugWindow.reportf(DebugWindow.STAGE, "Zone", "Name: \"%1$s\" Path: %2$s", zoneName, parentFolder);
    }

    public void incrementMagicPotatoCounter(){
        magicPotatoCounter++;
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

    private String getZoneNameFromInfoFile(){
        File infoTxt = new File(parentFolder.concat("scorecard.zinfo"));
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

    @Override
    public boolean onMouseMove(Coordinate levelPos, Coordinate screenPos) {
        return scorecardLayer.getVisible();
    }

    @Override
    public boolean onMouseClick(Coordinate levelPos, Coordinate screenPos, int mouseButton) {
        return scorecardLayer.getVisible();
    }

    @Override
    public boolean onMouseWheel(Coordinate levelPos, Coordinate screenPos, double wheelMovement) {
        return scorecardLayer.getVisible();
    }

    @Override
    public boolean onInputDown(Coordinate levelPos, Coordinate screenPos, ArrayList<Integer> actions) {
        return scorecardLayer.getVisible();
    }

    @Override
    public boolean onInputUp(Coordinate levelPos, Coordinate screenPos, ArrayList<Integer> actions) {
        return scorecardLayer.getVisible();
    }

    @Override
    public boolean onNumberKey(Coordinate levelPos, Coordinate screenPos, int number) {
        return scorecardLayer.getVisible();
    }
}

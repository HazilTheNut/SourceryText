package Data;

import Editor.EditorToolPanel;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URLDecoder;

/**
 * Created by Jared on 3/16/2018.
 */
public class FileIO {

     /*
      FileIO:

      A catch-all utility class for saving and loading LevelData's (and hopefully game saves soon!)
     */

    /**
     * Gets the 'root file path' that points to the file folder where the SourceryText and LevelEditor .jar's are located.
     *
     * Useful for trying to reference files nearby the executable, like idk, a .lda or two....
     * Maybe a couple others too....
     *
     * Getting the root file path is handy because that means SourceryText requires no Installation Wizards to correctly configure.
     * This is also why you can't just run SourceryText out of the IDE, because the root path is now in src instead of CompiledGame
     *
     * You gotta configure for outputting a .jar and then run it from there.
     *
     * @return The root file path
     */
    public String getRootFilePath(){
        String path = decodeFilePath(getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
        System.out.println("[FileIO.getRootFilePath] base path: " + path);
        String reducedPath = path.substring(0, path.lastIndexOf('/'));
        reducedPath += "/";
        System.out.println("[FileIO.getRootFilePath] root path: " + reducedPath);
        return reducedPath;
    }

    /**
     * Takes a file path and 'cuts off' the portion that contains the root file path.
     *
     * If the file path doesn't contain the root path, it throws a hissy fit and returns "PATHS DO NOT MATCH"
     * So, that it might be easier to discover if the .jar is in the wrong spot.
     *
     * @param fullPath The 'full' file path
     * @return The file path relative to the root file path
     */
    public String getRelativeFilePath(String fullPath){
        String rootPath = getRootFilePath();
        if (fullPath.contains(rootPath)){
            return fullPath.substring(rootPath.length());
        }
        return "PATHS DO NOT MATCH";
    }

    /**
     * Cleans up file paths for easier interfacing with.
     *
     * 1) It parses the file path using URLDecoder
     * 2) It replaces every '\' with a '/', which is less of a pain to work with.
     * 3) It removes the '/' at the start of the file path.
     *
     * @param rawPath The 'raw' file path that needs cleaning
     * @return A much cleaner file path
     */
    public String decodeFilePath(String rawPath){
        String path;
        try {
            path = URLDecoder.decode(rawPath, "UTF-8");
            String output = path.replace('\\','/');
            if (output.startsWith("/")) output = output.substring(1);
            return output;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return rawPath;
        }
    }

    /**
     * Runs the FileChooser to pick a .lda file.
     *
     * This method lazily starts the FileChooser beginning location at the root file path
     * @return The selected File
     */
    public File chooseLevel(){
        String path = "";
        try {
            path = decodeFilePath(EditorToolPanel.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
            return chooseLevel(path);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return chooseLevel(path);
    }

    /**
     * Runs the FileChooser to pick a .lda file.
     *
     * @param startingPath The beginning location of FileChooser
     * @return The selected File
     */
    public File chooseLevel(String startingPath){
        System.out.printf("[FileIO.chooseLevel] Starting path: %1$s\n", startingPath);
        JFileChooser chooser = new JFileChooser(startingPath);
        System.out.println(startingPath);
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Sourcery Text Level Data", "lda");
        chooser.setFileFilter(filter);
        int returnVal = chooser.showOpenDialog(new Component() {
        });
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File chosenFile = chooser.getSelectedFile();
            System.out.println("[FileIO.chooseLevel] Opening file: " +
                    chosenFile.getName() + " at " + chosenFile.getPath());
            return chosenFile;
        } else return null;
    }

    /**
     * De-serializes a .lda file.
     *
     * @param savedLevel The LevelData file being opened.
     * @return The now-usable LevelData.
     */
    public LevelData openLevel(File savedLevel ){
        try {
            FileInputStream fileIn = new FileInputStream(savedLevel);
            ObjectInputStream objIn = new ObjectInputStream(fileIn);
            return (LevelData)objIn.readObject();
        } catch (IOException | ClassNotFoundException e) {
            JOptionPane.showMessageDialog(new JFrame(), "ERROR: File being accessed is out of date / improper!", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Serializes a LevelData (saving it) as a .lda file
     * @param ldata LevelData being saved
     * @param startingPath file path to start prompt from
     * @return chosen file path
     */
    private String serializeLevelData(LevelData ldata, String startingPath){
        String path;
        JFileChooser fileChooser = new JFileChooser(startingPath);
        int fileChooseOption = fileChooser.showSaveDialog(new Component(){});
        if (fileChooseOption == JFileChooser.APPROVE_OPTION){
            path = decodeFilePath(fileChooser.getSelectedFile().getPath());
            if (!path.endsWith(".lda")) { // Add .sav to file if user didn't.
                path += ".lda";
            }
            quickSerializeLevelData(ldata, path);
            return path;
        }
        return "";
    }

    /**
     * Performs a serialization of a LevelData without opening a FileChooser.
     *
     * NOTE: the input file path must be the non-relative type.
     *
     * @param ldata The LevelData being serialized
     * @param path The 'full' file path being saved to.
     */
    public void quickSerializeLevelData(LevelData ldata, String path){
        try {
            FileOutputStream out = new FileOutputStream(path);
            ObjectOutputStream objOut = new ObjectOutputStream(out);
            objOut.writeObject(ldata);
            System.out.println("[FileIO.serializeLevelData] Saved level to: " + path);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    public String serializeLevelData(LevelData ldata) {return serializeLevelData(ldata, getRootFilePath()); }
}

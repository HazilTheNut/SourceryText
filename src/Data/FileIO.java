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

    public String getRootFilePath(){
        String path = decodeFilePath(getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
        System.out.println("[FileIO.getRootFilePath] base path: " + path);
        String reducedPath = path.substring(0, path.lastIndexOf('/'));
        reducedPath += "/";
        System.out.println("[FileIO.getRootFilePath] root path: " + reducedPath);
        return reducedPath;
    }

    public String getRelativeFilePath(String fullPath){
        String rootPath = getRootFilePath();
        if (fullPath.contains(rootPath)){
            return fullPath.substring(rootPath.length());
        }
        return "PATHS DO NOT MATCH";
    }

    public String decodeFilePath(String rawPath){
        String path;
        try {
            path = URLDecoder.decode(rawPath, "UTF-8");
            return reformatFilePath(path);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return rawPath;
        }
    }

    private String reformatFilePath(String path){
        String output = path.replace('\\','/');
        if (output.startsWith("/")) output = output.substring(1);
        return output;
    }

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

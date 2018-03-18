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
            e.printStackTrace();
        }
        return null;
    }

    public void serializeLevelData(LevelData ldata){
        String path;
        JFileChooser fileChooser = new JFileChooser();
        int fileChooseOption = fileChooser.showSaveDialog(new Component(){});
        if (fileChooseOption == JFileChooser.APPROVE_OPTION){
            path = fileChooser.getSelectedFile().getPath();
            if (!path.endsWith(".lda")) { // Add .sav to file if user didn't.
                path += ".lda";
            }
            System.out.println("You chose to save the file to: " + path);
            try {
                FileOutputStream out = new FileOutputStream(path);
                ObjectOutputStream objOut = new ObjectOutputStream(out);
                objOut.writeObject(ldata);
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
        }
    }
}

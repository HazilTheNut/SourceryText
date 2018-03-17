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

    public File chooseLevel(){
        String path;
        String decodedPath = "";
        try {
            path = EditorToolPanel.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
            decodedPath = URLDecoder.decode(path, "UTF-8");
        } catch (UnsupportedEncodingException | URISyntaxException e) {
            e.printStackTrace();
        }
        return chooseLevel(decodedPath);
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
            System.out.println("You chose to open this file: " +
                    chooser.getSelectedFile().getName());
            return chooser.getSelectedFile();
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

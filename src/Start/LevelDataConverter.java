package Start;

import Data.FileIO;
import Data.LevelData;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class LevelDataConverter {

    private static LevelData ldata;
    private static String previousPath;

    public static void main (String[] args) {

        JFrame frame = new JFrame();
        frame.setLayout(new BorderLayout());

        frame.add(new JLabel("LevelData Compressor:"), BorderLayout.PAGE_START);

        JLabel ldataLabel = new JLabel("Path: ");
        JButton saveLevelBtn = new JButton("Save As Compressed");

        JButton openLevelBtn = new JButton("Open Uncompressed");
        openLevelBtn.addActionListener(e -> {
            FileIO io = new FileIO();
            File picked = io.chooseLevelData(previousPath);
            ldata = io.openLevelNonGZIP(picked);
            previousPath = picked.getPath();
            ldataLabel.setText(String.format("Path: %s", picked.getPath()));
            ldataLabel.repaint();
            saveLevelBtn.setEnabled(true);
        });
        frame.add(openLevelBtn, BorderLayout.LINE_START);

        saveLevelBtn.addActionListener(e -> {
            FileIO io = new FileIO();
            if (ldata != null) {
                io.serializeLevelData(ldata, previousPath);
            } else {
                System.out.println("ERROR: LevelData is null!");
            }
        });
        saveLevelBtn.setEnabled(false);

        frame.add(saveLevelBtn, BorderLayout.LINE_END);
        frame.add(ldataLabel, BorderLayout.PAGE_END);

        frame.pack();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
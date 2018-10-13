package Editor;

import Data.Coordinate;
import Data.LevelData;

import javax.swing.*;
import java.awt.*;

public class TextInsctiptionWindow extends JFrame {

    public TextInsctiptionWindow(LevelData ldata, UndoManager undoManager){

        Container c = getContentPane();

        JTextArea inputTextField = new JTextArea("Input text here");

        JScrollPane scrollPane = new JScrollPane(inputTextField, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.LINE_AXIS));

        JTextField xCoordinateField = new JTextField();
        xCoordinateField.setColumns(3);
        xCoordinateField.setMaximumSize(new Dimension(50, 30));

        JTextField yCoordinateField = new JTextField();
        yCoordinateField.setColumns(3);
        yCoordinateField.setMaximumSize(new Dimension(50, 30));

        JCheckBox shouldWrapBox = new JCheckBox();
        shouldWrapBox.setSelected(true);

        JButton inscribeButton = new JButton("Inscribe");
        inscribeButton.addActionListener(e -> {
            if (performInscription(ldata, undoManager, inputTextField.getText(), xCoordinateField, yCoordinateField, shouldWrapBox.isSelected()))
                dispose();
        });

        bottomPanel.add(new JLabel("x:"));
        bottomPanel.add(xCoordinateField);
        bottomPanel.add(new JLabel("y:"));
        bottomPanel.add(yCoordinateField);
        bottomPanel.add(Box.createHorizontalGlue());
        bottomPanel.add(new JLabel("Wrap?"));
        bottomPanel.add(shouldWrapBox);
        bottomPanel.add(inscribeButton);

        c.add(scrollPane, BorderLayout.CENTER);
        c.add(bottomPanel, BorderLayout.PAGE_END);

        c.validate();

        setTitle("Text Inscription");
        setSize(400, 200);

        setVisible(true);
    }

    private boolean performInscription(LevelData ldata, UndoManager undoManager, String text, JTextField xBox, JTextField yBox, boolean wrapping){ //Insert gaming joke here
        Coordinate startLoc;
        try {
            int x = Integer.valueOf(xBox.getText());
            int y = Integer.valueOf(yBox.getText());
            startLoc = new Coordinate(x, y);
        } catch (NumberFormatException e) {
            return false;
        }
        ldata.getBackdrop().inscribeString(text, startLoc.getX(), startLoc.getY(), Color.WHITE, wrapping);
        undoManager.recordLevelData();

        return true;
    }

}

package Editor.DialgoueCreator;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

public class DialogueSpecialActionCreator extends JPanel {

    public DialogueSpecialActionCreator(){

        Font font = new Font(Font.MONOSPACED, Font.PLAIN, 12);

        JComboBox<ActionType> selectionBox = new JComboBox<>();
        selectionBox.setFont(font);
        selectionBox.addItem(new ActionType("end", "End parser", false));
        selectionBox.addItem(new ActionType("trigger","LevelScript trigger", true));
        selectionBox.addItem(new ActionType("speakername","Change speaker name", true));

        JTextField argumentField = new JTextField(15);
        argumentField.setFont(font);

        JLabel outputLabel = new JLabel("=");
        outputLabel.setFont(font);

        JTextArea outputField = new JTextArea();
        outputField.setFont(font);
        outputField.setPreferredSize(new Dimension(150, 20));
        outputField.setEditable(false);

        selectionBox.addItemListener(e -> {
            outputField.setText(generateFormattedText(selectionBox, argumentField));
            argumentField.setEnabled(getSelectedConditional(selectionBox).requiresArgument);
        });
        argumentField.addCaretListener(e -> outputField.setText(generateFormattedText(selectionBox, argumentField)));

        JButton copyButton = new JButton("Copy");
        copyButton.addActionListener(e -> {
            StringSelection generated = new StringSelection(generateFormattedText(selectionBox, argumentField));
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(generated, generated);
        });

        //Format and add elements

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.LINE_AXIS));

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.LINE_AXIS));

        topPanel.add(selectionBox);
        topPanel.add(argumentField);

        bottomPanel.add(outputField);
        bottomPanel.add(copyButton);

        add(topPanel, BorderLayout.PAGE_START);
        add(bottomPanel, BorderLayout.PAGE_END);

        setPreferredSize(new Dimension(450, 110));
    }

    private ActionType getSelectedConditional(JComboBox<ActionType> conditionalJComboBox){
        ActionType cond = (ActionType)conditionalJComboBox.getSelectedItem();
        if (cond == null)
            return new ActionType("ERROR","ERROR",false);
        return cond;
    }

    private String generateFormattedText(JComboBox<ActionType> conditionalJComboBox, JTextField argumentField){
        ActionType actionType = getSelectedConditional(conditionalJComboBox);
        if (actionType == null) return "ERROR";
        return String.format("!%1$s|%2$s!", actionType.keyword, argumentField.getText());
    }

    private class ActionType {
        String keyword;
        String description;
        boolean requiresArgument;
        private ActionType(String keyword, String description, boolean requiresArgument){
            this.keyword = keyword;
            this.description = description;
            this.requiresArgument = requiresArgument;
        }

        @Override
        public String toString() {
            return description;
        }
    }

}

package Editor.DialgoueCreator;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

public class DialogueConditionalCreator extends JPanel {

    public DialogueConditionalCreator(){

        Font font = new Font(Font.MONOSPACED, Font.PLAIN, 12);

        JComboBox<Conditional> selectionBox = new JComboBox<>();
        selectionBox.setFont(font);
        selectionBox.addItem(new Conditional("ifm", "If member of my faction", false));
        selectionBox.addItem(new Conditional("ifop","If my opinion is at least", true));
        selectionBox.addItem(new Conditional("ifevent","If event happened", true));
        selectionBox.addItem(new Conditional("ifitem","If player has items", true));

        JTextField argumentField = new JTextField(15);
        argumentField.setFont(font);

        JLabel outputLabel = new JLabel("=");
        outputLabel.setFont(font);

        JTextArea outputField = new JTextArea();
        outputField.setFont(font);
        outputField.setPreferredSize(new Dimension(150, 20));
        outputField.setEditable(false);

        JSpinner ifTrueField  = new JSpinner(new SpinnerNumberModel(0, 0, 999, 1));
        JSpinner ifFalseField = new JSpinner(new SpinnerNumberModel(0, 0, 999, 1));

        selectionBox.addItemListener(e -> {
            outputField.setText(generateConditoinal(selectionBox, argumentField, ifTrueField, ifFalseField));
            argumentField.setEnabled(getSelectedConditional(selectionBox).requiresArgument);
        });
        argumentField.addCaretListener(e -> outputField.setText(generateConditoinal(selectionBox, argumentField, ifTrueField, ifFalseField)));
        ifTrueField.getModel().addChangeListener(e -> outputField.setText(generateConditoinal(selectionBox, argumentField, ifTrueField, ifFalseField)));
        ifFalseField.getModel().addChangeListener(e -> outputField.setText(generateConditoinal(selectionBox, argumentField, ifTrueField, ifFalseField)));

        JButton copyButton = new JButton("Copy");
        copyButton.addActionListener(e -> {
            StringSelection generated = new StringSelection(generateConditoinal(selectionBox, argumentField, ifTrueField, ifFalseField));
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
        topPanel.add(copyButton);

        bottomPanel.add(new JLabel(" GOTO: If True "));
        bottomPanel.add(ifTrueField);
        bottomPanel.add(new JLabel(" If False "));
        bottomPanel.add(ifFalseField);
        bottomPanel.add(outputField);
        bottomPanel.add(Box.createHorizontalGlue());

        add(topPanel, BorderLayout.PAGE_START);
        add(bottomPanel, BorderLayout.PAGE_END);

        setPreferredSize(new Dimension(450, 110));
    }

    private Conditional getSelectedConditional(JComboBox<Conditional> conditionalJComboBox){
        Conditional cond = (Conditional)conditionalJComboBox.getSelectedItem();
        if (cond == null)
            return new Conditional("ERROR","ERROR",false);
        return cond;
    }

    private String generateConditoinal(JComboBox<Conditional> conditionalJComboBox, JTextField argumentField, JSpinner ifTrue, JSpinner ifFalse){
        Conditional conditional = getSelectedConditional(conditionalJComboBox);
        if (conditional == null) return "ERROR";
        int gotoTrue = (Integer)ifTrue.getModel().getValue();
        int gotoFalse = (Integer)ifFalse.getModel().getValue();
        return String.format("[%1$s|%2$s?%3$d:%4$d]", conditional.keyword, argumentField.getText(), gotoTrue, gotoFalse);
    }

    private class Conditional{
        String keyword;
        String description;
        boolean requiresArgument;
        private Conditional(String keyword, String description, boolean requiresArgument){
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

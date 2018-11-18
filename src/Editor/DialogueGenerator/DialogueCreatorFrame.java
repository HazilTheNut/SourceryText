package Editor.DialogueGenerator;

import javax.swing.*;
import java.awt.*;

public class DialogueCreatorFrame extends JFrame {

    public DialogueCreatorFrame(){

        setTitle("DialogueGenerator");

        Container c = getContentPane();
        c.setLayout(new BoxLayout(c, BoxLayout.PAGE_AXIS));

        JTabbedPane utilitiesPanel = new JTabbedPane();
        utilitiesPanel.setMaximumSize(new Dimension(400, 200));
        utilitiesPanel.setAlignmentX(LEFT_ALIGNMENT);
        utilitiesPanel.setBorder(BorderFactory.createTitledBorder("Utilities"));

        c.add(utilitiesPanel);

        JTextArea dialoguePanel = new JTextArea("#0#");

        JScrollPane scrollPane = new JScrollPane(dialoguePanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Content"));
        scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);

        c.add(scrollPane);
        c.add(createToolsPanel(dialoguePanel));

        setSize(500, 500);
        setMinimumSize(new Dimension(400, 400));

        c.validate();
        validate();

        setVisible(true);

    }

    private JPanel createToolsPanel(JTextArea editorPanel){
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(450, 60));
        panel.setBorder(BorderFactory.createTitledBorder("Import / Export"));

        JTextField generateField = new JTextField("");
        generateField.setEditable(true);
        generateField.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        generateField.setPreferredSize(new Dimension(330, 25));

        panel.add(createImportButton(editorPanel, generateField), BorderLayout.LINE_START);
        panel.add(generateField, BorderLayout.CENTER);
        panel.add(createExportButton(editorPanel, generateField), BorderLayout.LINE_END);
        panel.validate();

        return panel;
    }

    private JButton createImportButton(JTextArea editorPanel, JTextField generateFrom){
        JButton btn = new JButton("Imp.");
        btn.setMargin(new Insets(2, 5, 2, 5));
        btn.addActionListener(e -> {
            if (generateFrom.getText().length() > 0){
                StringBuilder builder = new StringBuilder();
                boolean ignoreHashtag = false;
                for (int i = 0; i < generateFrom.getText().length(); i++) {
                    char c = generateFrom.getText().charAt(i);
                    if (c == '#'){
                        if (ignoreHashtag)
                            ignoreHashtag = false;
                        else {
                            if (i > 0) builder.append('\n'); //Newline characters should not be inserted at the start of the string
                            ignoreHashtag = true;
                        }
                    }
                    builder.append(c);
                }
                editorPanel.setText(builder.toString());
            }
            generateFrom.setText("");
        });
        return btn;
    }

    private JButton createExportButton(JTextArea editorPanel, JTextField generateField){
        JButton btn = new JButton("Exp.");
        btn.setMargin(new Insets(2, 5, 2, 5));
        btn.addActionListener(e -> {
            if (editorPanel.getText().length() > 0){
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < editorPanel.getText().length(); i++) {
                    char c = editorPanel.getText().charAt(i);
                    if (c != '\n')
                        builder.append(c);
                }
                generateField.setText(builder.toString());
            }
        });
        return btn;
    }

}

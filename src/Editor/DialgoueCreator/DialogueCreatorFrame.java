package Editor.DialgoueCreator;

import Data.ItemStruct;
import Game.Registries.ItemRegistry;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

public class DialogueCreatorFrame extends JFrame {

    public DialogueCreatorFrame(){

        setTitle("DialogueGenerator");

        Container c = getContentPane();
        c.setLayout(new BoxLayout(c, BoxLayout.PAGE_AXIS));

        JTextArea dialoguePanel = new JTextArea("#0#");

        JScrollPane scrollPane = new JScrollPane(dialoguePanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Editor"));
        scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);

        c.add(createUtilitiesPanel(dialoguePanel));
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

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.LINE_AXIS));

        JButton copyButton = new JButton("Copy");
        copyButton.addActionListener(e -> {
            StringSelection selection = new StringSelection(generateField.getText());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
        });

        panel.add(createImportButton(editorPanel, generateField), BorderLayout.LINE_START);
        panel.add(generateField, BorderLayout.CENTER);
        rightPanel.add(createExportButton(editorPanel, generateField));
        rightPanel.add(copyButton);
        panel.add(rightPanel, BorderLayout.LINE_END);
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

    private JTabbedPane createUtilitiesPanel(JTextArea editorPanel){
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setMaximumSize(new Dimension(450, 200));
        tabbedPane.setPreferredSize(new Dimension(450, 110));
        tabbedPane.setMinimumSize(new Dimension(450, 50));
        tabbedPane.setAlignmentX(LEFT_ALIGNMENT);
        tabbedPane.setBorder(BorderFactory.createTitledBorder("Utilities"));

        tabbedPane.addTab("Items", new DialogueItemCreator());
        tabbedPane.addTab("Conditionals", new DialogueConditionalCreator());
        tabbedPane.addTab("Options", new DialogueOptionsCreator());
        tabbedPane.addTab("Trades", new DialogueTradeCreator());
        tabbedPane.addTab("Special Actions", new DialogueSpecialActionCreator());

        tabbedPane.addChangeListener(e -> tabbedPane.setPreferredSize(tabbedPane.getSelectedComponent().getPreferredSize()));
        tabbedPane.setPreferredSize(tabbedPane.getComponentAt(0).getPreferredSize());

        tabbedPane.validate();

        return tabbedPane;
    }





}

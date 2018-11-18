package Editor;

import Data.ItemStruct;
import Game.Registries.ItemRegistry;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

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

    private JTabbedPane createUtilitiesPanel(JTextArea editorPanel){
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setMaximumSize(new Dimension(450, 200));
        tabbedPane.setAlignmentX(LEFT_ALIGNMENT);
        tabbedPane.setBorder(BorderFactory.createTitledBorder("Utilities"));

        tabbedPane.addTab("Items", createItemUtilityPanel());

        return tabbedPane;
    }

    private JPanel createItemUtilityPanel(){
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));

        JLabel infoLabel = new JLabel("?");
        infoLabel.setToolTipText("NOTE: Item quantities are in \'literal quantities.\' \"3 x Weapon\" means \"3 Weapons of any durability\" and not \"1 Weapon of durability 3\".");
        infoLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, 12));
        infoLabel.setForeground(Color.BLUE);

        Font font = new Font(Font.MONOSPACED, Font.PLAIN, 12);

        JSpinner qtyField = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));
        qtyField.setFont(font);

        JComboBox<ItemStruct> itemSelect = new JComboBox<>();
        for (int key : ItemRegistry.getMapKeys()) itemSelect.addItem(ItemRegistry.getItemStruct(key));
        itemSelect.setMaximumRowCount(20);
        itemSelect.setFont(font);

        JTextField outputField = new JTextField(10);
        outputField.setFont(font);

        qtyField.getModel().addChangeListener(e -> outputField.setText(generateItemIxQ(qtyField, itemSelect)));
        itemSelect.addItemListener(e -> outputField.setText(generateItemIxQ(qtyField, itemSelect)));

        panel.add(Box.createRigidArea(new Dimension(5, 0)));
        panel.add(infoLabel);
        panel.add(Box.createRigidArea(new Dimension(5, 0)));
        panel.add(qtyField);
        panel.add(new JLabel(" X "));
        panel.add(itemSelect);
        panel.add(Box.createRigidArea(new Dimension(5, 0)));
        panel.add(outputField);

        return panel;
    }

    private String generateItemIxQ(JSpinner qtySpinner, JComboBox<ItemStruct> itemSelect){
        ItemStruct selectedStruct = ((ItemStruct)itemSelect.getSelectedItem());
        if (selectedStruct == null) return "";
        int id = selectedStruct.getItemId();
        int qty = (Integer)qtySpinner.getModel().getValue();
        return String.format("%1$dx%2$d", id, qty);
    }
}

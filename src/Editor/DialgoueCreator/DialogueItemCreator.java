package Editor.DialgoueCreator;

import Data.ItemStruct;
import Game.Registries.ItemRegistry;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

public class DialogueItemCreator extends JPanel {

    public DialogueItemCreator(){
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

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

        JButton copyButton = new JButton("Copy");
        copyButton.addActionListener(e -> {
            StringSelection generated = new StringSelection(generateItemIxQ(qtyField, itemSelect));
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(generated, generated);
        });

        add(Box.createRigidArea(new Dimension(5, 0)));
        add(infoLabel);
        add(Box.createRigidArea(new Dimension(5, 0)));
        add(qtyField);
        add(new JLabel(" X "));
        add(itemSelect);
        add(Box.createRigidArea(new Dimension(5, 0)));
        add(outputField);
        add(copyButton);

        setPreferredSize(new Dimension(450, 75));
    }

    private String generateItemIxQ(JSpinner qtySpinner, JComboBox<ItemStruct> itemSelect){
        ItemStruct selectedStruct = ((ItemStruct)itemSelect.getSelectedItem());
        if (selectedStruct == null) return "";
        int id = selectedStruct.getItemId();
        int qty = (Integer)qtySpinner.getModel().getValue();
        return String.format("%1$dx%2$d", id, qty);
    }
}

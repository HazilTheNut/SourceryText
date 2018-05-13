package Editor;

import Data.EntityStruct;
import Data.ItemStruct;
import Game.Registries.ItemRegistry;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by Jared on 4/15/2018.
 */
public class EntityInventoryPanel extends JPanel {

    private DefaultListModel<ItemStruct> entityInvModel;
    private EntityStruct entity;

    public EntityInventoryPanel(JFrame frame, EntityStruct struct){
        entity = struct;

        setLayout(new BorderLayout());

        JPanel selectionPanel = new JPanel();
        selectionPanel.setLayout(new BorderLayout());

        Font textFont = new Font(Font.MONOSPACED, Font.PLAIN, 12);

        JComboBox<ItemStruct> newItemSelect = new JComboBox<>();

        int[] mapKeys = ItemRegistry.getMapKeys();
        for (int key : mapKeys)
            newItemSelect.addItem(ItemRegistry.getItemStruct(key));

        newItemSelect.setFont(textFont);
        newItemSelect.setMaximumRowCount(20);
        selectionPanel.add(newItemSelect, BorderLayout.PAGE_START);

        entityInvModel = new DefaultListModel<>();
        updateEntityInventory();
        JList<ItemStruct> invSelectionList = new JList<>();
        invSelectionList.setModel(entityInvModel);
        invSelectionList.setLayoutOrientation(JList.VERTICAL);
        invSelectionList.setFont(textFont);

        JScrollPane invScrollPane = new JScrollPane(invSelectionList, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        selectionPanel.add(invScrollPane);

        selectionPanel.setBorder(BorderFactory.createEtchedBorder());

        add(selectionPanel);

        JPanel editPanel = new JPanel();

        JTextField itemQtySetter = new JTextField(2);
        itemQtySetter.setFont(textFont);
        itemQtySetter.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                onTextBoxUpdate(itemQtySetter, invSelectionList);
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                onTextBoxUpdate(itemQtySetter, invSelectionList);
            }
            @Override
            public void changedUpdate(DocumentEvent e) {}
        });

        invSelectionList.addListSelectionListener(e -> {
            ItemStruct item = invSelectionList.getSelectedValue();
            if (item != null)
                itemQtySetter.setText(String.valueOf(item.getQty()));
        });

        JButton addItemButton = new JButton("+");
        addItemButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        addItemButton.addActionListener(e -> {
            ItemStruct selectedItem = (ItemStruct)newItemSelect.getSelectedItem();
            if (selectedItem != null){
                entity.addItem(selectedItem.copy());
                updateEntityInventory();
            }
        });

        JButton removeItemButton = new JButton("-");
        removeItemButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        removeItemButton.addActionListener(e -> {
            ItemStruct selectedItem = invSelectionList.getSelectedValue();
            if (selectedItem != null){
                entity.removeItem(selectedItem);
                updateEntityInventory();
            }
        });

        editPanel.add(addItemButton);
        editPanel.add(removeItemButton);
        editPanel.add(itemQtySetter);

        int count = editPanel.getComponentCount();
        editPanel.setLayout(new GridLayout(count, 1, 3, 2));
        editPanel.setMaximumSize(new Dimension(50, count * 28));

        JPanel encapsulatingPanel = new JPanel();
        encapsulatingPanel.setLayout(new BoxLayout(encapsulatingPanel, BoxLayout.PAGE_AXIS));
        encapsulatingPanel.add(editPanel);

        encapsulatingPanel.add(Box.createVerticalGlue());

        JButton finishButton = new JButton("Done");
        finishButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        finishButton.setMargin(new Insets(2, 4, 2, 4));
        finishButton.addActionListener(e -> frame.dispose());
        encapsulatingPanel.add(finishButton);

        add(encapsulatingPanel, BorderLayout.LINE_END);
    }

    private void updateEntityInventory(){
        ArrayList<ItemStruct> items = entity.getItems();
        entityInvModel.clear();
        ItemRegistry registry = new ItemRegistry();
        for (ItemStruct i : items) {
            ItemStruct struct = registry.getItemStruct(i.getItemId());
            struct.setQty(i.getQty());
            entityInvModel.addElement(struct);
        }
    }

    private void changeItemQty(String textData, ItemStruct item){
        Scanner sc = new Scanner(textData);
        if (sc.hasNextInt()){
            int qty = Integer.parseInt(textData);
            if (qty < 0)  qty = 0;
            if (qty > 99) qty = 99;
            item.setQty(qty);
        }
    }

    private void onTextBoxUpdate(JTextField itemQtySetter, JList invSelectionList){
        changeItemQty(itemQtySetter.getText(), entity.getItems().get(invSelectionList.getSelectedIndex()));
        changeItemQty(itemQtySetter.getText(), (ItemStruct)invSelectionList.getSelectedValue());
        invSelectionList.repaint();
    }

}

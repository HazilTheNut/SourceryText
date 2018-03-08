package Editor;

import Game.Registries.EntityStruct;
import Game.Registries.ItemRegistry;
import Game.Registries.ItemStruct;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by Jared on 3/7/2018.
 */
public class EntityEditor extends JFrame {

    private EntityStruct entity;
    private DefaultListModel<ItemStruct> entityInvModel;

    public EntityEditor (EntityStruct struct){

        entity = struct;

        setTitle(entity.getEntityName());
        setSize(350, 400);

        JPanel selectionPanel = new JPanel();
        selectionPanel.setLayout(new BorderLayout());

        Font textFont = new Font(Font.MONOSPACED, Font.PLAIN, 12);

        ItemRegistry itemRegistry = new ItemRegistry();
        JComboBox<ItemStruct> newItemSelect = new JComboBox<>();

        int[] mapKeys = itemRegistry.getMapKeys();
        for (int key : mapKeys)
            newItemSelect.addItem(itemRegistry.getItemStruct(key));

        newItemSelect.setFont(textFont);
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
        itemQtySetter.addActionListener(e -> {
            changeItemQty(itemQtySetter.getText(), invSelectionList.getSelectedValue());
            invSelectionList.repaint();
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
        finishButton.addActionListener(e -> dispose());
        encapsulatingPanel.add(finishButton);

        add(encapsulatingPanel, BorderLayout.LINE_END);

        setVisible(true);
    }

    private void updateEntityInventory(){
        ArrayList<ItemStruct> items = entity.getItems();
        entityInvModel.clear();
        for (ItemStruct i : items) entityInvModel.addElement(i);
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
}

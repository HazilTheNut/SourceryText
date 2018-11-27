package Editor.DialgoueCreator;

import Data.ItemStruct;
import Game.Registries.ItemRegistry;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.util.Scanner;

public class DialogueTradeCreator extends JPanel {

    public DialogueTradeCreator(){

        JPanel topPanel = new JPanel();

        JButton addRowButton = new JButton("+");
        JButton removeRowButton = new JButton("-");

        Font font = new Font(Font.MONOSPACED, Font.PLAIN, 12);
        Font smallFont = new Font(Font.MONOSPACED, Font.PLAIN, 10);

        JTextArea outputArea = new JTextArea();
        outputArea.setPreferredSize(new Dimension(200, 25));
        outputArea.setEditable(false);

        JButton copyButton = new JButton("Copy");
        copyButton.addActionListener(e -> {
            StringSelection selection = new StringSelection(outputArea.getText());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
        });

        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.LINE_AXIS));
        topPanel.add(addRowButton);
        topPanel.add(removeRowButton);
        topPanel.add(outputArea);
        topPanel.add(copyButton);

        JComboBox<ItemStruct> itemSelect = new JComboBox<>();
        for (int key : ItemRegistry.getMapKeys()) itemSelect.addItem(ItemRegistry.getItemStruct(key));
        itemSelect.setMaximumRowCount(20);
        //itemSelect.setFont(smallFont);

        DefaultTableModel tableModel = new DefaultTableModel();
        tableModel.addColumn("Player Items");
        tableModel.addColumn("Qty");
        tableModel.addColumn("NPC Items");
        tableModel.addColumn("Qty");
        tableModel.addRow(new Object[]{"",1,"",1});
        JTable itemsTable = new JTable(tableModel);
        itemsTable.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(itemSelect));
        itemsTable.getColumnModel().getColumn(1).setMaxWidth(50);
        itemsTable.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(itemSelect));
        itemsTable.getColumnModel().getColumn(3).setMaxWidth(50);
        itemsTable.setPreferredScrollableViewportSize(new Dimension(400, 50));

        JScrollPane tablePane = new JScrollPane(itemsTable, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        tablePane.setMaximumSize(new Dimension(400, 200));

        addRowButton.addActionListener(e -> tableModel.addRow(new Object[]{"", 1,"",1}));
        removeRowButton.addActionListener(e -> tableModel.removeRow(tableModel.getRowCount()-1));

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new GridLayout(1, 4));

        JSpinner favorField = new JSpinner(new SpinnerNumberModel(0, -10, 10, 1));
        favorField.setBorder(BorderFactory.createTitledBorder("dFavor"));
        favorField.setFont(font);

        JSpinner successField = new JSpinner(new SpinnerNumberModel(0, 0, 999, 1));
        successField.setBorder(BorderFactory.createTitledBorder("Successful"));
        successField.setFont(font);

        JSpinner npcNoItemsField = new JSpinner(new SpinnerNumberModel(0, 0, 999, 1));
        npcNoItemsField.setBorder(BorderFactory.createTitledBorder("NPC Empty"));
        npcNoItemsField.setFont(font);

        JSpinner failedField = new JSpinner(new SpinnerNumberModel(0, 0, 999, 1));
        failedField.setBorder(BorderFactory.createTitledBorder("Failed"));
        failedField.setFont(font);

        bottomPanel.add(favorField);
        bottomPanel.add(new JLabel(" GOTO If: ", JLabel.TRAILING));
        bottomPanel.add(successField);
        bottomPanel.add(npcNoItemsField);
        bottomPanel.add(failedField);

        bottomPanel.setPreferredSize(new Dimension(425, 35));

        add(topPanel, BorderLayout.PAGE_START);
        add(tablePane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.PAGE_END);
        
        UtilityElements ue = new UtilityElements();
        ue.tableModel = tableModel;
        ue.dFavor = favorField;
        ue.ifSuccess = successField;
        ue.ifNPCEmpty = npcNoItemsField;
        ue.ifFailed = failedField;
        ue.output = outputArea;

        tableModel.addTableModelListener(e -> updateOutput(ue));
        favorField.getModel().addChangeListener(e -> updateOutput(ue));
        successField.getModel().addChangeListener(e -> updateOutput(ue));
        npcNoItemsField.getModel().addChangeListener(e -> updateOutput(ue));
        failedField.getModel().addChangeListener(e -> updateOutput(ue));

        setPreferredSize(new Dimension(450, 205));
    }
    
    private class UtilityElements {
        DefaultTableModel tableModel;
        JSpinner dFavor;
        JSpinner ifSuccess;
        JSpinner ifFailed;
        JSpinner ifNPCEmpty;
        JTextArea output;
    }

    private void updateOutput(UtilityElements ue){
        StringBuilder builder = new StringBuilder();
        builder.append("$");
        //Player Items
        for (int i = 0; i < ue.tableModel.getRowCount(); i++) {
            Object obj = ue.tableModel.getValueAt(i, 0);
            int qty = getFirstInt(ue.tableModel.getValueAt(i, 1).toString());
            if (obj instanceof ItemStruct && qty > 0) {
                ItemStruct itemStruct = (ItemStruct) obj;
                builder.append(String.format("%1$dx%2$d", itemStruct.getItemId(), qty));
            }
        }
        builder.append("|");
        //NPC Items
        for (int i = 0; i < ue.tableModel.getRowCount(); i++) {
            Object obj = ue.tableModel.getValueAt(i, 2);
            int qty = getFirstInt(ue.tableModel.getValueAt(i, 3).toString());
            if (obj instanceof ItemStruct && qty > 0) {
                ItemStruct itemStruct = (ItemStruct) obj;
                builder.append(String.format("%1$dx%2$d", itemStruct.getItemId(), qty));
            }
        }
        //All the spinners at the bottom
        builder.append(String.format("|%1$s", ue.dFavor.getValue()));
        builder.append(String.format("|%1$s", ue.ifSuccess.getValue()));
        builder.append(String.format("|%1$s", ue.ifNPCEmpty.getValue()));
        builder.append(String.format("|%1$s", ue.ifFailed.getValue()));
        ue.output.setText(builder.append('$').toString());
    }

    private int getFirstInt(String str){
        Scanner scanner = new Scanner(str);
        try {
            return scanner.nextInt();
        } catch (NumberFormatException e){
            return 0;
        }
    }
}

package Editor.DialgoueCreator;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.util.InputMismatchException;
import java.util.Scanner;

public class DialogueOptionsCreator extends JPanel {

    public DialogueOptionsCreator(){

        JPanel topPanel = new JPanel();

        JButton addRowButton = new JButton("+");
        JButton removeRowButton = new JButton("-");

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

        DefaultTableModel tableModel = new DefaultTableModel();
        tableModel.addColumn("Response");
        tableModel.addColumn("Goto");
        tableModel.addRow(new Object[]{"Enter your response here...",0});
        tableModel.addTableModelListener(e -> outputArea.setText(generateDialgoueOptions(tableModel)));
        JTable dialogueTable = new JTable(tableModel);
        //dialogueTable.setFillsViewportHeight(true);
        dialogueTable.getColumnModel().getColumn(1).setMaxWidth(50);
        dialogueTable.setPreferredScrollableViewportSize(new Dimension(400, 50));

        JScrollPane tablePane = new JScrollPane(dialogueTable, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        tablePane.setMaximumSize(new Dimension(400, 200));

        addRowButton.addActionListener(e -> tableModel.addRow(new Object[]{"", 0}));
        removeRowButton.addActionListener(e -> tableModel.removeRow(tableModel.getRowCount()-1));

        add(topPanel, BorderLayout.PAGE_START);
        add(tablePane, BorderLayout.CENTER);

        setPreferredSize(new Dimension(450, 175));
    }

    private String generateDialgoueOptions(DefaultTableModel tableModel){
        StringBuilder builder = new StringBuilder("{");
        for (int row = 0; row < tableModel.getRowCount(); row++) {
            String response = (String)tableModel.getValueAt(row, 0);
            int gotoValue = 0;
            try {
                String gotoStr = tableModel.getValueAt(row, 1).toString();
                Scanner scanner = new Scanner(gotoStr);
                gotoValue = scanner.nextInt();
            } catch (InputMismatchException e){
                e.printStackTrace();
            }
            if (row > 0) builder.append('|');
            builder.append(String.format("%1$s=%2$d", response, gotoValue));
        }
        return builder.append('}').toString();
    }
}

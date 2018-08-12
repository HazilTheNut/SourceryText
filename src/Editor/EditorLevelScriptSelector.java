package Editor;

import Data.LevelData;
import Game.Registries.LevelScriptRegistry;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Created by Jared on 4/8/2018.
 */
public class EditorLevelScriptSelector extends JFrame {

    /**
     * EditorEntitySelector:
     *
     * The Window that pops up when you want to pick a LevelScript to edit.
     * Could have been a dropdown menu, like the tiles are, but here you get a bigger list.
     */

    @SuppressWarnings("unchecked")
    EditorLevelScriptSelector(EditorToolPanel editorToolPanel, LevelData ldata) {

        setTitle("Level Script Selector");
        setSize(new Dimension(400, 300));

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        //Create the list
        JList<LevelScriptStruct> scriptList = new JList<>();
        DefaultListModel<LevelScriptStruct> listModel = new DefaultListModel<>();
        for (int id : ldata.getLevelScripts()) {
            LevelScriptStruct struct = new LevelScriptStruct();
            struct.id = id;
            struct.name = LevelScriptRegistry.getLevelScriptClass(id).getSimpleName();
            listModel.addElement(struct);
        }
        scriptList.setCellRenderer(new LevelScriptListRenderer());
        scriptList.setModel(listModel);

        JScrollPane listPane = new JScrollPane(scriptList, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        scriptList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) { //Handles double-clicking to select the level script
                    doSelection(scriptList, editorToolPanel, ldata);
                    dispose();
                }
            }
        });

        //Create the dropdown menu
        JComboBox<LevelScriptStruct> scriptComboBox = new JComboBox<>();
        for (int id : LevelScriptRegistry.getMapKeys()){
            LevelScriptStruct struct = new LevelScriptStruct();
            struct.id = id;
            struct.name = LevelScriptRegistry.getLevelScriptClass(id).getSimpleName();
            scriptComboBox.addItem(struct);
        }

        mainPanel.add(scriptComboBox, BorderLayout.PAGE_START);
        mainPanel.add(listPane, BorderLayout.CENTER);

        mainPanel.validate();

        add(mainPanel, BorderLayout.CENTER);

        //And of course, the panel on the side
        JPanel sidePanel = new JPanel();
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.PAGE_AXIS));

        JButton addButton = new JButton("+");
        addButton.addActionListener(e -> {
            if (scriptComboBox.getSelectedItem() instanceof LevelScriptStruct) {
                LevelScriptStruct selectedItem = (LevelScriptStruct) scriptComboBox.getSelectedItem();
                if (!ldata.getLevelScripts().contains(selectedItem.id)) {
                    ldata.addLevelScript(selectedItem.id);
                    listModel.addElement(selectedItem);
                }
            }
        });
        addButton.setMaximumSize(new Dimension(65, 38));
        sidePanel.add(addButton);

        JButton removeButton = new JButton("-");
        removeButton.addActionListener(e -> {
            if (scriptList.getSelectedValue() != null) {
                ldata.removeLevelScript(scriptList.getSelectedValue().id);
                listModel.removeElementAt(scriptList.getSelectedIndex());
            }
        });
        removeButton.setMaximumSize(new Dimension(65, 38));
        sidePanel.add(removeButton);

        sidePanel.add(Box.createVerticalGlue());

        JButton cancelButton = new JButton("Exit");
        cancelButton.addActionListener(e -> dispose());
        cancelButton.setMaximumSize(new Dimension(65, 30));
        sidePanel.add(cancelButton);

        JButton finishButton = new JButton("Select");
        finishButton.addActionListener(e -> {
            doSelection(scriptList, editorToolPanel, ldata);
            dispose();
        });
        finishButton.setMaximumSize(new Dimension(65, 30));
        sidePanel.add(finishButton);

        add(sidePanel, BorderLayout.LINE_END);

        validate();

        setVisible(true);
    }

    private void doSelection(JList<LevelScriptStruct> scriptList, EditorToolPanel editorToolPanel, LevelData ldata){
        if (scriptList.getSelectedValue() != null && scriptList.getSelectedValue().id != editorToolPanel.getSelectedLevelScript()) {
            editorToolPanel.assignLevelScriptPanel(LevelScriptRegistry.getLevelScript(scriptList.getSelectedValue().id), ldata);
            ldata.getLevelScriptLayer().setVisible(false);
        }
    }

    private class LevelScriptStruct {
        private String name;
        private int id;

        @Override
        public String toString() {
            return name;
        }
    }

    class LevelScriptListRenderer extends JLabel implements ListCellRenderer {

        LevelScriptListRenderer(){
            setOpaque(true);
            setHorizontalAlignment(LEFT);
            setVerticalAlignment(CENTER);
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
            setText(value.toString());
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                if (index % 2 == 0)
                    setBackground(list.getBackground());
                else
                    setBackground(shade(list.getBackground()));
                setForeground(list.getForeground());
            }
            return this;
        }

        private Color shade(Color color){
            float multiplier = 0.96f;
            int[] rgb = {(int)(color.getRed() * multiplier), (int)(color.getGreen() * multiplier), (int)(color.getBlue() * multiplier)};
            //System.out.printf("[EditorEntitySelector.shade] r: %1$d g: %2$d b: %3$d\n", rgb[0], rgb[1], rgb[2]);
            return new Color(rgb[0], rgb[1], rgb[2]);
        }
    }
}

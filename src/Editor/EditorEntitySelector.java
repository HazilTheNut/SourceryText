package Editor;

import Data.EntityStruct;
import Editor.DrawTools.EntityPlace;
import Game.Registries.EntityRegistry;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Created by Jared on 4/8/2018.
 */
public class EditorEntitySelector extends JFrame {

    public EditorEntitySelector(EditorToolPanel editorToolPanel){

        setTitle("Entity Selector");
        setSize(new Dimension(400, 300));

        JList<EntityStruct> structList = new JList<>();
        DefaultListModel<EntityStruct> entityStructModel = new DefaultListModel<>();
        EntityRegistry entityRegistry = new EntityRegistry();
        for (int id : entityRegistry.getMapKeys()){
            EntityStruct struct = entityRegistry.getEntityStruct(id);
            entityStructModel.addElement(struct);
        }
        structList.setModel(entityStructModel);
        structList.setLayoutOrientation(JList.VERTICAL);
        structList.setFont(new Font(Font.DIALOG, Font.PLAIN, 12));

        JScrollPane listPane = new JScrollPane(structList, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        structList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2){
                    editorToolPanel.assignEntityPlaceStruct(structList.getSelectedValue());
                    dispose();
                }
            }
        });

        add(listPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.LINE_AXIS));

        bottomPanel.add(Box.createHorizontalGlue());

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());
        bottomPanel.add(cancelButton);

        JButton finishButton = new JButton("Finish");
        finishButton.addActionListener(e -> {
            editorToolPanel.assignEntityPlaceStruct(structList.getSelectedValue());
            dispose();
        });
        bottomPanel.add(finishButton);

        add(bottomPanel, BorderLayout.PAGE_END);

        validate();

        setVisible(true);
    }
}

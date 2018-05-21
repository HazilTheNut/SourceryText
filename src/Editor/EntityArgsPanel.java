package Editor;

import Data.Coordinate;
import Data.EntityArg;
import Data.EntityStruct;
import Game.Entities.Entity;
import Game.Registries.EntityRegistry;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.ArrayList;

/**
 * Created by Jared on 4/15/2018.
 */
public class EntityArgsPanel extends JPanel {

    public EntityArgsPanel(JFrame frame, EntityStruct entityStruct){
        setLayout(new BorderLayout());

        JPanel argEditorPanel = new JPanel();
        argEditorPanel.setLayout(new BoxLayout(argEditorPanel, BoxLayout.PAGE_AXIS));

        Entity generated = null;
        try {
            generated = (Entity) EntityRegistry.getEntityClass(entityStruct.getEntityId()).newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        if (generated != null) {
            generated.simpleInit(entityStruct, new Coordinate(0, 0));
            ArrayList<EntityArg> args = generated.generateArgs();
            if (args != null) {
                for (EntityArg arg : args) {
                    if (!entityStruct.hasArg(arg.getArgName()))
                        entityStruct.addArg(arg);
                }
            }
        }
        for (EntityArg arg : entityStruct.getArgs()){
            argEditorPanel.add(new ArgEditor(arg));
        }

        JScrollPane scrollPane = new JScrollPane(argEditorPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.LINE_AXIS));

        bottomPanel.add(Box.createHorizontalGlue());

        JButton finishButton = new JButton("Done");
        finishButton.addActionListener(e -> frame.dispose());
        bottomPanel.add(finishButton);

        add(bottomPanel, BorderLayout.PAGE_END);
    }

    private class ArgEditor extends JPanel{

        JLabel argNameLabel;
        JTextField argValueField;

        private ArgEditor(EntityArg arg){
            setLayout(new BorderLayout());

            argNameLabel = new JLabel(arg.getArgName() + ": ", SwingConstants.TRAILING);
            argNameLabel.setPreferredSize(new Dimension(80, 25));
            argNameLabel.setBorder(BorderFactory.createEtchedBorder());

            argValueField = new JTextField(arg.getArgValue());
            argValueField.setEditable(true);
            argValueField.setMinimumSize(new Dimension(200, 30));
            argValueField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    arg.setArgValue(argValueField.getText());
                }
                @Override
                public void removeUpdate(DocumentEvent e) {
                    arg.setArgValue(argValueField.getText());
                }
                @Override
                public void changedUpdate(DocumentEvent e) {
                    arg.setArgValue(argValueField.getText());
                }
            });

            setMaximumSize(new Dimension(9001, 30));
            add(argNameLabel, BorderLayout.LINE_START);
            add(argValueField, BorderLayout.CENTER);
            setBorder(BorderFactory.createEtchedBorder());
        }
    }
}

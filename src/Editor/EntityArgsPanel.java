package Editor;

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

        EntityRegistry entityRegistry = new EntityRegistry();
        Entity generated = null;
        try {
            generated = (Entity)entityRegistry.getEntityClass(entityStruct.getEntityId()).newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        if (entityStruct.getArgs().size() == 0) {
            if (generated != null) {
                ArrayList<EntityArg> args = generated.generateArgs();
                if (args != null) {
                    for (EntityArg arg : args) {
                        entityStruct.addArg(arg);
                    }
                }
            }
        }
        for (EntityArg arg : entityStruct.getArgs()){
            argEditorPanel.add(new ArgEditor(arg));
        }

        JScrollPane scrollPane = new JScrollPane(argEditorPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

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
            argNameLabel.setPreferredSize(new Dimension(80, 30));
            argNameLabel.setBorder(BorderFactory.createEtchedBorder());

            argValueField = new JTextField(arg.getArgValue());
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

            setMaximumSize(new Dimension(500, 30));
            add(argNameLabel, BorderLayout.LINE_START);
            add(argValueField, BorderLayout.CENTER);
            setBorder(BorderFactory.createEtchedBorder());
        }
    }
}

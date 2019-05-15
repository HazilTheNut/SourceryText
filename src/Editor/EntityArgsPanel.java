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

    /**
     * EntityArgsPanel:
     *
     * In the code, Entity Attributes are named EntityArgs. Due to compatibility issues, EntityArg will not be renamed.
     * And thus, the naming follows for EntityArgsPanel too.
     */

    public EntityArgsPanel(JFrame frame, EntityStruct entityStruct){
        setLayout(new BorderLayout());

        JPanel argEditorPanel = new JPanel();
        argEditorPanel.setLayout(new BoxLayout(argEditorPanel, BoxLayout.PAGE_AXIS));

        //In order to get what EntityArgs to fill into the list, an actual Entity must be instantiated to generate the necessary EntityArgs.
        Entity generated = null;
        try {
            generated = (Entity) EntityRegistry.getEntityClass(entityStruct.getEntityId()).newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        if (generated != null) { //If Java's reflection stuff worked, we can now get down to business
            generated.simpleInit(entityStruct, new Coordinate(0, 0)); //SimpleInit covers exactly enough to get what we need, without any nasty side effects.
            ArrayList<EntityArg> args = generated.generateArgs();
            if (args != null) {
                for (EntityArg arg : args) {
                    if (!entityStruct.hasArg(arg.getArgName()))
                        entityStruct.addArg(arg);
                }
            }
        }
        int width = calculateLabelWidth(entityStruct);
        for (EntityArg arg : entityStruct.getArgs()){
            argEditorPanel.add(new ArgEditor(arg, width));
        }

        JScrollPane scrollPane = new JScrollPane(argEditorPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.LINE_AXIS));

        JButton resetButton = new JButton("Reset");
        resetButton.addActionListener(e -> reset(entityStruct, argEditorPanel));
        bottomPanel.add(resetButton);

        bottomPanel.add(Box.createHorizontalGlue()); //We want the bottom buttons to be right-justified, consistent with all the other UI's

        JButton finishButton = new JButton("Done");
        finishButton.addActionListener(e -> frame.dispose());
        bottomPanel.add(finishButton);

        add(bottomPanel, BorderLayout.PAGE_END);
    }

    private void reset(EntityStruct entityStruct, JPanel argEditorPanel){
        //Generate an entity so that we can obtain its EntityArgs
        Entity generated = null;
        try {
            generated = (Entity) EntityRegistry.getEntityClass(entityStruct.getEntityId()).newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        if (generated != null) {
            argEditorPanel.removeAll(); //Clear everything out
            entityStruct.getArgs().clear();
            generated.simpleInit(entityStruct, new Coordinate(0, 0));
            ArrayList<EntityArg> args = generated.generateArgs();
            if (args != null) {
                for (EntityArg arg : args) {
                    writeArgValue(entityStruct, arg.getArgName(), arg.getArgValue()); //Write stuff onto the entity struct
                }
            }
            int width = calculateLabelWidth(entityStruct);
            for (EntityArg arg : entityStruct.getArgs()){ //Update editors once finished
                argEditorPanel.add(new ArgEditor(arg, width));
            }
            argEditorPanel.validate();
            argEditorPanel.repaint();
        }
    }

    private int calculateLabelWidth(EntityStruct entityStruct){
        FontMetrics metrics = getFontMetrics(getFont());
        int maxWidth = 0;
        for (EntityArg arg : entityStruct.getArgs())
            maxWidth = Math.max(maxWidth, metrics.stringWidth(arg.getArgName()));
        return maxWidth + 15;
    }

    private void writeArgValue(EntityStruct entityStruct, String name, String value){
        for (EntityArg arg : entityStruct.getArgs()){
            if (arg.getArgName().equals(name)) {
                arg.setArgValue(value);
                return;
            }
        }
        entityStruct.getArgs().add(new EntityArg(name, value));
    }

    private class ArgEditor extends JPanel{

        /**
         * ArgEditor:
         *
         * Represents each 'cell' in the Attributes pane.
         */

        JLabel argNameLabel;
        JTextField argValueField;

        private ArgEditor(EntityArg arg, int labelWidth){
            setLayout(new BorderLayout()); //BorderLayout used because it does pack things.

            argNameLabel = new JLabel(arg.getArgName() + ": ", SwingConstants.TRAILING);
            argNameLabel.setPreferredSize(new Dimension(labelWidth, 25));
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

            setMaximumSize(new Dimension(9001, 30)); //Supports most screen resolutions
            add(argNameLabel, BorderLayout.LINE_START);
            add(argValueField, BorderLayout.CENTER);
            setBorder(BorderFactory.createEtchedBorder());
        }
    }
}

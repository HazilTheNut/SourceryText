package Editor;

import Data.EntityStruct;
import Engine.SpecialText;
import Game.Registries.EntityRegistry;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Created by Jared on 4/8/2018.
 */
public class EditorEntitySelector extends JFrame {

    /**
     * EditorEntitySelector:
     *
     * The Window that pops up when you want to pick an entity to place.
     * Could have been a dropdown menu, like the tiles are, but here you get a bigger list.
     *
     * Plus, filters can be added later if the list gets super big.
     */

    @SuppressWarnings("unchecked")
    EditorEntitySelector(EditorToolPanel editorToolPanel){

        setTitle("Entity Selector");
        setSize(new Dimension(400, 300));

        JList<EntityStruct> structList = new JList<>(); //A JList is used to create the list of options.
        DefaultListModel<EntityStruct> entityStructModel = new DefaultListModel<>();
        for (int id : EntityRegistry.getMapKeys()){
            EntityStruct struct = EntityRegistry.getEntityStruct(id); //Gets everything the EntityRegistry has in its registry.
            entityStructModel.addElement(struct);
        }
        structList.setModel(entityStructModel);
        structList.setLayoutOrientation(JList.VERTICAL);
        EntityListRenderer renderer = new EntityListRenderer(); //I really wanted to show the icon of the entity next to its name.
        structList.setCellRenderer(renderer);

        JScrollPane listPane = new JScrollPane(structList, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        structList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2){ //Handles double-clicking to select the entity
                    editorToolPanel.assignEntityPlaceStruct(structList.getSelectedValue());
                    dispose();
                }
            }
        });

        add(listPane, BorderLayout.CENTER);

        //And of course, the panel at the bottom.
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.LINE_AXIS));

        bottomPanel.add(Box.createHorizontalGlue());

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());
        bottomPanel.add(cancelButton);

        JButton finishButton = new JButton("Finish");
        finishButton.addActionListener(e -> {
            if (structList.getSelectedValue() != null)
                editorToolPanel.assignEntityPlaceStruct(structList.getSelectedValue());
            dispose();
        });
        bottomPanel.add(finishButton);

        add(bottomPanel, BorderLayout.PAGE_END);

        validate();

        setVisible(true);
    }

    class EntityListRenderer extends JLabel implements ListCellRenderer {

        public EntityListRenderer(){
            setOpaque(true);
            setHorizontalAlignment(LEFT);
            setVerticalAlignment(CENTER);
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value instanceof EntityStruct){
                EntityStruct struct = (EntityStruct)value;
                Color bkg = struct.getDisplayChar().getBkgColor();
                Color newBkg = new Color((bkg.getRed() * bkg.getAlpha()) / 255, (bkg.getGreen() * bkg.getAlpha()) / 255, (bkg.getBlue() * bkg.getAlpha()) / 255);
                SpecialText icon = new SpecialText(struct.getDisplayChar().getCharacter(), struct.getDisplayChar().getFgColor(), newBkg);
                setIcon(new SingleTextRenderer(icon));
                setText(struct.getEntityName());
            } else {
                setIcon(new SingleTextRenderer(new SpecialText('/', Color.RED, Color.BLACK)));
                setText("GAME LOAD ERROR");
            }
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

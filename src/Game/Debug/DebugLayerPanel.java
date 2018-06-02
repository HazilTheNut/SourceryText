package Game.Debug;

import Engine.Layer;

import javax.swing.*;
import java.awt.*;

public class DebugLayerPanel extends JPanel{

    /**
     * DebugLayerPanel:
     *
     * The 'Layers' panel of the DebugWindow.
     */

    private JPanel listPanel;

    public DebugLayerPanel(){

        setLayout(new BorderLayout());

        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.PAGE_AXIS));
        listPanel.setBackground(Color.BLACK);

        JScrollPane scrollPane = new JScrollPane(listPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane, BorderLayout.CENTER);

        validate();
    }

    void addLayerView(Layer toView, int pos){
        listPanel.add(new LayerView(toView), pos);
        listPanel.validate();
    }

    void removeLayerView(Layer toView){
        for (Component c : listPanel.getComponents()){
            if (c instanceof LayerView) {
                LayerView layerView = (LayerView) c;
                if (layerView.view.getName().equals(toView.getName())){
                    listPanel.remove(c);
                }
            }
        }
        listPanel.validate();
    }

    void updateLayerCheckBoxes(){
        for (Component c : listPanel.getComponents()){
            if (c instanceof LayerView) {
                LayerView layerView = (LayerView) c;
                layerView.visibleCheckBox.setSelected(layerView.view.getVisible());
            }
        }
    }

    private class LayerView extends JPanel{

        /**
         * LayerView:
         *
         * Handles a single Layer and its associated check box
         */

        Layer view;
        JCheckBox visibleCheckBox;

        private LayerView (Layer toView) {
            view = toView;

            visibleCheckBox = new JCheckBox();
            visibleCheckBox.setSelected(view.getVisible());
            visibleCheckBox.addItemListener(e -> view.setVisible(visibleCheckBox.isSelected()));
            visibleCheckBox.setBackground(Color.BLACK);

            JLabel layerLabel = new JLabel(String.format("| %1$-2d | %2$s", view.getImportance(), view.getName()));
            layerLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            layerLabel.setForeground(DebugWindow.textColor);

            setLayout(new BorderLayout());

            add(visibleCheckBox, BorderLayout.LINE_START);
            add(layerLabel, BorderLayout.CENTER);

            setMaximumSize(new Dimension(1500, 20));
            setBorder(BorderFactory.createEtchedBorder());

            setBackground(Color.BLACK);

            validate();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof LayerView) {
                LayerView layerView = (LayerView) obj;
                return layerView.view.getName().equals(view.getName());
            }
            return false;
        }
    }
}

package Editor;

import Engine.Layer;

import javax.swing.*;
import java.awt.*;

public class LayerToggler extends JPanel {

    private Layer layer;
    private JCheckBox checkBox;

    public LayerToggler(Layer layer, String name){

        this.layer = layer;

        setLayout(new BorderLayout());

        checkBox = new JCheckBox();
        checkBox.setSelected(layer.getVisible());
        checkBox.addActionListener(e -> layer.setVisible(checkBox.isSelected()));

        add(checkBox, BorderLayout.LINE_START);
        add(new JLabel(name), BorderLayout.CENTER);

        setBorder(BorderFactory.createEtchedBorder());
    }

    public void update(){
        checkBox.setSelected(layer.getVisible());
    }

    public Layer getLayer() {
        return layer;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LayerToggler) {
            LayerToggler layerToggler = (LayerToggler) obj;
            return layerToggler.getLayer().equals(layer);
        }
        return false;
    }
}

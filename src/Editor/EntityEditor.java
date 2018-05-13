package Editor;

import Data.EntityStruct;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Jared on 3/7/2018.
 */
public class EntityEditor extends JFrame {

    private EntityStruct entity;

    public EntityEditor (EntityStruct struct){

        entity = struct;

        setTitle(entity.getEntityName());
        setMinimumSize(new Dimension(350, 350));

        JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.addTab("Items", new EntityInventoryPanel(this, entity));

        tabbedPane.addTab("Attributes", new EntityArgsPanel(this, entity));

        add(tabbedPane);

        setVisible(true);
    }

}

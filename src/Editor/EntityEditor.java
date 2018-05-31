package Editor;

import Data.EntityStruct;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Jared on 3/7/2018.
 */
public class EntityEditor extends JFrame {

    /**
     * EntityEditor:
     *
     * A JFrame that establishes a JTabbedPane and then fills it with
     * 1) An EntityInventoryPanel
     * 2) An EntityArgsPanel
     */

    public EntityEditor (EntityStruct struct){

        setTitle(struct.getEntityName());
        setMinimumSize(new Dimension(350, 350));

        JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.addTab("Items", new EntityInventoryPanel(this, struct));

        tabbedPane.addTab("Attributes", new EntityArgsPanel(this, struct));

        add(tabbedPane);

        setVisible(true);
    }

}

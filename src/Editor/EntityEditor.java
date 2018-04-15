package Editor;

import Data.EntityStruct;
import Game.Registries.ItemRegistry;
import Data.ItemStruct;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.Scanner;

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

        tabbedPane.addTab("Args", new EntityArgsPanel(this, entity));

        add(tabbedPane);

        setVisible(true);
    }

}

package Editor.DialgoueCreator;

import Game.FactionManager;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

public class DialogueFactionsReference extends JPanel {

    public DialogueFactionsReference(){

        FactionManager factionManager = new FactionManager();
        factionManager.initialize();

        setPreferredSize(new Dimension(450, 170));
        setLayout(new FlowLayout());

        for (FactionManager.Faction faction : factionManager.getFactions()){
            JButton btn = new JButton(faction.getName());
            btn.addActionListener(e -> {
                StringSelection generated = new StringSelection(faction.getName());
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(generated, generated);
            });
            add(btn);
        }

        validate();
    }

}

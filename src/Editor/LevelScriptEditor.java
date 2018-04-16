package Editor;

import Data.LevelData;
import Game.Registries.LevelScriptRegistry;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Jared on 4/16/2018.
 */
public class LevelScriptEditor extends JFrame {

    public LevelScriptEditor(LevelData ldata){
        setTitle("Level Scripts");
        setMinimumSize(new Dimension(350, 300));

        JPanel enabledScriptsPanel = new JPanel();
        enabledScriptsPanel.setLayout(new BoxLayout(enabledScriptsPanel, BoxLayout.PAGE_AXIS));
        enabledScriptsPanel.setBorder(BorderFactory.createEtchedBorder());

        JPanel disabledScriptsPanel = new JPanel();
        disabledScriptsPanel.setLayout(new BoxLayout(disabledScriptsPanel, BoxLayout.PAGE_AXIS));
        disabledScriptsPanel.setBorder(BorderFactory.createEtchedBorder());

        JPanel scriptsMasterPanel = new JPanel();
        LevelScriptRegistry lsr = new LevelScriptRegistry();
        for (int id : lsr.getMapKeys()) {
            Class scriptClass = lsr.getLevelScriptClass(id);
            if (scriptClass != null) {
                new LevelButtonPanel(scriptClass, enabledScriptsPanel, disabledScriptsPanel, scriptsMasterPanel, ldata, id);
            }
        }

        scriptsMasterPanel.setLayout(new BorderLayout());
        enabledScriptsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        scriptsMasterPanel.add(enabledScriptsPanel, BorderLayout.PAGE_START);
        disabledScriptsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        scriptsMasterPanel.add(disabledScriptsPanel, BorderLayout.CENTER);

        JScrollPane scrollPane = new JScrollPane(scriptsMasterPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.LINE_AXIS));

        bottomPanel.add(Box.createHorizontalGlue());

        JButton finishButton = new JButton("Done");
        finishButton.addActionListener(e -> dispose());
        bottomPanel.add(finishButton);

        add(bottomPanel, BorderLayout.PAGE_END);

        validate();

        setVisible(true);
    }

    private class LevelButtonPanel extends JPanel {
        JButton button;
        JLabel scriptLabel;

        JPanel enabledScriptsPanel;
        JPanel disabledScriptsPanel;
        JPanel masterPanel;
        boolean isEnabled = false;

        LevelData ldata;
        int id;

        private LevelButtonPanel(Class scriptClass, JPanel enabledScriptsPanel, JPanel disabledScriptsPanel, JPanel masterPanel, LevelData ldata, int scriptId){
            this.enabledScriptsPanel = enabledScriptsPanel;
            this.disabledScriptsPanel = disabledScriptsPanel;
            this.masterPanel = masterPanel;
            setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
            button = new JButton("+");
            button.setMargin(new Insets(1, 1, 1, 1));
            button.setMaximumSize(new Dimension(20, 20));
            button.setMinimumSize(new Dimension(20, 20));
            button.addActionListener(e -> toggleEnable());
            scriptLabel = new JLabel(scriptClass.getSimpleName());
            add(button);
            add(Box.createRigidArea(new Dimension(5, 5)));
            add(scriptLabel);
            setAlignmentX(LEFT_ALIGNMENT);
            validate();
            this.ldata = ldata;
            id = scriptId;
            if (ldata.hasScript(id))
                moveToEnabled();
            else
                moveToDisabled();
        }

        private void toggleEnable(){
            if (isEnabled) {
                moveToDisabled();
                ldata.removeLevelScript(id);
            } else {
                moveToEnabled();
                ldata.addLevelScript(id);
            }
        }

        private void moveToEnabled(){
            isEnabled = true;
            disabledScriptsPanel.remove(this);
            button.setText("-");
            scriptLabel.setForeground(new Color(5, 75, 5));
            Font labelFont = scriptLabel.getFont();
            scriptLabel.setFont(new Font(labelFont.getFontName(), Font.BOLD, labelFont.getSize()));
            scriptLabel.repaint();
            enabledScriptsPanel.add(this);
            updatePanels();
        }

        private void moveToDisabled(){
            isEnabled = false;
            enabledScriptsPanel.remove(this);
            button.setText("+");
            scriptLabel.setForeground(Color.BLACK);
            Font labelFont = scriptLabel.getFont();
            scriptLabel.setFont(new Font(labelFont.getName(), Font.PLAIN, labelFont.getSize()));
            scriptLabel.repaint();
            disabledScriptsPanel.add(this);
            updatePanels();
        }

        private void updatePanels(){
            enabledScriptsPanel.validate();
            enabledScriptsPanel.repaint();
            disabledScriptsPanel.validate();
            disabledScriptsPanel.repaint();
            masterPanel.updateUI();
        }
    }
}

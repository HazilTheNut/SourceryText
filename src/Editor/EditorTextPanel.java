package Editor;

import Engine.SpecialText;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

/**
 * Created by Jared on 2/20/2018.
 */
public class EditorTextPanel extends JPanel implements ActionListener{

    /**
     * EditorTextPanel:
     *
     * The panel on the left of the Level Editor that handles creating, selecting, editing, and deleting SpecialText.
     * Thus the name, 'Text Panel'
     */

    private JPanel textBtnPanel;

    SpecialText selectedSpecialText = new SpecialText(' ');
    private SingleTextRenderer selectionRender;
    private JLabel selectionLabel;
    private JButton selectedTextButton;

    private ArrayList<JButton> buttonManifest = new ArrayList<>(); //Useful when importing buttons into another EditorWindow

    private EditorToolPanel toolPanel;

    public EditorTextPanel (){
        BorderLayout layout = new BorderLayout();
        setPreferredSize(new Dimension(60, 500));
        setLayout(layout);

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout(0, 0));

        //Create the very large 'Add New SpecialText' button.
        JButton addNewTextButton = new JButton("+");
        addNewTextButton.setFont(new Font("Monospaced", Font.BOLD, 20));
        addNewTextButton.setSelected(false);
        addNewTextButton.setMargin(new Insets(2, 2, 2, 2));
        addNewTextButton.setActionCommand("Add New Btn");
        addNewTextButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_A, 0), "add");
        addNewTextButton.getActionMap().put("add", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addNewTextButton.doClick();
            }
        });
        addNewTextButton.addActionListener(this);

        topPanel.add(addNewTextButton, BorderLayout.CENTER);

        add(topPanel, BorderLayout.PAGE_START);

        //Create the middle panel for the SpecialText buttons.
        textBtnPanel = new JPanel();
        textBtnPanel.setLayout(new BoxLayout(textBtnPanel, BoxLayout.PAGE_AXIS));
        JScrollPane textBtnScrollPane = new JScrollPane(textBtnPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        textBtnScrollPane.getVerticalScrollBar().setUnitIncrement(13);
        add(textBtnScrollPane, BorderLayout.CENTER);

        //Create the panel on the bottom with the preview and extra buttons.
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new GridLayout(3, 1, 1, 3));

        selectionRender = new SingleTextRenderer(null);
        selectionLabel = new JLabel(selectionRender);
        selectionLabel.setMaximumSize(new Dimension(20, 20));
        selectionLabel.setAlignmentX(CENTER_ALIGNMENT);

        bottomPanel.add(selectionLabel);

        JButton editButton = new JButton("Edit");
        editButton.setAlignmentX(CENTER_ALIGNMENT);
        editButton.setMargin(new Insets(4, 1, 4, 1));
        editButton.addActionListener(e -> editButton());

        JButton removeButton = new JButton("Remove");
        removeButton.setAlignmentX(CENTER_ALIGNMENT);
        removeButton.setMargin(new Insets(4, 1, 4, 1));
        removeButton.setActionCommand("Remove Btn");
        removeButton.addActionListener(this);

        bottomPanel.add(editButton);
        bottomPanel.add(removeButton);

        add(bottomPanel, BorderLayout.PAGE_END);

        validate();

        generateNewButton(null);
    }

    void setToolPanel(EditorToolPanel toolPanel) { this.toolPanel = toolPanel; }

    @Override
    public void actionPerformed(ActionEvent e) {
        System.out.printf("[EditorTextPanel.actionPerformed] Button cmd: %1$s\n", e.getActionCommand());
        if (e.getActionCommand().equals("Add New Btn")) { //Adds new button
            createNewButton();
        } else if (e.getActionCommand().equals("Remove Btn")){ //Removes selected button
            if (selectedTextButton != null){
                textBtnPanel.remove(selectedTextButton);
                buttonManifest.remove(selectedTextButton);
                System.out.printf("[EditorTextPanel.actionPerformed] Btn manifest size: %1$d\n", buttonManifest.size());
                textBtnPanel.validate();
                textBtnPanel.repaint();
                buttonManifest.get(buttonManifest.size()-1).doClick();
            }
        } else if (e.getActionCommand().equals("nulltext")){ //The 'null' button
            selectedSpecialText = null;
            selectionRender.specText = null;
            selectionLabel.repaint();
            if (toolPanel != null) toolPanel.updateSearchForIcon(selectedSpecialText);
        } else if (e.getActionCommand().equals("settext")){ //Any button in the middle panel.
            if (e.getSource() instanceof JButton) {
                JButton btn = (JButton) e.getSource();
                selectedSpecialText = ((SingleTextRenderer)btn.getIcon()).specText;
                System.out.printf("[EditorTextPanel.actionPerformed] Btn spec text: %1$s\n", ((SingleTextRenderer)btn.getIcon()).specText.toString());
                selectionRender.specText = selectedSpecialText;
                selectionLabel.repaint();
                if (toolPanel != null) toolPanel.updateSearchForIcon(selectedSpecialText);
                selectedTextButton = (JButton) e.getSource();
            } else {
                System.out.printf("[EditorTextPanel.actionPerformed] Not a button! %1$s\n", e.getActionCommand());
            }
        }
    }

    private JButton createBaseButton(){
        JButton btn = new JButton(new SingleTextRenderer(null));
        btn.setFont(new Font("Monospaced", Font.PLAIN, 12));
        btn.setHorizontalTextPosition(SwingConstants.LEFT);
        btn.setAlignmentX(0.25f);
        btn.setMargin(new Insets(2, 2, 2, 2));
        btn.setOpaque(true);
        btn.addActionListener(this);
        btn.setActionCommand("settext");
        textBtnPanel.add(btn, 0);
        buttonManifest.add(btn);
        System.out.printf("[EditorTextPanel] Btn manifest size: %1$d\n", buttonManifest.size());
        textBtnPanel.validate();
        validate();
        return btn;
    }

    public JButton generateNewButton(SpecialText text){
        JButton btn = createBaseButton();
        btn.setIcon(new SingleTextRenderer(text));
        if (text != null)
            btn.setActionCommand("settext");
        else
            btn.setActionCommand("nulltext");
        return btn;
    }

    private void createNewButton(){
        JButton btn = createBaseButton();
        EditorSpecialTextMaker textMaker;
        if (selectedTextButton == null || selectedTextButton.getIcon() == null)
            textMaker = new EditorSpecialTextMaker(textBtnPanel, btn, new SpecialText(' ', Color.WHITE, Color.BLACK), buttonManifest);
        else
            textMaker = new EditorSpecialTextMaker(textBtnPanel, btn, ((SingleTextRenderer)selectedTextButton.getIcon()).specText, buttonManifest);
        textMaker.setVisible(true);
    }

    private void editButton(){
        if (selectedTextButton != null && selectedTextButton.getIcon() != null) {
            EditorSpecialTextMaker textMaker = new EditorSpecialTextMaker(textBtnPanel, selectedTextButton, ((SingleTextRenderer) selectedTextButton.getIcon()).specText, buttonManifest);
            textMaker.setVisible(true);
        }
    }

    ArrayList<JButton> getButtonManifest() { return buttonManifest; }

    void setButtonPanelContents(ArrayList<JButton> btns){
        for (Component c : textBtnPanel.getComponents()) textBtnPanel.remove(c); //Removes all buttons in the panel
        buttonManifest = new ArrayList<>();
        generateNewButton(null); //Re-adds the null text button
        for (JButton btn1 : btns) {
            SpecialText otherText = ((SingleTextRenderer) btn1.getIcon()).specText;
            if (otherText != null) {
                JButton btn = createBaseButton();
                //Creates a new SpecialText that is a copy of the previous button. There must not be any pointer to the input button list.
                String toAssign = otherText.toString();
                ((SingleTextRenderer) btn.getIcon()).specText = SpecialText.fromString(toAssign);
                btn.addActionListener(this);
            }
        }
        validate();
    }
}

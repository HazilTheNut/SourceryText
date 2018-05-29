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

    private JPanel textBtnPanel;

    SpecialText selectedSpecialText = new SpecialText(' ');
    private SingleTextRenderer selectionRender;
    private JLabel selectionLabel;
    private JButton selectedTextButton;

    private ArrayList<JButton> buttonManifest = new ArrayList<>();

    private EditorToolPanel toolPanel;

    public EditorTextPanel (){
        BorderLayout layout = new BorderLayout();
        setPreferredSize(new Dimension(60, 500));
        setLayout(layout);

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout(0, 0));

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

        textBtnPanel = new JPanel();
        textBtnPanel.setLayout(new BoxLayout(textBtnPanel, BoxLayout.PAGE_AXIS));
        JScrollPane textBtnScrollPane = new JScrollPane(textBtnPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        textBtnScrollPane.getVerticalScrollBar().setUnitIncrement(13);
        add(textBtnScrollPane, BorderLayout.CENTER);

        //add(new JButton("Test"), BorderLayout.PAGE_END);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new GridLayout(3, 1, 1, 3));

        selectionRender = new SingleTextRenderer(null);
        selectionLabel = new JLabel(selectionRender);
        selectionLabel.setMaximumSize(new Dimension(20, 20));
        selectionLabel.setAlignmentX(CENTER_ALIGNMENT);

        //bottomPanel.add(Box.createRigidArea(new Dimension(1, 5)));
        bottomPanel.add(selectionLabel);
        //bottomPanel.add(Box.createRigidArea(new Dimension(1, 5)));

        JButton editButton = new JButton("Edit");
        editButton.setAlignmentX(CENTER_ALIGNMENT);
        editButton.setMargin(new Insets(4, 1, 4, 1));
        editButton.addActionListener(e -> createNewButton(selectedTextButton));

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
            createNewButton(createBaseButton());
        } else if (e.getActionCommand().equals("Remove Btn")){
            if (selectedTextButton != null){
                textBtnPanel.remove(selectedTextButton);
                buttonManifest.remove(selectedTextButton);
                System.out.printf("[EditorTextPanel.actionPerformed] Btn manifest size: %1$d\n", buttonManifest.size());
                textBtnPanel.validate();
                textBtnPanel.repaint();
            }
        } else if (e.getActionCommand().equals("nulltext")){
            selectedSpecialText = null;
            selectionRender.specText = null;
            selectionLabel.repaint();
        } else if (e.getActionCommand().equals("settext")){
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
        JButton btn = new JButton(new SingleTextRenderer(new SpecialText(' ')));
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

    private void createNewButton(JButton btn){
        EditorSpecialTextMaker textMaker;
        if (selectedTextButton == null || selectedTextButton.getIcon() == null)
            textMaker = new EditorSpecialTextMaker(textBtnPanel, btn, new SpecialText(' ', Color.WHITE, Color.BLACK), buttonManifest, true);
        else
            textMaker = new EditorSpecialTextMaker(textBtnPanel, btn, ((SingleTextRenderer)selectedTextButton.getIcon()).specText, buttonManifest, false);
        textMaker.setVisible(true);
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
    
    private void buildSpecTxtFromButtonClick(String command){
        System.out.println(command);
        System.out.println(command.substring(0,1));
        int[] textData = new int[9];
        int startIndex = 3;
        int endingIndex;
        for (int ii= 0; ii < 4; ii++){
            endingIndex = Math.min(command.indexOf(",", startIndex),command.indexOf("]", startIndex));
            String str = command.substring(startIndex, endingIndex);
            System.out.println(str);
            textData[ii] = Integer.valueOf(str);
            startIndex = endingIndex + 1;
        }
        //startIndex = command.indexOf("#")+2;
        startIndex += 2;
        for (int ii= 0; ii < 4; ii++){
            endingIndex = command.indexOf(",", startIndex);
            if (endingIndex < 0) endingIndex = command.indexOf("]", startIndex);
            String str = command.substring(startIndex, endingIndex);
            System.out.println(str);
            textData[ii+4] = Integer.valueOf(str);
            startIndex = endingIndex + 1;
        }
        System.out.println("----\n");
        Color fg = new Color(textData[0], textData[1], textData[2], textData[3]);
        Color bg = new Color(textData[4], textData[5], textData[6], textData[7]);
        selectedSpecialText = new SpecialText(command.substring(0,1).charAt(0), fg, bg);
        selectionRender.specText = selectedSpecialText;
        selectionLabel.repaint();
        if (toolPanel != null) toolPanel.updateSearchForIcon(selectedSpecialText);
    }

    ArrayList<JButton> getButtonManifest() { return buttonManifest; }

    void setButtonPanelContents(ArrayList<JButton> btns){
        for (Component c : textBtnPanel.getComponents()) textBtnPanel.remove(c);
        buttonManifest.clear();
        for (int ii = btns.size()-1; ii >= 0; ii--) {
            JButton btn = btns.get(ii);
            for (ActionListener listener : btn.getActionListeners()) btn.removeActionListener(listener);
            btn.addActionListener(this);
            textBtnPanel.add(btn);
            buttonManifest.add(btn);
        }
        validate();
    }
}

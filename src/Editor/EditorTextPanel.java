package Editor;

import Engine.SpecialText;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by Jared on 2/20/2018.
 */
public class EditorTextPanel extends JPanel implements ActionListener{

    private JScrollPane textBtnScrollPane;
    private JPanel textBtnPanel;

    SpecialText selectedSpecialText = new SpecialText(' ');
    private SingleTextRenderer selectionRender;
    private JLabel selectionLabel;
    private JButton selectedTextButton;

    public EditorTextPanel (){

        BorderLayout layout = new BorderLayout();
        setPreferredSize(new Dimension(50, 500));
        setLayout(layout);

        JButton addNewTextButton = new JButton("+");
        addNewTextButton.setFont(new Font("Monospaced", Font.BOLD, 20));
        addNewTextButton.setSelected(false);
        addNewTextButton.setMargin(new Insets(2, 2, 2, 2));
        addNewTextButton.setActionCommand("Add New Btn");
        addNewTextButton.addActionListener(this);

        add(addNewTextButton, BorderLayout.PAGE_START);

        textBtnPanel = new JPanel();
        textBtnPanel.setLayout(new BoxLayout(textBtnPanel, BoxLayout.PAGE_AXIS));
        textBtnScrollPane = new JScrollPane(textBtnPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        textBtnScrollPane.getVerticalScrollBar().setUnitIncrement(13);
        add(textBtnScrollPane, BorderLayout.CENTER);

        //add(new JButton("Test"), BorderLayout.PAGE_END);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.PAGE_AXIS));

        selectionRender = new SingleTextRenderer(new SpecialText(' ', Color.WHITE, Color.WHITE));
        selectionLabel = new JLabel(selectionRender);
        selectionLabel.setMaximumSize(new Dimension(20, 20));
        selectionLabel.setAlignmentX(CENTER_ALIGNMENT);

        bottomPanel.add(Box.createRigidArea(new Dimension(1, 5)));
        bottomPanel.add(selectionLabel);
        bottomPanel.add(Box.createRigidArea(new Dimension(1, 5)));

        JButton editButton = new JButton("Edit");
        editButton.setAlignmentX(CENTER_ALIGNMENT);
        editButton.setMargin(new Insets(4, 11, 4, 13));
        editButton.addActionListener(e -> createNewButton(selectedTextButton));

        JButton removeButton = new JButton("Remove");
        removeButton.setAlignmentX(CENTER_ALIGNMENT);
        removeButton.setMargin(new Insets(4, 2, 4, 2));
        removeButton.setActionCommand("Remove Btn");
        removeButton.addActionListener(this);

        bottomPanel.add(editButton);
        bottomPanel.add(removeButton);

        add(bottomPanel, BorderLayout.PAGE_END);

        validate();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("Add New Btn")) { //Adds new button
            createNewButton(new JButton(new SingleTextRenderer(new SpecialText(' '))));
        } else if (e.getActionCommand().equals("Remove Btn")){
            if (selectedTextButton != null){
                textBtnPanel.remove(selectedTextButton);
                textBtnPanel.validate();
                textBtnPanel.repaint();
            }
        } else {
            buildSpecTxtFromButtonClick(e.getActionCommand());
            selectedTextButton = (JButton)e.getSource();
        }
    }
    
    private void createNewButton(JButton btn){
        btn.setFont(new Font("Monospaced", Font.PLAIN, 12));
        btn.setHorizontalTextPosition(SwingConstants.LEFT);
        btn.setMargin(new Insets(2, 2, 2, 2));
        btn.setOpaque(true);
        btn.addActionListener(this);
        textBtnPanel.add(btn, 0);
        textBtnPanel.validate();
        validate();
        EditorSpecialTextMaker textMaker;
        if (selectedTextButton == null || selectedTextButton.getIcon() == null)
            textMaker = new EditorSpecialTextMaker(textBtnPanel, btn, new SpecialText(' ', Color.WHITE, Color.BLACK));
        else
            textMaker = new EditorSpecialTextMaker(textBtnPanel, btn, ((SingleTextRenderer)selectedTextButton.getIcon()).specText);
        textMaker.setVisible(true);
    }
    
    private void buildSpecTxtFromButtonClick(String command){
        System.out.println(command);
        System.out.println(command.substring(11,12));
        int[] textData = new int[9];
        int startIndex = 14;
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
        selectedSpecialText = new SpecialText(command.substring(11,12).charAt(0), fg, bg);
        selectionRender.specText = selectedSpecialText;
        selectionLabel.repaint();
    }

}

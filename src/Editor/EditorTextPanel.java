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

    private SpecialText selectedSpecialText = new SpecialText(' ');
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

        bottomPanel.add(selectionLabel);

        JButton editButton = new JButton("Edit");
        editButton.setAlignmentX(CENTER_ALIGNMENT);
        editButton.setMargin(new Insets(4, 11, 4, 13));

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

    private int arbitraryNumber = 0;

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("Add New Btn")) { //Adds new button
            SpecialText text = new SpecialText(String.valueOf(arbitraryNumber).charAt(0), Color.WHITE, new Color(50 + arbitraryNumber * 20, 50 + arbitraryNumber * 20, 100));

            JButton btn = new JButton(new SingleTextRenderer(text));
            btn.setFont(new Font("Monospaced", Font.PLAIN, 12));
            btn.setHorizontalTextPosition(SwingConstants.LEFT);
            btn.setMargin(new Insets(2, 2, 2, 2));
            btn.setOpaque(true);
            btn.addActionListener(this);
            //btn.setActionCommand(text.getStr() + text.getFgColor().toString() + text.getBkgColor().toString());
            Color fg = text.getFgColor();
            Color bg = text.getBkgColor();
            btn.setActionCommand(String.format("Selection: %1$c *[%2$d,%3$d,%4$d,%5$d] #[%6$d,%7$d,%8$d,%9$d]", text.getCharacter(), fg.getRed(), fg.getGreen(), fg.getBlue(), fg.getAlpha(), bg.getRed(), bg.getGreen(), bg.getBlue(), bg.getAlpha()));

            textBtnPanel.add(btn, 0);
            System.out.println(textBtnPanel.getComponents().length);
            textBtnPanel.validate();
            validate();
            arbitraryNumber++;
            if (arbitraryNumber > 9) arbitraryNumber = 0;
        } else if (e.getActionCommand().equals("Remove Btn")){
            if (selectedTextButton != null){
                textBtnPanel.remove(selectedTextButton);
                textBtnPanel.validate();
            }
        } else {
            System.out.println(e.getActionCommand());
            System.out.println(e.getActionCommand().substring(11,12));
            int[] textData = new int[9];
            int startIndex = e.getActionCommand().indexOf("*")+2;
            int endingIndex;
            for (int ii= 0; ii < 4; ii++){
                endingIndex = Math.min(e.getActionCommand().indexOf(",", startIndex),e.getActionCommand().indexOf("]", startIndex));
                String str = e.getActionCommand().substring(startIndex, endingIndex);
                System.out.println(str);
                textData[ii] = Integer.valueOf(str);
                startIndex = endingIndex + 1;
            }
            startIndex = e.getActionCommand().indexOf("#")+2;
            for (int ii= 0; ii < 4; ii++){
                endingIndex = e.getActionCommand().indexOf(",", startIndex);
                if (endingIndex < 0) endingIndex = e.getActionCommand().indexOf("]", startIndex);
                String str = e.getActionCommand().substring(startIndex, endingIndex);
                System.out.println(str);
                textData[ii+4] = Integer.valueOf(str);
                startIndex = endingIndex + 1;
            }
            System.out.println("----\n");
            Color fg = new Color(textData[0], textData[1], textData[2], textData[3]);
            Color bg = new Color(textData[4], textData[5], textData[6], textData[7]);
            selectedSpecialText = new SpecialText(e.getActionCommand().substring(11,12).charAt(0), fg, bg);
            selectionRender.specText = selectedSpecialText;
            selectionLabel.repaint();
            selectedTextButton = (JButton)e.getSource();
        }
    }

    class SingleTextRenderer implements Icon {

        SpecialText specText;

        SingleTextRenderer (SpecialText text){
            specText = text;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.setColor(specText.getBkgColor());
            g.fillRect(x,y,getIconWidth(),getIconHeight());
            g.setColor(specText.getFgColor());
            g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 17));
            g.drawString(specText.getStr(), x+(getIconWidth()/2)-5, y+(getIconHeight()-4));
        }

        @Override
        public int getIconWidth() {
            return 19;
        }

        @Override
        public int getIconHeight() {
            return 19;
        }
    }

}

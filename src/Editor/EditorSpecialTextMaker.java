package Editor;

import Engine.SpecialText;

import javax.swing.*;
import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.awt.event.*;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Jared on 2/22/2018.
 */
public class EditorSpecialTextMaker extends JFrame implements ActionListener {

    private JButton openedButton; //The button this SpecialTextMaker is editing
    private Container buttonContainer;

    private JTextField charField;

    private JButton fgButton;
    private JButton bgButton;
    private boolean settingForeground = true;

    private ColorPicker colorPicker;
    private float[] fgHSB = new float[3];
    private float[] bgHSB = new float[3];

    EditorSpecialTextMaker(Container c, JButton button, SpecialText startingText){
        openedButton = button;
        buttonContainer = c;

        setTitle("SpecialText Creator");

        setMinimumSize(new Dimension(410, 285));

        //Buttons and text box on left
        charField = new JTextField(3);
        charField.setMaximumSize(new Dimension(50, 30));
        charField.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
        charField.setHorizontalAlignment(SwingConstants.CENTER);
        charField.setAlignmentX(Component.CENTER_ALIGNMENT);
        charField.addActionListener(this);
        charField.setForeground(startingText.getFgColor());
        charField.setBackground(startingText.getBkgColor());
        charField.setText(startingText.getStr());

        Color fg = startingText.getFgColor();
        fgHSB = Color.RGBtoHSB(fg.getRed(), fg.getGreen(), fg.getBlue(), new float[3]);
        Color bg = startingText.getBkgColor();
        bgHSB = Color.RGBtoHSB(bg.getRed(), bg.getGreen(), bg.getBlue(), new float[3]);

        fgButton = new JButton("Fg");
        fgButton.setMaximumSize(new Dimension(70, 30));
        fgButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        fgButton.setEnabled(false);
        fgButton.addActionListener(this);
        fgButton.setActionCommand("fg");

        bgButton = new JButton("Bg");
        bgButton.setMaximumSize(new Dimension(70, 30));
        bgButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        bgButton.setEnabled(true);
        bgButton.addActionListener(this);
        bgButton.setActionCommand("bg");

        JButton finishButton = new JButton("Finish");
        finishButton.setMaximumSize(new Dimension(80, 30));
        finishButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        finishButton.addActionListener(e -> finish());
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setMaximumSize(new Dimension(80, 30));
        cancelButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        cancelButton.addActionListener(e -> cancel());

        JPanel selectorPanel = new JPanel();
        selectorPanel.setPreferredSize(new Dimension(150, 200));
        selectorPanel.setLayout(new BoxLayout(selectorPanel, BoxLayout.PAGE_AXIS));

        selectorPanel.add(Box.createRigidArea(new Dimension(1, 20)));
        selectorPanel.add(charField);

        //JPanel colorSwitchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        //colorSwitchPanel.add(fgButton, BorderLayout.CENTER);
        //colorSwitchPanel.add(bgButton, BorderLayout.CENTER);
        //selectorPanel.add(colorSwitchPanel, BorderLayout.CENTER);

        selectorPanel.add(Box.createRigidArea(new Dimension(1, 20)));
        selectorPanel.add(fgButton);
        selectorPanel.add(bgButton);

        selectorPanel.add(Box.createRigidArea(new Dimension(1, 20)));
        selectorPanel.add(finishButton);
        selectorPanel.add(cancelButton);

        add(selectorPanel, BorderLayout.LINE_START);

        selectorPanel.setBorder(BorderFactory.createEtchedBorder());

        //Right side panel with color and opacity ui
        JPanel colorPickerPanel = new JPanel();
        colorPicker = new ColorPicker();
        colorPicker.setPreferredSize(new Dimension(215, 200));
        colorPickerPanel.addMouseListener(colorPicker);
        colorPickerPanel.addMouseMotionListener(colorPicker);
        colorPickerPanel.add(colorPicker, BorderLayout.CENTER);

        add(colorPickerPanel, BorderLayout.CENTER);

        validate();

        colorPicker.setSize(new Dimension(215, 200));
        System.out.println("Initial HSB Value: \n" + fgHSB[0] + "\n" + fgHSB[1] + "\n" + fgHSB[2]);
        System.out.println("Color Picker sizing: " + colorPicker.getWidth() + " x " + colorPicker.getHeight());
        colorPicker.setColorData(fgHSB);

        addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {}
            @Override
            public void windowClosing(WindowEvent e) {
                cancel();
            }
            @Override
            public void windowClosed(WindowEvent e) {}
            @Override
            public void windowIconified(WindowEvent e) {}
            @Override
            public void windowDeiconified(WindowEvent e) {}
            @Override
            public void windowActivated(WindowEvent e) {}
            @Override
            public void windowDeactivated(WindowEvent e) {}
        });

        java.util.Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateElements();
            }
        }, 10, 75);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("fg")){
            bgHSB = colorPicker.getColorData();
            colorPicker.setColorData(fgHSB);
            fgButton.setEnabled(false);
            bgButton.setEnabled(true);
            settingForeground = true;
        } else if (e.getActionCommand().equals("bg")){
            fgHSB = colorPicker.getColorData();
            colorPicker.setColorData(bgHSB);
            fgButton.setEnabled(true);
            bgButton.setEnabled(false);
            settingForeground = false;
        }
    }



    private void updateElements() {
        String charText = charField.getText();
        if (charText.length() > 1) {
            String endText = charText.substring(charText.length()-1, charText.length());
            charField.setText(endText);
            charField.setCaretPosition(1);
        }
        colorPicker.repaint();
    }

    private void finish(){
        SingleTextRenderer icon = (SingleTextRenderer)openedButton.getIcon();
        if (charField.getText().length() > 0)
            icon.specText = new SpecialText(charField.getText().charAt(0), charField.getForeground(), charField.getBackground());
        else
            icon.specText = new SpecialText(' ', charField.getForeground(), charField.getBackground());
        openedButton.setActionCommand(icon.specText.toString());
        openedButton.doClick();
        buttonContainer.repaint();
        dispose();
    }

    private void cancel(){
        buttonContainer.remove(openedButton);
        buttonContainer.validate();
        buttonContainer.repaint();
        dispose();
    }

    class ColorPicker extends JComponent implements MouseInputListener {

        int mousePointX = 0;
        int mousePointY = 0;
        
        int satBriPointX = 0;
        int satBriPointY = 0;

        float[] colorData = new float[3];

        void setColorData(float[] data) {
            colorData = data;
            mousePointX = (int)(colorData[1] * (getBoxWidth())) + 1;
            mousePointY = (int)(colorData[2] * (getHeight()-1)) + 1;
        }

        float[] getColorData() { return colorData; }

        private int getBoxWidth() { return getWidth() - 15; }

        ColorPicker() { colorData = new float[]{0, 0, 0}; }

        @Override
        public void paintComponent(Graphics g) {

            int boxWidth = getBoxWidth();

            for (int y = 1; y < getHeight()-1; y++){
                for (int x = 1; x < boxWidth; x++){
                    //Color col = clipColor(new Color(x * (255 / getWidth()), y * (255 / getHeight()), 50));
                    Color col = Color.getHSBColor(colorData[0], (float)(x-1)/(boxWidth), (float)(y-1)/(getHeight()-1));
                    g.setColor(col);
                    g.fillRect(x, y, 1, 1);
                }
                if (Math.abs( ((float)y)/getHeight() - colorData[0]) < 0.005)
                    g.setColor(Color.WHITE);
                else
                    g.setColor(Color.getHSBColor(((float)y)/getHeight(), 0.9f, 0.9f));
                g.drawLine(boxWidth + 2, y, getWidth(), y);
            }

            g.setColor(Color.black);
            g.drawRect(0,0,boxWidth,getHeight()-1);
            g.drawRect(boxWidth + 2,0,getWidth()-boxWidth-3,getHeight()-1);

            if (mousePointX < boxWidth + 2) {
                satBriPointX = mousePointX;
                satBriPointY = mousePointY;
            }

            g.setColor(Color.WHITE);
            g.drawLine(mousePointX-1, mousePointY, mousePointX-2, mousePointY);
            g.drawLine(mousePointX+1, mousePointY, mousePointX+2, mousePointY);
            g.drawLine(mousePointX, mousePointY+1, mousePointX, mousePointY+2);
            g.drawLine(mousePointX, mousePointY-1, mousePointX, mousePointY-2);
        }

        private void generateColor(){
            Color col = Color.getHSBColor(colorData[0], colorData[1], colorData[2]);
            if (settingForeground) {
                charField.setForeground(col);
            }
            else {
                charField.setBackground(col);
            }
        }

        private void onMouseInput(MouseEvent e){
            mousePointX = e.getX() - getX();
            mousePointY = e.getY() - getY();
            if (mousePointX >= getWidth() - 13){ //Selecting hue
                colorData[0] = (float)mousePointY / getHeight();
            } else {
                colorData[1] = (float)(mousePointX-1)/(getWidth()-15);
                colorData[2] = (float)(mousePointY-1)/(getHeight()-1);
            }
            generateColor();
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            onMouseInput(e);
        }

        @Override
        public void mouseMoved(MouseEvent e) {}

        @Override
        public void mouseClicked(MouseEvent e) {
            onMouseInput(e);
        }

        @Override
        public void mousePressed(MouseEvent e) {

        }

        @Override
        public void mouseReleased(MouseEvent e) {

        }

        @Override
        public void mouseEntered(MouseEvent e) {

        }

        @Override
        public void mouseExited(MouseEvent e) {

        }
    }
}

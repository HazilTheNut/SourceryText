package Editor;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Jared on 2/22/2018.
 */
public class EditorSpecialTextMaker extends JFrame implements ActionListener {

    private JButton openedButton; //The button this SpecialTextMaker is editing
    private JTextField charField;

    private JButton fgButton;
    private JButton bgButton;
    private boolean settingForeground = true;

    private ColorPicker colorPicker;
    private JSlider bgOpacitySlider;
    private float fgHue = 0;
    private float bgHue = 0;

    private JButton finishButton;
    private JButton cancelButton;

    public EditorSpecialTextMaker(){

        setMinimumSize(new Dimension(410, 310));

        //Buttons and text box on left
        charField = new JTextField(3);
        charField.setMaximumSize(new Dimension(50, 30));
        charField.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
        charField.setHorizontalAlignment(SwingConstants.CENTER);
        charField.setAlignmentX(Component.CENTER_ALIGNMENT);
        charField.addActionListener(this);

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

        finishButton = new JButton("Finish");
        finishButton.setMaximumSize(new Dimension(80, 30));
        finishButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        cancelButton = new JButton("Cancel");
        cancelButton.setMaximumSize(new Dimension(80, 30));
        cancelButton.setAlignmentX(Component.CENTER_ALIGNMENT);

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
        colorPickerPanel.addMouseMotionListener(colorPicker);
        colorPickerPanel.add(colorPicker, BorderLayout.CENTER);

        JPanel opacityPanel = new JPanel(); //Sub-panel for selecting opacity

        JTextField sliderValue = new JTextField("255", 3);
        sliderValue.setBorder(BorderFactory.createEmptyBorder());

        bgOpacitySlider = new JSlider(SwingConstants.HORIZONTAL, 0, 255, 255);
        bgOpacitySlider.setMinorTickSpacing(5);
        bgOpacitySlider.setMajorTickSpacing(50);
        bgOpacitySlider.addChangeListener(e -> sliderValue.setText(String.valueOf(bgOpacitySlider.getValue())));

        opacityPanel.setBorder(BorderFactory.createTitledBorder("Background Opacity"));

        opacityPanel.add(bgOpacitySlider, BorderLayout.CENTER);
        opacityPanel.add(sliderValue, BorderLayout.LINE_END);

        colorPickerPanel.add(opacityPanel, BorderLayout.PAGE_END);

        add(colorPickerPanel, BorderLayout.CENTER);

        validate();

        java.util.Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateElements();
            }
        }, 10, 100);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("fg")){
            bgHue = colorPicker.getHue();
            colorPicker.setHue(fgHue);
            fgButton.setEnabled(false);
            bgButton.setEnabled(true);
            settingForeground = true;
        } else if (e.getActionCommand().equals("bg")){
            fgHue = colorPicker.getHue();
            colorPicker.setHue(bgHue);
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

    class ColorPicker extends JComponent implements MouseMotionListener {

        float hue = 0f;
        float sat = 0f;
        float bri = 0f;
        int mousePointX = 0;
        int mousePointY = 0;

        public float getHue() { return hue; }

        public void setHue(float hue) { this.hue = hue; }

        @Override
        public void paintComponent(Graphics g) {

            int boxWidth = getWidth() - 15;

            for (int y = 1; y < getHeight()-1; y++){
                for (int x = 1; x < boxWidth; x++){
                    //Color col = clipColor(new Color(x * (255 / getWidth()), y * (255 / getHeight()), 50));
                    Color col = Color.getHSBColor(hue, (float)(x-1)/(boxWidth), (float)(y-1)/(getHeight()-1));
                    g.setColor(col);
                    g.fillRect(x, y, 1, 1);
                }
                if (Math.abs( ((float)y)/getHeight() - hue) < 0.01)
                    g.setColor(Color.WHITE);
                else
                    g.setColor(Color.getHSBColor(((float)y)/getHeight(), 0.9f, 0.9f));
                g.drawLine(boxWidth + 2, y, getWidth(), y);
            }

            g.setColor(Color.black);
            g.drawRect(0,0,boxWidth,getHeight()-1);
            g.drawRect(boxWidth + 2,0,getWidth()-boxWidth-3,getHeight()-1);

            g.setColor(Color.WHITE);
            g.drawRect(mousePointX, mousePointY, 1, 1);
        }

        private void generateColor(){
            Color col = Color.getHSBColor(hue, sat, bri);
            if (settingForeground) {
                charField.setForeground(col);
            }
            else {
                charField.setBackground(col);
            }
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            mousePointX = e.getX() - getX();
            mousePointY = e.getY() - getY();
            if (mousePointX >= getWidth() - 13){ //Selecting hue
                hue = (float)mousePointY / getHeight();
            } else {
                sat = (float)(mousePointX-1)/(getWidth()-15);
                bri = (float)(mousePointY-1)/(getHeight()-1);
            }
            generateColor();
        }

        @Override
        public void mouseMoved(MouseEvent e) {}
    }
}

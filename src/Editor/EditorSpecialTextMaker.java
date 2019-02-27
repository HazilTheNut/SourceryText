package Editor;

import Engine.SpecialText;

import javax.swing.*;
import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.TimerTask;

/**
 * Created by Jared on 2/22/2018.
 */
public class EditorSpecialTextMaker extends JFrame implements ActionListener {

    private JButton openedButton; //The button this SpecialTextMaker is editing
    private ArrayList<JButton> btnManifest;
    private Container buttonContainer;

    private JTextField charField;

    private JButton fgButton;
    private JButton bgButton;
    private boolean settingForeground = true;

    private SingleTextRenderer preview;

    private ColorPicker colorPicker;
    private float[] fgHSB;
    private float[] bgHSB;

    EditorSpecialTextMaker(Container c, JButton button, SpecialText startingText, ArrayList<JButton> manifest){
        openedButton = button;
        buttonContainer = c;
        btnManifest = manifest;

        setTitle("SpecialText Creator");

        setMinimumSize(new Dimension(600, 500));

        preview = new SingleTextRenderer(startingText);
        JLabel previewLabel = new JLabel(preview);
        previewLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

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

        //Now for the Fg and Bg buttons
        Color fg = startingText.getFgColor();
        fgHSB = Color.RGBtoHSB(fg.getRed(), fg.getGreen(), fg.getBlue(), new float[3]);
        Color bg = startingText.getBkgColor();
        bgHSB = Color.RGBtoHSB(bg.getRed(), bg.getGreen(), bg.getBlue(), new float[3]);
        //The 'foreground' button
        fgButton = new JButton("Fg");
        fgButton.setMaximumSize(new Dimension(70, 30));
        fgButton.setMargin(new Insets(5, 10, 5, 10));
        fgButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        fgButton.setEnabled(false);
        fgButton.addActionListener(this);
        fgButton.setActionCommand("fg");
        //The 'background' button
        bgButton = new JButton("Bg");
        bgButton.setMaximumSize(new Dimension(70, 30));
        bgButton.setMargin(new Insets(5, 10, 5, 10));
        bgButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        bgButton.setEnabled(true);
        bgButton.addActionListener(this);
        bgButton.setActionCommand("bg");
        //The finish button
        JButton finishButton = new JButton("Finish");
        finishButton.setMaximumSize(new Dimension(80, 30));
        finishButton.setMargin(new Insets(5, 10, 5, 10));
        finishButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        finishButton.addActionListener(e -> finish());
        //The cancel button
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setMaximumSize(new Dimension(80, 30));
        cancelButton.setMargin(new Insets(5, 10, 5, 10));
        cancelButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        cancelButton.addActionListener(e -> cancel());
        //Create JPanel for the left-side stuff
        JPanel selectorPanel = new JPanel();
        selectorPanel.setPreferredSize(new Dimension(120, 200));
        selectorPanel.setLayout(new BoxLayout(selectorPanel, BoxLayout.PAGE_AXIS));

        selectorPanel.add(Box.createRigidArea(new Dimension(1, 15)));
        selectorPanel.add(previewLabel);
        selectorPanel.add(Box.createRigidArea(new Dimension(1, 5)));
        selectorPanel.add(charField);

        selectorPanel.add(Box.createRigidArea(new Dimension(1, 20)));
        selectorPanel.add(fgButton);
        selectorPanel.add(bgButton);
        selectorPanel.add(Box.createRigidArea(new Dimension(1, 10)));
        selectorPanel.add(generateQuickColorPanel());

        selectorPanel.add(Box.createVerticalGlue());
        selectorPanel.add(Box.createRigidArea(new Dimension(1, 5)));
        selectorPanel.add(cancelButton);
        selectorPanel.add(Box.createRigidArea(new Dimension(1, 5)));
        selectorPanel.add(finishButton);
        selectorPanel.add(Box.createRigidArea(new Dimension(1, 20)));

        add(selectorPanel, BorderLayout.LINE_START);

        selectorPanel.setBorder(BorderFactory.createEtchedBorder());

        //Right side panel with color ui
        JPanel colorPickerPanel = new JPanel();
        GridLayout gridLayout = new GridLayout(1, 1);
        gridLayout.setHgap(5);
        gridLayout.setVgap(5);
        colorPickerPanel.setLayout(gridLayout);
        colorPickerPanel.setBorder(BorderFactory.createLoweredSoftBevelBorder());
        //Add in the color picker
        colorPicker = new ColorPicker();
        colorPicker.setPreferredSize(new Dimension(315, 300));
        colorPickerPanel.addMouseListener(colorPicker);
        colorPickerPanel.addMouseMotionListener(colorPicker);
        getContentPane().addComponentListener(colorPicker);

        colorPickerPanel.add(colorPicker);

        add(colorPickerPanel, BorderLayout.CENTER);

        validate();

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

        java.util.Timer timer = new java.util.Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateElements();
            }
        }, 10, 75);
    }

    private JPanel generateQuickColorPanel(){
        JPanel masterPanel = new JPanel();
        masterPanel.setLayout(new GridLayout(3, 3, 1, 1));
        masterPanel.add(createQuickColorButton(Color.WHITE));
        masterPanel.add(createQuickColorButton(Color.BLACK));
        masterPanel.add(createQuickColorButton(Color.GRAY));
        masterPanel.add(createQuickColorButton(new Color(255, 64, 64)));
        masterPanel.add(createQuickColorButton(new Color(66, 255, 66)));
        masterPanel.add(createQuickColorButton(new Color(66, 66, 255)));
        masterPanel.add(createQuickColorButton(new Color(169, 100, 45)));
        masterPanel.add(createQuickColorButton(new Color(219, 200, 162)));
        masterPanel.add(createQuickColorButton(new Color(61, 194, 219)));
        masterPanel.setMaximumSize(new Dimension(80, 80));
        masterPanel.validate();
        return masterPanel;
    }

    private void updatePreview(){
        preview.specText = new SpecialText(charField.getText().charAt(0), charField.getForeground(), charField.getBackground());
        repaint();
    }

    private JButton createQuickColorButton(Color color){
        JButton btn = new JButton(new SingleTextRenderer(new SpecialText(' ', Color.WHITE, color)));
        btn.addActionListener(e -> {
            float[] hsb = new float[3];
            hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsb);
            colorPicker.setColorData(hsb);
            colorPicker.generateColor();
        });
        return btn;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("fg")){ //Pushed 'Fg' button
            bgHSB = colorPicker.getColorData();
            colorPicker.setColorData(fgHSB);
            fgButton.setEnabled(false);
            bgButton.setEnabled(true);
            settingForeground = true;
        } else if (e.getActionCommand().equals("bg")){ //Pushed 'Bg' button
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
            updatePreview();
        }
    }

    private void finish(){
        SingleTextRenderer icon = (SingleTextRenderer)openedButton.getIcon();
        if (charField.getText().length() > 0)
            icon.specText = new SpecialText(charField.getText().charAt(0), charField.getForeground(), charField.getBackground());
        else
            icon.specText = new SpecialText(' ', charField.getForeground(), charField.getBackground());
        //openedButton.setActionCommand(icon.specText.toString());
        openedButton.doClick();
        buttonContainer.repaint();
        dispose();
    }

    private void cancel(){
        SingleTextRenderer icon = (SingleTextRenderer)openedButton.getIcon();
        if (icon.specText == null) {
            buttonContainer.remove(openedButton);
            btnManifest.remove(openedButton);
            buttonContainer.validate();
            buttonContainer.repaint();
        } else {
            openedButton.doClick();
        }
        dispose();
    }

    class ColorPicker extends JComponent implements MouseInputListener, ComponentListener {

        /**
         * ColorPicker:
         *
         * A custom JComponent that generates colors, using an HSB-based user interface.
         *
         * NOMENCLATURE:
         *
         * Main Box: The large square that allows for modifying Saturation and Brightness
         * Hue Slider: The slider on the right that modifies Hue.
         */

        float[] colorData;
        Color[][] colorMatrix;

        void setColorData(float[] data) {
            colorData = data;
            recalculateColorMatrix();
            repaint();
        }

        float[] getColorData() { return colorData; }

        //Gets the width of the main box of the color picker, without the hue slider on the side.
        private int getBoxWidth() { return getWidth() - 16; }

        private int BOX_SLIDER_MARGIN = 5;

        ColorPicker() {
            colorData = new float[]{0, 0, 0};
        }

        private void recalculateColorMatrix(){
            if (getBoxWidth() <= 0 || getHeight() <= 0) return;
            colorMatrix = new Color[getBoxWidth() + 1][getHeight() + 1];
            for (int y = 1; y < getHeight()-1; y++){
                for (int x = 1; x < getBoxWidth(); x++) {
                    Color col = Color.getHSBColor(colorData[0], (float) (x - 1) / (getBoxWidth()), (float) (y - 1) / (getHeight() - 1));
                    colorMatrix[x][y] = col;
                }
            }
        }

        private long repaintTimestamp = 0;

        @Override
        public void paintComponent(Graphics g) {
            repaintTimestamp = System.currentTimeMillis();

            int boxWidth = getBoxWidth();

            //Draw the main box and hue slider
            for (int y = 1; y < getHeight()-1; y++){
                for (int x = 1; x < boxWidth; x++) { //Draw stuff in the main box
                    try {
                        g.setColor(colorMatrix[x][y]);
                    } catch (NullPointerException | ArrayIndexOutOfBoundsException e){
                        recalculateColorMatrix();
                    }
                    g.fillRect(x, y, 1, 1);
                }
                //Then draw the hue slider, using the same y value for compactness.
                g.setColor(Color.getHSBColor(((float)y)/getHeight(), 0.9f, 0.9f));
                g.drawLine(boxWidth + BOX_SLIDER_MARGIN, y, getWidth(), y);
            }

            //Draw white line on the hue slider
            int lineY = (int)(getHeight() * colorData[0]);
            g.setColor(Color.WHITE);
            g.fillRect(boxWidth, lineY - 2, getBoxWidth() + BOX_SLIDER_MARGIN, 4);

            //Draw black line boundary
            g.setColor(Color.black);
            g.drawRect(0 ,0, boxWidth, getHeight()-1);
            g.drawRect(boxWidth + BOX_SLIDER_MARGIN ,0 , getWidth() - boxWidth - BOX_SLIDER_MARGIN - 1, getHeight() - 1);

            //if (mousePointX < getBoxWidth() + 2) { //Don't move Sat-Bri point if hue is changing.
            int satBriPointX = (int)(getBoxWidth() * colorData[1]);
            int satBriPointY = (int)(getHeight()   * colorData[2]);
            //}

            //Draw the little cross-hair on the main box
            g.setColor(Color.WHITE);
            g.drawLine(satBriPointX-1, satBriPointY, satBriPointX-2, satBriPointY);
            g.drawLine(satBriPointX+1, satBriPointY, satBriPointX+2, satBriPointY);
            g.drawLine(satBriPointX, satBriPointY+1, satBriPointX, satBriPointY+2);
            g.drawLine(satBriPointX, satBriPointY-1, satBriPointX, satBriPointY-2);
        }

        private void generateColor(){
            Color col = Color.getHSBColor(colorData[0], colorData[1], colorData[2]);
            if (settingForeground) {
                charField.setForeground(col);
            } else {
                charField.setBackground(col);
            }
            updatePreview();
        }

        private void onMouseInput(MouseEvent e){
            int mousePointX = e.getX() - getX();
            int mousePointY = e.getY() - getY() - 1;
            mousePointX = Math.max(0, Math.min(mousePointX, getWidth()));
            mousePointY = Math.max(0, Math.min(mousePointY, getHeight()));
            if (mousePointX >= getBoxWidth() + BOX_SLIDER_MARGIN){ //Selecting hue
                colorData[0] = (float)mousePointY / getHeight();
                recalculateColorMatrix();
            } else if (mousePointX <= getBoxWidth() + 1){
                colorData[1] = ((float)mousePointX)/(getBoxWidth());
                colorData[2] = ((float)mousePointY)/(getHeight());
            }
            if (System.currentTimeMillis() - repaintTimestamp > 25)
                repaint();
            //System.out.printf("Pt: %1$d, %2$d (%3$dx%4$d) sat: %5$.1f%% bri: %6$.1f%%\n", mousePointX, mousePointY, getBoxWidth(), getHeight(), 100 * colorData[1], 100 * colorData[2]);
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

        }

        @Override
        public void mousePressed(MouseEvent e) {
            onMouseInput(e);
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

        @Override
        public void componentResized(ComponentEvent e) {
            recalculateColorMatrix();
            repaint();
        }

        @Override
        public void componentMoved(ComponentEvent e) {

        }

        @Override
        public void componentShown(ComponentEvent e) {

        }

        @Override
        public void componentHidden(ComponentEvent e) {

        }
    }
}

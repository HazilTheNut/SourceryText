package Editor;

import Data.LevelData;
import Engine.SpecialText;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Jared on 3/25/2018.
 */
public class EditorFindAndReplace extends JFrame {

    /**
     * EditorFindAndReplace:
     *
     * The Editor Window that allows the user to find and replace SpecialText in the Level backdrop.
     * Also has a randomize feature, so randomized, textured environments aren't too hard to generate.
     */

    private SpecialText findText;
    private SpecialText replaceText;

    private JLabel findTextLabel;
    private JLabel replaceTextLabel;

    private JSpinner replaceChanceSpinner;

    private JButton previousFindButton;
    private JButton previousReplaceButton;
    
    public EditorFindAndReplace(EditorTextPanel editorTextPanel, LevelData ldata, UndoManager undoManager){

        setMinimumSize(new Dimension(400, 300));
        setTitle("Find and Replace (Level Backdrop)");

        JPanel findButtonPanel = new JPanel(); //The JPanel containing the contents of the Text Panel designated for selecting the SpecialText to look for.
        findButtonPanel.setLayout(new ModifiedFlowLayout(FlowLayout.LEFT));
        JScrollPane findButtonScrollPane = new JScrollPane(findButtonPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        findButtonScrollPane.setMinimumSize(new Dimension(180, 200));
        findButtonScrollPane.setBorder(BorderFactory.createTitledBorder("Find"));
        
        JPanel replaceButtonPanel = new JPanel(); //The corresponding JPanel for selecting the SpecialText to replace with
        replaceButtonPanel.setLayout(new ModifiedFlowLayout(FlowLayout.LEFT));
        JScrollPane replaceButtonScrollPane = new JScrollPane(replaceButtonPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        replaceButtonScrollPane.setPreferredSize(new Dimension(180, 200));
        replaceButtonScrollPane.setBorder(BorderFactory.createTitledBorder("Replace"));

        System.out.println("\n[EditorFindAndReplace] Start build of scroll pane contents");
        
        for (JButton btn : editorTextPanel.getButtonManifest()){
            if (btn.getIcon() instanceof SingleTextRenderer){
                SingleTextRenderer icon = (SingleTextRenderer) btn.getIcon();

                JButton findBtn = new JButton(icon);
                findBtn.setMargin(new Insets(2, 2, 2, 2));
                findBtn.addActionListener(e -> {
                    if (btn.getActionCommand().equals("nulltext"))
                        findText = null;
                    else
                        findText = ((SingleTextRenderer)findBtn.getIcon()).specText;
                    if (previousFindButton != null) {
                        previousFindButton.setEnabled(true);
                    }
                    findBtn.setEnabled(false);
                    previousFindButton = findBtn;
                    findTextLabel.setIcon(new SingleTextRenderer(findText));
                    findTextLabel.repaint();
                });
                findButtonPanel.add(findBtn);

                JButton replaceBtn = new JButton(icon);
                replaceBtn.setMargin(new Insets(2, 2, 2, 2));
                replaceBtn.addActionListener(e -> {
                    if (btn.getActionCommand().equals("nulltext"))
                        replaceText = null;
                    else
                        replaceText = ((SingleTextRenderer)replaceBtn.getIcon()).specText;
                    if (previousReplaceButton != null) {
                        previousReplaceButton.setEnabled(true);
                    }
                    replaceBtn.setEnabled(false);
                    previousReplaceButton = replaceBtn;
                    replaceTextLabel.setIcon(new SingleTextRenderer(replaceText));
                    replaceTextLabel.repaint();
                });
                replaceButtonPanel.add(replaceBtn);
            }
        }

        JPanel topPanel = new JPanel(new GridLayout(1, 2));

        topPanel.add(findButtonScrollPane,  BorderLayout.LINE_START);
        topPanel.add(replaceButtonScrollPane, BorderLayout.LINE_END);

        add(topPanel, BorderLayout.CENTER);

        //Now for the bottom panel
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new FlowLayout());

        //Create the 'preview' panel that summarizes the action you are about to take
        JPanel replacePreviewPanel = new JPanel();
        replacePreviewPanel.setLayout(new BoxLayout(replacePreviewPanel, BoxLayout.LINE_AXIS));

        //Create the icon for what SpecialText is being looked for
        SingleTextRenderer findTextIcon = new SingleTextRenderer(findText);
        findTextLabel = new JLabel(findTextIcon);
        replacePreviewPanel.add(findTextLabel);

        //Create the little arrow
        replacePreviewPanel.add(new JLabel(" --> "));

        //Create the icon for what SpecialText is being replaced with
        SingleTextRenderer replaceTextIcon = new SingleTextRenderer(replaceText);
        replaceTextLabel = new JLabel(replaceTextIcon);

        replacePreviewPanel.setBorder(BorderFactory.createEtchedBorder());
        replacePreviewPanel.add(replaceTextLabel);

        bottomPanel.setBorder(BorderFactory.createEtchedBorder());
        bottomPanel.add(replacePreviewPanel);
        //The word 'random' in this context in fact does not mean 'purposeless and silly'
        JPanel randomPanel = new JPanel();
        randomPanel.setBorder(BorderFactory.createEtchedBorder());
        randomPanel.setLayout(new BoxLayout(randomPanel, BoxLayout.LINE_AXIS));

        replaceChanceSpinner = new JSpinner(new SpinnerNumberModel(100, 0, 100, 1));
        randomPanel.add(new JLabel("Chance (%): "));
        randomPanel.add(replaceChanceSpinner);

        bottomPanel.add(randomPanel);

        JButton replaceButton = new JButton("Replace");
        replaceButton.addActionListener(e -> {
            ldata.getBackdrop().findAndReplace(findText, replaceText, ((SpinnerNumberModel)replaceChanceSpinner.getModel()).getNumber().intValue());
            undoManager.recordLevelData();
            dispose();
        });
        bottomPanel.add(replaceButton);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());
        bottomPanel.add(cancelButton);

        add(bottomPanel, BorderLayout.PAGE_END);

        validate();

        setVisible(true);
    }



    //Copied from StackOverflow, url: https://stackoverflow.com/questions/3679886/how-can-i-let-jtoolbars-wrap-to-the-next-line-flowlayout-without-them-being-hi
    private class ModifiedFlowLayout extends FlowLayout {
        public ModifiedFlowLayout() {
            super();
        }

        public ModifiedFlowLayout(int align) {
            super(align);
        }
        public ModifiedFlowLayout(int align, int hgap, int vgap) {
            super(align, hgap, vgap);
        }

        public Dimension minimumLayoutSize(Container target) {
            // Size of largest component, so we can resize it in
            // either direction with something like a split-pane.
            return computeMinSize(target);
        }

        public Dimension preferredLayoutSize(Container target) {
            return computeSize(target);
        }

        private Dimension computeSize(Container target) {
            synchronized (target.getTreeLock()) {
                int hgap = getHgap();
                int vgap = getVgap();
                int w = target.getWidth();

                // Let this behave like a regular FlowLayout (single row)
                // if the container hasn't been assigned any size yet
                if (w == 0) {
                    w = Integer.MAX_VALUE;
                }

                Insets insets = target.getInsets();
                if (insets == null){
                    insets = new Insets(0, 0, 0, 0);
                }
                int reqdWidth = 0;

                int maxwidth = w - (insets.left + insets.right + hgap * 2);
                int n = target.getComponentCount();
                int x = 0;
                int y = insets.top + vgap; // FlowLayout starts by adding vgap, so do that here too.
                int rowHeight = 0;

                for (int i = 0; i < n; i++) {
                    Component c = target.getComponent(i);
                    if (c.isVisible()) {
                        Dimension d = c.getPreferredSize();
                        if ((x == 0) || ((x + d.width) <= maxwidth)) {
                            // fits in current row.
                            if (x > 0) {
                                x += hgap;
                            }
                            x += d.width;
                            rowHeight = Math.max(rowHeight, d.height);
                        }
                        else {
                            // Start of new row
                            x = d.width;
                            y += vgap + rowHeight;
                            rowHeight = d.height;
                        }
                        reqdWidth = Math.max(reqdWidth, x);
                    }
                }
                y += rowHeight;
                y += insets.bottom;
                return new Dimension(reqdWidth+insets.left+insets.right, y);
            }
        }

        private Dimension computeMinSize(Container target) {
            synchronized (target.getTreeLock()) {
                int minx = Integer.MAX_VALUE;
                int miny = Integer.MIN_VALUE;
                boolean found_one = false;
                int n = target.getComponentCount();

                for (int i = 0; i < n; i++) {
                    Component c = target.getComponent(i);
                    if (c.isVisible()) {
                        found_one = true;
                        Dimension d = c.getPreferredSize();
                        minx = Math.min(minx, d.width);
                        miny = Math.min(miny, d.height);
                    }
                }
                if (found_one) {
                    return new Dimension(minx, miny);
                }
                return new Dimension(0, 0);
            }
        }

    }
}

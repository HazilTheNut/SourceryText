package Game.Debug;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class DebugLogPane extends JComponent {

    /**
     * DebugLogPane:
     *
     * The standard component for the DebugWindow.
     *
     * All text being sent to this component must be associated with a caption.
     * The DebugLogPane is capable of replacing text that have matching captions, keeping the DebugWindow more tidy.
     */

    private ArrayList<DebugEntry> debugEntries = new ArrayList<>();
    private final int VERT_SEP = 18;

    private boolean captionSensitive;

    private JScrollPane scrollPane;

    public DebugLogPane(boolean captionSensitive){
        this.captionSensitive = captionSensitive;
        setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
    }

    public boolean isCaptionSensitive() {
        return captionSensitive;
    }

    void addEntry(String caption, String text){
        if (captionSensitive) {
            for (int i = 0; i < debugEntries.size(); i++) {
                if (debugEntries.get(i).caption.equals(caption)) {
                    debugEntries.get(i).text = text;
                    return;
                }
            }
        }
        debugEntries.add(new DebugEntry(caption, text));
    }

    void removeEntry(String caption){
        for (int i = 0; i < debugEntries.size(); i++) {
            if (debugEntries.get(i).caption.equals(caption)) {
                debugEntries.remove(i);
                return;
            }
        }
    }

    public void update(){
        debugEntries.removeAll(debugEntries.subList(0, Math.max(0, debugEntries.size() - 100000))); //Prevents memory leaks!
        setPreferredSize(new Dimension(calculatePreferredWidth(), VERT_SEP * debugEntries.size()));
        repaint();
    }

    private int calculatePreferredWidth(){
        int max = 0;
        for (int i = 0 ; i < debugEntries.size(); i++) {
            DebugEntry entry = debugEntries.get(i);
            String entryText = String.format("[%1$s] %2$s", entry.caption, entry.text);
            max = Math.max(getFontMetrics(getFont()).stringWidth(entryText), max);
        }
        return max + 10;
    }

    void clear(){
        debugEntries.clear();
        repaint();
    }

    @Override
    public void paint(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(DebugWindow.textColor);
        for (int i = 0 ; i < debugEntries.size(); i++) {
            DebugEntry entry = debugEntries.get(i);
            String entryText = String.format("[%1$s] %2$s", entry.caption, entry.text);
            g.drawString(entryText, 1, (i+1) * VERT_SEP - 5);
        }
    }

    public void setScrollPane(JScrollPane scrollPane) {
        this.scrollPane = scrollPane;
    }

    void moveScrollBar(){
        scrollPane.getViewport().revalidate();
        revalidate();
        scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());
        scrollPane.repaint();
    }

    private class DebugEntry {
        String caption;
        String text;
        private DebugEntry(String caption, String text){
            this.text = text;
            this.caption = caption;
        }
    }
}

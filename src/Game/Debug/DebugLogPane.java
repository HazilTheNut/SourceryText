package Game.Debug;

import javax.swing.*;
import java.awt.*;

public class DebugLogPane extends JComponent {

    /**
     * DebugLogPane:
     *
     * The standard component for the DebugWindow.
     *
     * All text being sent to this component must be associated with a caption.
     * The DebugLogPane is capable of replacing text that have matching captions, keeping the DebugWindow more tidy.
     */

    private DebugEntry[] debugEntries;
    private final int VERT_SEP = 18;
    private int mostRecentEntry;
    private static final int ARRAY_SIZE = 5000;
    private int entryCount;

    private boolean captionSensitive;
    private boolean requiresUpdate = false;
    private boolean showCaptions = true;

    private JScrollPane scrollPane;

    public DebugLogPane(boolean captionSensitive){
        this.captionSensitive = captionSensitive;
        setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        debugEntries = new DebugEntry[ARRAY_SIZE];
        mostRecentEntry = 0;
        entryCount = 0;
    }

    public DebugLogPane(boolean captionSensitive, boolean showCaptions){
        this(captionSensitive);
        this.showCaptions = showCaptions;
    }

    public boolean isCaptionSensitive() {
        return captionSensitive;
    }

    void addEntry(String caption, String text){
        requiresUpdate = true;
        if (captionSensitive) {
            for (DebugEntry debugEntry : debugEntries) {
                if (debugEntry != null && debugEntry.caption.equals(caption)) {
                    debugEntry.text = text;
                    return;
                }
            }
        }
        mostRecentEntry++;
        if (mostRecentEntry >= debugEntries.length) mostRecentEntry = 0;
        debugEntries[mostRecentEntry] = new DebugEntry(caption, text);
        entryCount = Math.min(entryCount + 1, ARRAY_SIZE);
    }

    void removeEntry(String caption){
        requiresUpdate = true;
        if (captionSensitive) {
            for (DebugEntry debugEntry : debugEntries) {
                if (debugEntry != null && debugEntry.caption.equals(caption)) {
                    debugEntry.text = null; //Null elements will get skipped over when rendering
                    entryCount--;
                    return;
                }
            }
        }
    }

    public void update(){
        if (requiresUpdate) { //Don't need to redraw if nothing has changed
            setPreferredSize(new Dimension(calculatePreferredWidth(), VERT_SEP * entryCount));
            repaint();
            requiresUpdate = false;
        }
    }

    private int calculatePreferredWidth(){
        int max = 0;
        FontMetrics metrics = getFontMetrics(getFont());
        int charLength = metrics.stringWidth("#");
        for (DebugEntry entry : debugEntries) {
            if (entry != null) {
                int stringLength = entry.caption.length() + entry.text.length() + 5; //You could do String.format for a more accurate picture, but that's inefficient to do ~1000 times / 0.1 sec
                max = Math.max(charLength * stringLength, max);
            }
        }
        return max + 10;
    }

    void clear(){
        debugEntries = new DebugEntry[ARRAY_SIZE];
        repaint();
    }

    @Override
    public void paint(Graphics g) {
        g.setColor(Color.BLACK);
        Rectangle viewRect = (scrollPane != null) ? scrollPane.getViewport().getViewRect() : new Rectangle(getWidth(), getHeight());
        g.fillRect((int)viewRect.getX(), (int)viewRect.getY(), (int)viewRect.getWidth(), (int)viewRect.getHeight());
        g.setColor(DebugWindow.textColor);
        if (entryCount <= 0) return;
        int arrayIndex = mostRecentEntry;
        do {
            int ypos = (arrayIndex) * VERT_SEP - 5;
            if (ypos - (int)viewRect.getY() >= -1 * VERT_SEP && ypos - (int)viewRect.getY() <= viewRect.getHeight() + VERT_SEP) { //Don't draw what will not be seen.
                DebugEntry entry = debugEntries[arrayIndex];
                if (entry != null) {
                    String entryText = (showCaptions) ? String.format("[%1$s] %2$s", entry.caption, entry.text) : entry.text;
                    g.drawString(entryText, 1, ypos);
                }
            }
            arrayIndex--;
            if (arrayIndex <= 0) arrayIndex = debugEntries.length - 1;
        } while (arrayIndex != mostRecentEntry);
    }

    public void setScrollPane(JScrollPane scrollPane) {
        this.scrollPane = scrollPane;
    }

    void moveScrollBar(){
        if (!captionSensitive) {
            scrollPane.getViewport().revalidate();
            revalidate();
            scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());
            scrollPane.repaint();
       }
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

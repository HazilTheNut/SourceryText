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
    private boolean requiresUpdate = false;

    private JScrollPane scrollPane;

    public DebugLogPane(boolean captionSensitive){
        this.captionSensitive = captionSensitive;
        setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
    }

    public boolean isCaptionSensitive() {
        return captionSensitive;
    }

    void addEntry(String caption, String text){
        requiresUpdate = true;
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
        requiresUpdate = true;
        if (captionSensitive) {
            for (int i = 0; i < debugEntries.size(); i++) {
                if (debugEntries.get(i).caption.equals(caption)) {
                    debugEntries.remove(i);
                    return;
                }
            }
        }
    }

    public void update(){
        if (requiresUpdate) { //Don't need to redraw if nothing has changed
            debugEntries.removeAll(debugEntries.subList(0, Math.max(0, debugEntries.size() - 100000))); //Prevents memory leaks!
            setPreferredSize(new Dimension(calculatePreferredWidth(), VERT_SEP * debugEntries.size()));
            repaint();
            requiresUpdate = false;
        }
    }

    private int calculatePreferredWidth(){
        int max = 0;
        FontMetrics metrics = getFontMetrics(getFont());
        int charLength = metrics.stringWidth("#");
        for (int i = 0 ; i < debugEntries.size(); i++) {
            DebugEntry entry = debugEntries.get(i);
            int stringLength = entry.caption.length() + entry.text.length() + 5; //You could do String.format for a more accurate picture, but that's inefficient to do ~1000 times / 0.1 sec
            max = Math.max(charLength * stringLength, max);
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
        Rectangle viewRect = scrollPane.getViewport().getViewRect();
        g.fillRect((int)viewRect.getX(), (int)viewRect.getY(), (int)viewRect.getWidth(), (int)viewRect.getHeight());
        g.setColor(DebugWindow.textColor);
        for (int i = 0 ; i < debugEntries.size(); i++) {
            int ypos = (i+1) * VERT_SEP - 5;
            if (ypos - (int)viewRect.getY() >= -1 * VERT_SEP && ypos - (int)viewRect.getY() <= viewRect.getHeight() + VERT_SEP) { //Don't draw what will not be seen.
                DebugEntry entry = debugEntries.get(i);
                String entryText = String.format("[%1$s] %2$s", entry.caption, entry.text);
                g.drawString(entryText, 1, ypos);
            }
        }
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

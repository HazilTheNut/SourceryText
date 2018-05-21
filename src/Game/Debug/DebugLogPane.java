package Game.Debug;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class DebugLogPane extends JComponent {

    private ArrayList<DebugEntry> debugEntries = new ArrayList<>();
    private final int VERT_SEP = 18;

    private boolean captionSensitive;

    public DebugLogPane(boolean captionSensitive){
        this.captionSensitive = captionSensitive;
        setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
    }

    void addEntry(String caption, String text){
        if (captionSensitive) {
            for (int i = 0; i < debugEntries.size(); i++) {
                if (debugEntries.get(i).caption.equals(caption)) {
                    debugEntries.get(i).text = text;
                    repaint();
                    return;
                }
            }
        }
        debugEntries.add(new DebugEntry(caption, text));
        if (debugEntries.size() > 10000) debugEntries.remove(0);
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
        g.setColor(new Color(191, 244, 255));
        for (int i = 0 ; i < debugEntries.size(); i++) {
            DebugEntry entry = debugEntries.get(i);
            String entryText = String.format("[%1$s] %2$s", entry.caption, entry.text);
            g.drawString(entryText, 1, (i+1) * VERT_SEP - 5);
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

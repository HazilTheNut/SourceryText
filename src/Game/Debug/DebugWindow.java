package Game.Debug;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * Created by Jared on 4/17/2018.
 */
public class DebugWindow{

    private static JFrame frame;
    private static DebugLogPane performance;
    private static DebugLogPane stage;
    private static DebugLogPane tags;
    private static DebugLogPane game;
    private static DebugLogPane misc;

    private static ArrayList<TextDispenseUpdate> dispenseUpdates;

    public static final int PERFORMANCE = 0; //For performance data
    public static final int STAGE       = 1; //'Stage' refers to systems that surround the main game stuff (like simulation, AI, etc.)
    public static final int TAGS        = 2; //For all events and things related to tags
    public static final int GAME        = 3; //The main game stuff that is surrounded by the 'stage'
    public static final int MISC        = 4; //Everything else

    private static final int OUTPUT_MAX_LENGTH = 1000000;

    static {
        dispenseUpdates = new ArrayList<>();

        frame = new JFrame();

        frame.setTitle("Debug Log");
        frame.setMinimumSize(new Dimension(300, 300));

        performance = new DebugLogPane(true);
        stage = new DebugLogPane(true);
        tags = new DebugLogPane(true);
        game = new DebugLogPane(false);
        misc = new DebugLogPane(false);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Perf",   createScrollPane(tabbedPane, performance));
        tabbedPane.addTab("Stage",  createScrollPane(tabbedPane, stage));
        tabbedPane.addTab("Tags",   createScrollPane(tabbedPane, tags));
        tabbedPane.addTab("Game",   createScrollPane(tabbedPane, game));
        tabbedPane.addTab("Misc",   createScrollPane(tabbedPane, misc));

        frame.add(tabbedPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.PAGE_AXIS));

        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> {
            Component c = tabbedPane.getComponentAt(tabbedPane.getSelectedIndex());
            if (c instanceof JScrollPane) {
                JScrollPane jScrollPane = (JScrollPane)c;
                if (jScrollPane.getViewport().getView() instanceof DebugLogPane) {
                    DebugLogPane logPane = (DebugLogPane)jScrollPane.getViewport().getView();
                    logPane.clear();
                }
            }
        });

        bottomPanel.add(clearButton);

        frame.add(bottomPanel, BorderLayout.PAGE_END);

        frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

    }

    public static void open(){
        frame.setVisible(true);
    }

    private static JScrollPane createScrollPane(JTabbedPane tabbedPane, JComponent screen){
        JScrollPane scrollPane =  new JScrollPane(screen, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        tabbedPane.addChangeListener(e -> moveScrollBarToBottom(scrollPane, screen));
        dispenseUpdates.add(() -> moveScrollBarToBottom(scrollPane, screen));
        return scrollPane;
    }

    private static void moveScrollBarToBottom(JScrollPane scrollPane, JComponent client){
        scrollPane.getViewport().revalidate();
        client.revalidate();
        scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());
        scrollPane.repaint();
    }

    public static void reportf(int screen, String caption, String value, Object... args){
        DebugLogPane logPane = getDebugLog(screen);
        assert logPane != null;
        logPane.addEntry(caption, String.format(value, args));
        for (TextDispenseUpdate update : dispenseUpdates) update.update();
    }

    public static void clear(int screen){
        DebugLogPane logPane = getDebugLog(screen);
        assert logPane != null;
        logPane.clear();
    }

    private static DebugLogPane getDebugLog(int screen){
        switch (screen){
            case PERFORMANCE:
                return performance;
            case STAGE:
                return stage;
            case TAGS:
                return tags;
            case GAME:
                return game;
            case MISC:
                return misc;
            default:
                return null;
        }
    }

    private interface TextDispenseUpdate {
        void update();
    }
}

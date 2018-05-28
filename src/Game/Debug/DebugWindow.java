package Game.Debug;

import Engine.Layer;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Jared on 4/17/2018.
 */
public class DebugWindow{

    private static JFrame frame;
    private static DebugLogPane performance;
    private static DebugLogPane stage;
    private static DebugLogPane tags;
    private static DebugLogPane game;
    private static DebugLogPane entity;
    private static DebugLogPane misc;
    private static DebugLogPane cursor;
    private static DebugLayerPanel layers;

    private static ArrayList<TextDispenseUpdate> dispenseUpdates;

    public static final int PERFORMANCE = 0; //For performance data
    public static final int STAGE       = 1; //'Stage' refers to systems that surround the main game stuff (like simulation, UI, etc.)
    public static final int TAGS        = 2; //For all events and things related to tags
    public static final int GAME        = 3; //The main game stuff that is surrounded by the 'stage'
    public static final int ENTITY      = 4; //Mainly a spam folder for entity info
    public static final int MISC        = 5; //Everything else
    public static final int CURSOR      = 6; //For display about stuff under the cursor

    static final Color textColor = new Color(191, 244, 255);

    private static ArrayList<Runnable> entryActions = new ArrayList<>();

    static {
        dispenseUpdates = new ArrayList<>();

        frame = new JFrame();

        frame.setTitle("Debug Log");
        frame.setMinimumSize(new Dimension(400, 400));
        frame.setLayout(new BorderLayout());

        performance = new DebugLogPane(true);
        stage = new DebugLogPane(true);
        tags = new DebugLogPane(true);
        game = new DebugLogPane(false);
        entity = new DebugLogPane(true);
        misc = new DebugLogPane(false);
        cursor = new DebugLogPane(true);
        layers = new DebugLayerPanel();

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Perf",   createScrollPane(tabbedPane, performance));
        tabbedPane.addTab("Stage",  createScrollPane(tabbedPane, stage));
        tabbedPane.addTab("Tags",   createScrollPane(tabbedPane, tags));
        tabbedPane.addTab("Game",   createScrollPane(tabbedPane, game));
        tabbedPane.addTab("Entity", createScrollPane(tabbedPane, entity));
        tabbedPane.addTab("Misc",   createScrollPane(tabbedPane, misc));
        tabbedPane.addTab("Cursor", createScrollPane(tabbedPane, cursor));
        tabbedPane.addTab("Layers", layers);

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

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    for (int i = 0; i < entryActions.size();) {
                        if (entryActions.get(i) != null) {
                            entryActions.get(i).run();
                            entryActions.remove(i);
                        }
                    }
                } catch (NullPointerException | ConcurrentModificationException e){
                    e.printStackTrace();
                }

            }
        }, 10, 100);
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
        entryActions.add(() -> {
            DebugLogPane logPane = getDebugLog(screen);
            assert logPane != null;
            logPane.addEntry(caption, String.format(value, args));
            for (TextDispenseUpdate update : dispenseUpdates) update.update();
        });
    }

    public static void addLayerView(Layer toView, int pos) {
        layers.addLayerView(toView, pos);
    }

    public static void removeLayerView(Layer toView) {
        layers.removeLayerView(toView);
    }

    public static void updateLayerInfo(){
        layers.updateLayerCheckBoxes();
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
            case ENTITY:
                return entity;
            case MISC:
                return misc;
            case CURSOR:
                return cursor;
            default:
                return null;
        }
    }

    private interface TextDispenseUpdate {
        void update();
    }
}

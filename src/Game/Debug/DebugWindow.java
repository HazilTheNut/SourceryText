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

    /**
     * DebugWindow:
     *
     * The master object that controls the debug output of SourceryText.
     *
     * You can send debug info to any of several tabs, allowing for everything to be nicely sorted.
     *
     * The DebugWindow also has a roster of the Layers currently active while playing SourceryText.
     * You can set each layer in the roster to be visible or invisible individually.
     */

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
        dispenseUpdates = new ArrayList<>(); //Holds a list of actions that moves every scroll bar to its bottom whenever new text is added.

        frame = new JFrame();

        frame.setTitle("Debug Log");
        frame.setMinimumSize(new Dimension(400, 400));
        frame.setLayout(new BorderLayout());

        performance = new DebugLogPane(true); //'true' marks a DebugLogPane to replace text of matching captions
        stage = new DebugLogPane(true); //The term 'caption sensitive' refers to whether or not it cares about it.
        tags = new DebugLogPane(true);
        game = new DebugLogPane(false);
        entity = new DebugLogPane(true);
        misc = new DebugLogPane(false);
        cursor = new DebugLogPane(true);
        layers = new DebugLayerPanel();

        //Create UI
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
        timer.scheduleAtFixedRate(new TimerTask() { //Keeping things happy with no co-modification errors.
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

    /**
     * Reports to a DebugLogPane with text and a caption.
     * reportf() uses String.format to make reporting data a lot easier.
     * However, most IDEs will not recognize the usage of String.format when calling this function, and will throw errors about malformed strings without warning you about it.
     *
     * The 'f' in 'reportf' stands for "formatted"
     *
     * @param screen The integer ID of the screen you are sending info to. Use the static constants above for your convenience.
     * @param caption The String caption for the text. IF the caption matches one of the entries already in there, the text of that entry will be replaced
     * @param value The formatted String being sent as an entry to a DebugLogPane
     * @param args The objects being substituting into the value, according to Java String formatting.
     */
    public static void reportf(int screen, String caption, String value, Object... args){
        entryActions.add(() -> {
            DebugLogPane logPane = getDebugLog(screen);
            assert logPane != null;
            logPane.addEntry(caption, String.format(value, args));
            for (TextDispenseUpdate update : dispenseUpdates) update.update();
        });
    }

    //Adds a Layer to the Layers pane
    public static void addLayerView(Layer toView, int pos) {
        layers.addLayerView(toView, pos);
    }

    //Removes a Layer from the Layers pane
    public static void removeLayerView(Layer toView) {
        layers.removeLayerView(toView);
    }

    //Update the Layers in the Layers pane to keep the check box states consistent with the Layers they are tracking.
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

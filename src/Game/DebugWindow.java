package Game;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.ArrayList;

/**
 * Created by Jared on 4/17/2018.
 */
public class DebugWindow{

    private static JFrame frame;
    private static JTextArea performance;
    private static JTextArea stage;
    private static JTextArea tags;
    private static JTextArea game;
    private static JTextArea misc;

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

        performance = new JTextArea();
        formatTextArea(performance);

        stage = new JTextArea();
        formatTextArea(stage);

        tags = new JTextArea();
        formatTextArea(tags);

        game = new JTextArea();
        formatTextArea(game);

        misc = new JTextArea();
        formatTextArea(misc);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Perf",   createScrollPane(tabbedPane, performance));
        tabbedPane.addTab("Stage",  createScrollPane(tabbedPane, stage));
        tabbedPane.addTab("Tags",   createScrollPane(tabbedPane, tags));
        tabbedPane.addTab("Entity", createScrollPane(tabbedPane, game));
        tabbedPane.addTab("Misc",  createScrollPane(tabbedPane, misc));

        frame.add(tabbedPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.PAGE_AXIS));

        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> {
            Component c = tabbedPane.getComponentAt(tabbedPane.getSelectedIndex());
            if (c instanceof JScrollPane) {
                JScrollPane jScrollPane = (JScrollPane)c;
                if (jScrollPane.getViewport().getView() instanceof JTextArea) {
                    JTextArea view = (JTextArea) jScrollPane.getViewport().getView();
                    view.setText("");
                }
            }
        });

        bottomPanel.add(clearButton);

        frame.add(bottomPanel, BorderLayout.PAGE_END);

        frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

    }

    static void open(){
        frame.setVisible(true);
    }

    private static void formatTextArea(JTextArea area){
        area.setForeground(Color.WHITE);
        area.setBackground(Color.BLACK);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
    }

    private static JScrollPane createScrollPane(JTabbedPane tabbedPane, JTextArea area){
        JScrollPane scrollPane =  new JScrollPane(area, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        tabbedPane.addChangeListener(e -> moveScrollBarToBottom(scrollPane));
        dispenseUpdates.add(() -> moveScrollBarToBottom(scrollPane));
        return scrollPane;
    }

    private static void moveScrollBarToBottom(JScrollPane scrollPane){
        scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());
    }

    public static void reportf(int screen, String value, Object... args){
        JTextArea area = getTextArea(screen);
        if (area != null) {
            area.append(String.format(value, args) + "\n");
            checkTextArea(area);
        }
    }

    public static void clear(int screen){
        JTextArea area = getTextArea(screen);
        if (area != null) {
            area.setText("");
            checkTextArea(area);
        }
    }

    private static void checkTextArea(JTextArea area){
        if (area.getText().length() > OUTPUT_MAX_LENGTH){
            area.setText(area.getText().substring(area.getText().length() - OUTPUT_MAX_LENGTH));
        }
        for (TextDispenseUpdate update : dispenseUpdates) update.update();
    }

    private static JTextArea getTextArea(int screen){
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

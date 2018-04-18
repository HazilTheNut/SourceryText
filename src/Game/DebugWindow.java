package Game;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Jared on 4/17/2018.
 */
public class DebugWindow extends JFrame{

    private JTextArea performance;

    public DebugWindow(){
        setTitle("Debug Log");
        setMinimumSize(new Dimension(300, 300));

        performance = new JTextArea();
        formatTextArea(performance);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Perf", performance);

        add(tabbedPane);

        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
    }

    private void formatTextArea(JTextArea area){
        area.setForeground(Color.WHITE);
        area.setBackground(Color.BLACK);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
    }

    public void reportPerformance(long[] times){
        if (times.length >= 4) {
            double entityop   = (double)(times[1] - times[0]) / 1000000;
            double entityturn = (double)(times[2] - times[1]) / 1000000;
            double tileupdate = (double)(times[3] - times[2]) / 1000000;
            double total      = (double)(times[3] - times[0]) / 1000000;
            performance.setText(String.format("[GameInstance.printTurnTimes] Results:\n>  entityop:   %1$f\n>  entityturn: %2$f\n>  tileupdate: %3$f\n\n>   TOTAL: %4$f", entityop, entityturn, tileupdate, total));
        }
        else
            performance.setText("");
        performance.repaint();
    }
}

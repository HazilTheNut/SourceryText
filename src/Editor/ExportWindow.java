package Editor;

import Data.*;
import Engine.Layer;
import Engine.SpecialText;
import Game.Registries.LevelScriptRegistry;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class ExportWindow extends JFrame {

    public ExportWindow(LevelData levelData){

        setLayout(new BorderLayout());

        JTextPane exportedTextField = new JTextPane();
        exportedTextField.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));

        exportToTextArt(exportedTextField, levelData);

        JPanel noWrapPanel = new JPanel(new BorderLayout());
        noWrapPanel.add(exportedTextField);

        JScrollPane scrollPane = new JScrollPane(noWrapPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane, BorderLayout.CENTER);

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.LINE_AXIS));

        JButton textArtButton = new JButton("to ASCII Art");
        textArtButton.addActionListener(e -> exportToTextArt(exportedTextField, levelData));
        topPanel.add(textArtButton);

        JButton textAllData = new JButton("to Data Dump");
        textAllData.addActionListener(e -> exportToDataDump(exportedTextField, levelData));
        topPanel.add(textAllData);

        add(topPanel, BorderLayout.PAGE_START);

        setSize(500, 350);
        setTitle("Export To Text");
        setVisible(true);
    }

    private void exportToTextArt(JTextPane textPane, LevelData ldata){
        StringBuilder builder = new StringBuilder();
        Layer backdrop = ldata.getBackdrop();
        for (int row = 0; row < backdrop.getRows(); row++) {
            for (int col = 0; col < backdrop.getCols(); col++) {
                if (backdrop.getSpecialText(col, row) == null)
                    builder.append(" ");
                else
                    builder.append(backdrop.getSpecialText(col, row).getCharacter());
            }
            builder.append("\n");
        }
        textPane.setText(builder.toString());
    }

    private void exportToDataDump(JTextPane textPane, LevelData ldata){
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("Dim: %1$d x %2$d\n\n", ldata.getBackdrop().getCols(), ldata.getBackdrop().getRows()));

        builder.append(dataDumpBackdrop(ldata));
        builder.append(dataDumpTileData(ldata));
        builder.append(dataDumpEntities(ldata));
        builder.append(dataDumpWarpZone(ldata));
        builder.append(dataDumpLevelScript(ldata)); //I did this to annoy you.

        textPane.setText(builder.toString());
    }

    private String dataDumpBackdrop(LevelData levelData){
        Layer backdrop = levelData.getBackdrop();
        ArrayList<SpecialText> dictionary = new ArrayList<>();
        dictionary.add(new SpecialText(' '));
        int[][] backdropMatrix = new int[backdrop.getCols()][backdrop.getRows()];
        for (int col = 0; col < backdrop.getCols(); col++) {
            for (int row = 0; row < backdrop.getRows(); row++) {
                SpecialText get = backdrop.getSpecialText(col, row);
                if (get == null) {
                    backdropMatrix[col][row] = 0;
                } else {
                    int index = dictionary.indexOf(get);
                    if (index == -1){
                        backdropMatrix[col][row] = dictionary.size();
                        dictionary.add(get.copy());
                    } else
                        backdropMatrix[col][row] = index;
                }
            }
        }
        StringBuilder builder = new StringBuilder();
        builder.append("BACKDROP\n\nDictionary:\n");
        builder.append("000 : null\n");
        for (int i = 1; i < dictionary.size(); i++) {
            builder.append(String.format("%1$03d : %2$s\n", i, dictionary.get(i)));
        }
        builder.append("\nContents:\n");
        for (int row = 0; row < backdropMatrix[0].length; row++) {
            for (int col = 0; col < backdropMatrix.length; col++) {
                builder.append(String.format("%1$-3d ", backdropMatrix[col][row]));
            }
            builder.append("#\n");
        }
        return builder.toString();
    }

    private String dataDumpTileData(LevelData levelData){
        StringBuilder builder = new StringBuilder();
        builder.append("\nTILE DATA\n\nContents:\n");
        for (int row = 0; row < levelData.getBackdrop().getRows(); row++) {
            for (int col = 0; col < levelData.getBackdrop().getCols(); col++) {
                builder.append(String.format("%1$-3d ", levelData.getTileData()[col][row]));
            }
            builder.append("#\n");
        }
        return builder.toString();
    }

    private String dataDumpEntities(LevelData levelData){
        StringBuilder builder = new StringBuilder();
        builder.append("\nENTITY\n\nContents:\n");
        for (int col = 0; col < levelData.getBackdrop().getCols(); col++) {
            for (int row = 0; row < levelData.getBackdrop().getRows(); row++) {
                EntityStruct struct = levelData.getEntityAt(col, row);
                if (struct != null){
                    builder.append(String.format("Pos: %1$s Id #%2$d \'%3$s\'\nInv:\n", new Coordinate(col, row), struct.getEntityId(), struct.getEntityName()));
                    for (ItemStruct itemStruct : struct.getItems())
                        builder.append(String.format("> Id: %1$d x %2$d \'%3$s\'\n", itemStruct.getItemId(), itemStruct.getQty(), itemStruct.getName()));
                    builder.append("Args:\n");
                    for (EntityArg entityArg : struct.getArgs())
                        builder.append(String.format("> %1$s:%2$s\n", entityArg.getArgName(), entityArg.getArgValue()));
                    builder.append("\n");
                }
            }
        }
        return builder.toString();
    }

    private String dataDumpWarpZone(LevelData levelData){
        StringBuilder builder = new StringBuilder();
        builder.append("\nWARP ZONES\n\nContents:\n");
        for (WarpZone warpZone : levelData.getWarpZones())
            builder.append(String.format("Pos: %1$s Dim: %2$d x %3$d\nTo: %4$s\nAt: %5$s\n\n", new Coordinate(warpZone.getXpos(), warpZone.getYpos()), warpZone.getWidth(), warpZone.getHeight(), warpZone.getRoomFilePath(), new Coordinate(warpZone.getNewRoomStartX(), warpZone.getNewRoomStartY())));
        return builder.toString();
    }

    private String dataDumpLevelScript(LevelData levelData){
        StringBuilder builder = new StringBuilder();
        builder.append("\nLEVEL SCRIPT\n\nActive Scripts:\n\n");
        ArrayList<LevelScriptMask> masks = levelData.getLevelScriptMasks();
        for (int scriptId : levelData.getLevelScripts()){
            builder.append(String.format("id: %1$d \'%2$s\'\nMasks:\n", scriptId, LevelScriptRegistry.getLevelScriptClass(scriptId).getSimpleName()));
            for (LevelScriptMask mask : masks){
                if (mask.getScriptId() == scriptId){
                    builder.append("> ").append(mask.getName()).append("\n").append(drawMask(mask));
                }
            }
            builder.append("\n");
        }
        return builder.toString();
    }

    private String drawMask(LevelScriptMask mask){
        StringBuilder builder = new StringBuilder();
        for (int row = 0; row < mask.getMask()[0].length; row++) {
            for (int col = 0; col < mask.getMask().length; col++) {
                if (mask.getMask()[col][row])
                    builder.append('#');
                else
                    builder.append('.');
            }
            builder.append(" |\n");
        }
        return builder.toString();
    }
}

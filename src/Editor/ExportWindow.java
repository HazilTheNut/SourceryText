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

        JTextField discordRenderXField = new JTextField("0");
        JTextField discordRenderYField = new JTextField("0");

        JButton textDiscordRender = new JButton("to Discord Render");
        textDiscordRender.addActionListener(e -> {
            try {
                exportToDiscordRender(exportedTextField, levelData, Integer.valueOf(discordRenderXField.getText()), Integer.valueOf(discordRenderYField.getText()));
            } catch (NumberFormatException ignored){}
        });
        topPanel.add(textDiscordRender);
        topPanel.add(new JLabel("x:"));
        topPanel.add(discordRenderXField);
        topPanel.add(new JLabel("y:"));
        topPanel.add(discordRenderYField);

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
        return builder.toString();k
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

    private void exportToDiscordRender(JTextPane textPane, LevelData levelData, int startX, int startY){
        //Draw the layer
        Layer renderLayer = drawDiscordRender(levelData, startX, startY);
        StringBuilder builder = new StringBuilder("```java\n");
        for (int row = 0; row < renderLayer.getRows(); row++){
            for (int col = 0; col < renderLayer.getCols(); col++){
                if (renderLayer.getSpecialText(col, row) == null){
                    builder.append(' ');
                } else
                    builder.append(renderLayer.getSpecialText(col, row).getCharacter());
            }
            builder.append('\n');
        }
        builder.append("```");
        textPane.setText(builder.toString());
    }

    private Layer drawDiscordRender(LevelData ldata, int startX, int startY){
        int renderRows = 23;
        char[] colLetters = {'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z','a','b','c','d','e','f','g','h','i','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z'};
        int renderCols = colLetters.length;
        Layer renderLayer = new Layer(renderCols + 4, renderRows + 3, "discord_render", 0, 0, 0);
        renderLayer.insert(ldata.getBackdrop(), new Coordinate(4, 2), new Coordinate(startX, startY));
        renderLayer.fillLayer(new SpecialText('|'), new Coordinate(2, 2), new Coordinate(2, renderRows + 1));
        renderLayer.fillLayer(new SpecialText('|'), new Coordinate(renderCols + 3, 2), new Coordinate(renderCols + 3, renderRows + 1));
        renderLayer.fillLayer(new SpecialText('-'), new Coordinate(3, 1), new Coordinate(renderCols + 2, 1));
        renderLayer.fillLayer(new SpecialText('-'), new Coordinate(3, renderRows + 2), new Coordinate(renderCols + 2, renderRows + 2));
        renderLayer.editLayer(2, 1, '+');
        renderLayer.editLayer(renderCols + 3, 1, '+');
        renderLayer.editLayer(2, renderRows + 2, '+');
        renderLayer.editLayer(renderCols + 3, renderRows + 2, '+');
        drawDiscordRenderDots(renderLayer, renderCols, renderRows, 3, 2);
        //Draw rows
        for (int i = 0; i < renderRows; i++) {
            renderLayer.inscribeString(String.valueOf(i), 0, i+2);
        }
        //Draw columns
        for (int i = 0; i < renderCols; i++) {
            renderLayer.editLayer(i+3, 0, colLetters[i]);
        }
        return renderLayer;
    }

    private void drawDiscordRenderDots(Layer renderLayer, int renderWidth, int renderHeight, int offsetX, int offsetY){
        int x = 0;
        int y = 0;
        while (y < renderHeight){
            while (x < renderWidth){
                SpecialText atLoc = renderLayer.getSpecialText(x + offsetX, y + offsetY);
                boolean unfilled = atLoc == null || atLoc.getCharacter() == ' ';
                boolean xSatisfied = (x-1) % 4 == 0;
                boolean ySatisfied = (y-1) % 4 == 0; // && (x-1) % 2 == 0
                if (unfilled && (xSatisfied || ySatisfied))
                    renderLayer.editLayer(x + offsetX, y + offsetY, '.');
                x++;
            }
            x = 0;
            y++;
        }
    }
}

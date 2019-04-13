package Game;

import Data.Coordinate;
import Data.LayerImportances;
import Engine.Layer;
import Engine.LayerManager;
import Engine.SpecialText;

import java.awt.*;
import java.util.ArrayList;

public class ZoneScorecard implements GameInputReciever {

    private Layer scorecardLayer;
    private Zone zone;

    public ZoneScorecard(LayerManager manager, Zone zone,  GameMouseInput mouseInput){
        if (manager != null)
            scorecardLayer = new Layer(manager.getWindow().RESOLUTION_WIDTH, manager.getWindow().RESOLUTION_HEIGHT, "scorecard", 0, 0, LayerImportances.MAIN_MENU);
        else
            scorecardLayer = new Layer(0, 0, "scorecard", 0, 0, LayerImportances.MAIN_MENU);
        scorecardLayer.setVisible(false);
        scorecardLayer.fixedScreenPos = true;
        this.zone = zone;
        mouseInput.addInputReceiver(this);
    }

    void drawScorecardForZone(GameInstance gi){
        //Don't draw scorecard if the zone is improperly configured
        if (zone.getZoneInfoMap() == null || getZoneInfoInt("showScorecard") == 0)
            return;
        //Initialize scorecard layer
        scorecardLayer.fillLayer(new SpecialText(' ', Color.WHITE, Color.BLACK));
        scorecardLayer.setVisible(true);
        gi.getLayerManager().addLayer(scorecardLayer);
        //Draw header
        String header = String.format("CHAPTER %1$d %2$s %3$s %2$s COMPLETED!", getZoneInfoInt("chapter"), getZoneInfoString("sign"), getZoneInfoString("name"));
        scorecardLayer.inscribeString(header, 1, 0);
        //Turns elapsed counter
        String turns = String.format("Turns Elapsed: %1$d", zone.getTurnCounter());
        scorecardLayer.inscribeString(turns, 1, 1, TextBox.txt_silver);
        //Draw separator
        String separator = String.format("%1$s %1$s %1$s", getZoneInfoString("sign"));
        scorecardLayer.inscribeString(separator, 9, 3);
        //Draw magic potato counter
        String magicPotatoes = String.format("Magic Potatoes Eaten: %1$d / %2$d", zone.getMagicPotatoCounter(), getZoneInfoInt("magicPotatoes"));
        scorecardLayer.inscribeString(magicPotatoes, 1, 5);
        scorecardLayer.inscribeString(String.valueOf(zone.getMagicPotatoCounter()), 23, 5, TextBox.txt_cyan);
        //Draw notable events
        scorecardLayer.inscribeString("Notable Events:", 1, 9);
        int yOffset = 0;
        for (int i = 0; i < gi.getGameEvents().size(); i++) {
            String input = "\"" + gi.getGameEvents().get(i) + "\"";
            if (zone.getZoneInfoMap().containsKey(input)) {
                yOffset++;
                scorecardLayer.inscribeString("* " + getZoneInfoString(input), 3, 11 + yOffset, new Color(185, 185, 180));
            }
        }
        //Draw continue text
        String toContinue = String.format("Press %1$s to continue...", gi.getGameMaster().getMouseInput().getInputMap().getInputForAction(InputMap.TEXTBOX_NEXT));
        scorecardLayer.inscribeString(toContinue, 1, 15 + yOffset);
        //Await input
        waitForInput();
    }

    private void waitForInput(){
        while (scorecardLayer.getVisible())
            try {
                Thread.sleep(50);
            } catch (InterruptedException e){
                e.printStackTrace();
            }
    }

    private String getZoneInfoString(String key){
        String value = zone.getZoneInfoMap().get(key);
        if (value == null) return "";
        return value.substring(1, value.length() - 1);
    }

    private int getZoneInfoInt(String key){
        String value = zone.getZoneInfoMap().get(key);
        if (value == null) return 0;
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e){
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public boolean onMouseMove(Coordinate levelPos, Coordinate screenPos) {
        return scorecardLayer.getVisible();
    }

    @Override
    public boolean onMouseClick(Coordinate levelPos, Coordinate screenPos, int mouseButton) {
        return scorecardLayer.getVisible();
    }

    @Override
    public boolean onMouseWheel(Coordinate levelPos, Coordinate screenPos, double wheelMovement) {
        return scorecardLayer.getVisible();
    }

    @Override
    public boolean onInputDown(Coordinate levelPos, Coordinate screenPos, ArrayList<Integer> actions) {
        return scorecardLayer.getVisible();
    }

    @Override
    public boolean onInputUp(Coordinate levelPos, Coordinate screenPos, ArrayList<Integer> actions) {
        return scorecardLayer.getVisible();
    }

    @Override
    public boolean onNumberKey(Coordinate levelPos, Coordinate screenPos, int number) {
        return scorecardLayer.getVisible();
    }
}

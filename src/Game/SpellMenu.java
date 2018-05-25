package Game;

import Data.Coordinate;
import Data.LayerImportances;
import Data.SerializationVersion;
import Engine.Layer;
import Engine.SpecialText;
import Game.Debug.DebugWindow;
import Game.Spells.Spell;

import java.awt.*;
import java.io.Serializable;

public class SpellMenu implements Serializable {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    private Layer menuLayer;
    private Layer cursorLayer;
    private Player player;

    private int listLength;
    private boolean isShowing = false;
    private boolean mouseOverDropdownBtn = false;

    private Coordinate prevMousePos;

    final int width = 12;

    public SpellMenu(Player player){
        menuLayer = new Layer(width, 50, "HUD_spellmenu", 0, 0, LayerImportances.HUD_SPELL_MENU);
        menuLayer.fixedScreenPos = true;
        cursorLayer = new Layer(width, 1, "HUD_spellcursor", 0, 0, LayerImportances.HUD_SPELL_CURSOR);
        cursorLayer.fixedScreenPos = true;
        cursorLayer.fillLayer(new SpecialText(' ', Color.WHITE, new Color(210, 210, 210, 100)));
        cursorLayer.setVisible(false);
        this.player = player;
        player.getGameInstance().getLayerManager().addLayer(menuLayer);
        player.getGameInstance().getLayerManager().addLayer(cursorLayer);
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    void show(){
        isShowing = true;
        menuLayer.clearLayer();
        drawTopBand(prevMousePos);
        listLength = player.getSpells().size();
        DebugWindow.reportf(DebugWindow.STAGE, "HUD.SpellMenu.show","player spell stack size: %1$d", listLength);
        for (int i = 0; i < listLength; i++) {
            drawBand(new Color(11, 10, 15), i+1);
            DebugWindow.reportf(DebugWindow.STAGE, "HUD.SpellMenu.show:"+i,"Player spell: %1$s", player.getSpells().get(i).getName());
            menuLayer.inscribeString(player.getSpells().get(i).getName(), 0, i+1);
        }
    }

    void hide(){
        isShowing = false;
        menuLayer.clearLayer();
        cursorLayer.setVisible(false);
        drawTopBand(prevMousePos);
    }

    void drawTopBand(Coordinate mousePos){
        if (isShowing)
            drawBand(new Color(44, 41, 51), 0);
        else if (player.isInSpellMode() || isMouseOnDropdownBtn(mousePos))
            drawBand(new Color(37, 34, 43), 0);
        else
            drawBand(new Color(28, 25, 32), 0);
        menuLayer.inscribeString(player.getEquippedSpell().getName(), 0, 0, new Color(237, 235, 247));
    }

    private void drawBand(Color color, int row){
        for (int i = 0; i < width; i++) {
            menuLayer.editLayer(i, row, new SpecialText(' ', Color.WHITE, color));
        }
    }

    Layer getMenuLayer(){ return menuLayer; }

    private boolean isMouseOnDropdownBtn(Coordinate screenPos){
        return screenPos != null && screenPos.getX() >= menuLayer.getX() && screenPos.getX() < menuLayer.getX() + menuLayer.getCols() && screenPos.getY() == menuLayer.getY();
    }

    boolean onMouseClick(Coordinate screenPos){
        if (isMouseOnDropdownBtn(screenPos)){
            if (screenPos.getY() == menuLayer.getY()) {
                if (isShowing)
                    hide();
                else
                    show();
                return true;
            }
        }
        Spell atCursor = getSpellAtCursor(screenPos);
        if (isShowing){
            if (atCursor != null)
                player.setEquippedSpell(atCursor);
            hide();
            player.updateHUD();
            return true;
        }
        return false;
    }

    private Spell getSpellAtCursor(Coordinate screenPos){
        if (isShowing && screenPos.getX() >= menuLayer.getX() && screenPos.getX() - menuLayer.getX() < menuLayer.getCols() && screenPos.getY() > 0 && screenPos.getY() <= listLength){
            return player.getSpells().get(screenPos.getY()-1);
        }
        return null;
    }

    boolean onMouseMove(Coordinate screenPos){
        if (isMouseOnDropdownBtn(screenPos) && !mouseOverDropdownBtn){
            mouseOverDropdownBtn = true;
            drawTopBand(screenPos);
        } else if (!isMouseOnDropdownBtn(screenPos) && mouseOverDropdownBtn){
            mouseOverDropdownBtn = false;
            drawTopBand(screenPos);
        }
        if (isShowing && (prevMousePos == null || !prevMousePos.equals(screenPos))) {
            if (getSpellAtCursor(screenPos) != null){
                cursorLayer.setVisible(true);
                cursorLayer.setPos(menuLayer.getX(), screenPos.getY());
            } else {
                cursorLayer.setVisible(false);
            }
            prevMousePos = screenPos;
        }
        return isShowing;
    }
}
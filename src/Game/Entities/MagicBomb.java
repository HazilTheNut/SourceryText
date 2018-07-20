package Game.Entities;

import Data.Coordinate;
import Data.EntityStruct;
import Data.LayerImportances;
import Data.SerializationVersion;
import Engine.Layer;
import Engine.LayerManager;
import Engine.SpecialText;
import Game.GameInstance;
import Game.TagHolder;
import Game.Tags.DiggingTag;
import Game.Tags.Tag;

import java.awt.*;
import java.util.ArrayList;

public class MagicBomb extends CombatEntity {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    private int timer;
    private Color fgColor = new Color(75, 92, 189, 125);
    private Color bgColor = new Color(38, 55, 153, 50);

    private Layer explosionLayer;
    private int explosionDamage;

    private ArrayList<Integer> startingTagIds;

    @Override
    public void initialize(Coordinate pos, LayerManager lm, EntityStruct entityStruct, GameInstance gameInstance) {
        super.initialize(pos, lm, entityStruct, gameInstance);
        setMaxHealth(20);
        timer = 9;
        indicateTimer();
        startingTagIds = new ArrayList<>();
        for (Tag tag : getTags())
            startingTagIds.add(tag.getId());
    }

    public void setExplosionDamage(int explosionDamage) {
        this.explosionDamage = explosionDamage;
    }

    @Override
    public void onTurn() {
        super.onTurn();
        timer--;
        indicateTimer();
        if (timer < 1) selfDestruct();
    }

    private void indicateTimer(){
        String timerText = String.valueOf(timer);
        setIcon(new SpecialText(timerText.charAt(timerText.length()-1), fgColor, bgColor));
        updateSprite();
    }

    @Override
    public void selfDestruct() {
        super.selfDestruct();
        explode();
    }

    private void explode(){
        explosionLayer = new Layer(5, 5, "explosion:" + getUniqueID(), getLocation().getX() - 2, getLocation().getY() - 2, LayerImportances.ANIMATION);
        explosionLayer.clearLayer();
        getSprite().setVisible(false);
        gi.getLayerManager().addLayer(explosionLayer);
        turnSleep(50);
        //Inner ring
        doExplosionRing(1);
        applyDamage(explosionDamage);
        turnSleep(100);
        explosionLayer.clearLayer();
        //Outer ring
        doExplosionRing(2);
        applyDamage(explosionDamage / 2);
        turnSleep(100);
        //Cleanup
        gi.getLayerManager().removeLayer(explosionLayer);
    }

    private void doExplosionRing(int dist){
        int centerX = (explosionLayer.getCols() - 1) / 2;
        int centerY = (explosionLayer.getRows() - 1) / 2;
        SpecialText specText = new SpecialText(' ', Color.WHITE, colorateWithTags(fgColor));
        //Northern section
        explosionLayer.editLayer(centerX, centerY - dist, specText);
        explosionLayer.editLayer(centerX - 1, centerY - dist, specText);
        explosionLayer.editLayer(centerX + 1, centerY - dist, specText);
        //Southern section
        explosionLayer.editLayer(centerX, centerY + dist, specText);
        explosionLayer.editLayer(centerX - 1, centerY + dist, specText);
        explosionLayer.editLayer(centerX + 1, centerY + dist, specText);
        //Western section
        explosionLayer.editLayer(centerX - dist, centerY, specText);
        explosionLayer.editLayer(centerX - dist, centerY - 1, specText);
        explosionLayer.editLayer(centerX - dist, centerY + 1, specText);
        //Eastern section
        explosionLayer.editLayer(centerX + dist, centerY, specText);
        explosionLayer.editLayer(centerX + dist, centerY - 1, specText);
        explosionLayer.editLayer(centerX + dist, centerY + 1, specText);
    }

    private void applyDamage(int amount){
        for (int col = 0; col < explosionLayer.getCols(); col++) {
            for (int row = 0; row < explosionLayer.getRows(); row++) {
                if (explosionLayer.getSpecialText(col, row) != null) {
                    //Damage
                    Coordinate levelPos = explosionLayer.getPos().add(new Coordinate(col, row));
                    ArrayList<Entity> entities = gi.getCurrentLevel().getEntitiesAt(levelPos);
                    //Copy tags
                    for (Entity e : entities) {
                        e.onReceiveDamage(amount, this, gi);
                        transmitTags(e);
                    }
                    //Break diggable tiles
                    DiggingTag diggingTag = new DiggingTag();
                    diggingTag.dig(gi.getTileAt(levelPos), gi);
                }
            }
        }
    }

    private void transmitTags(TagHolder other){
        for (Tag tag : getTags()){
            if (!startingTagIds.contains(tag.getId()))
                other.addTag(tag.getId(), this);
        }
    }
}

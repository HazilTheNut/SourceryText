package Game;

import Data.Coordinate;
import Data.LayerImportances;
import Data.SerializationVersion;
import Engine.Layer;
import Engine.SpecialText;
import Game.Debug.DebugWindow;
import Game.Entities.Entity;
import Game.Tags.Tag;

import java.awt.*;
import java.util.ArrayList;

public class Explosion extends TagHolder{

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    private Layer explosionLayer;
    private ArrayList<Integer> transmissionBlacklist = new ArrayList<>();
    private boolean explosionBegan = false;

    public void explode(int explosionDamage, Coordinate center, GameInstance gi, Color baseColor){
        explosionBegan = true;
        explosionLayer = new Layer(5, 5, "explosion:" + System.currentTimeMillis() % 30, center.getX() - 2, center.getY() - 2, LayerImportances.ANIMATION);
        explosionLayer.clearLayer();
        gi.getLayerManager().addLayer(explosionLayer);
        turnSleep(50);
        //Inner ring
        doExplosionRing(1, baseColor);
        applyDamage(explosionDamage, gi);
        turnSleep(100);
        explosionLayer.clearLayer();
        //Outer ring
        doExplosionRing(2, baseColor);
        applyDamage(explosionDamage / 2, gi);
        turnSleep(100);
        //Cleanup
        gi.getLayerManager().removeLayer(explosionLayer);
    }

    public void addToTransmissionBlacklist(int id) {
        transmissionBlacklist.add(id);
    }


    private void doExplosionRing(int dist, Color fgColor){
        int centerX = (explosionLayer.getCols() - 1) / 2;
        int centerY = (explosionLayer.getRows() - 1) / 2;
        SpecialText specText = new SpecialText(' ', Color.WHITE, colorateWithTags(fgColor));
        if (dist < 2){
            explosionLayer.editLayer(centerX, centerY, specText);
        }
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

    private void applyDamage(int amount, GameInstance gi){
        for (int col = 0; col < explosionLayer.getCols(); col++) {
            for (int row = 0; row < explosionLayer.getRows(); row++) {
                if (explosionLayer.getSpecialText(col, row) != null) {
                    long startTime = System.nanoTime();
                    //Damage
                    Coordinate levelPos = explosionLayer.getPos().add(new Coordinate(col, row));
                    ArrayList<Entity> entities = gi.getCurrentLevel().getEntitiesAt(levelPos);
                    DebugWindow.reportf(DebugWindow.STAGE, "Explosion.applyDamage:damage", "time: %1$.03fms", (System.nanoTime() - startTime) / 1000000f);
                    //Copy tags
                    for (Entity e : entities) {
                        if (shouldContact(this, e)) {
                            e.onReceiveDamage(amount, this, gi);
                            transmitTags(e);
                        }
                    }
                    DebugWindow.reportf(DebugWindow.STAGE, "Explosion.applyDamage:copy", "time: %1$.03fms", (System.nanoTime() - startTime) / 1000000f);
                    //Contact tiles below
                    onContact(gi.getCurrentLevel().getTileAt(levelPos), gi);
                    transmitTags(gi.getCurrentLevel().getTileAt(levelPos));
                    DebugWindow.reportf(DebugWindow.STAGE, "Explosion.applyDamage:contact", "time: %1$.03fms", (System.nanoTime() - startTime) / 1000000f);
                }
            }
        }
    }

    private void transmitTags(TagHolder other){
        for (Tag tag : getTags()){
            if (!transmissionBlacklist.contains(tag.getId()))
                other.addTag(tag.copy(), this);
        }
    }

    @Override
    public void addTag(Tag tag, TagHolder source) {
        if (!hasTag(tag.getId())) {
            getTags().add(tag);
            TagEvent e = new TagEvent(0, true, source, this, null, this);
            for (int i = 0; i < getTags().size(); i++){
                getTags().get(i).onAdd(e);
            }
            e.doFutureActions();
            if (hasTag(tag.getId())) { //If the tag hasn't already been removed
                tag.onAddThis(e);
                if (e.eventPassed()) {
                    e.doCancelableActions();
                }
                //Cancelling the event will not remove the tag. Stuff like flammability shouldn't matter in this context.
            }
        }
        if (explosionBegan) transmissionBlacklist.add(tag.getId());
    }

    private void turnSleep(long millis){
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

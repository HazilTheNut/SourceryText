package Game.AnimatedTiles;

import Data.Coordinate;
import Data.SerializationVersion;
import Engine.SpecialText;

import java.awt.*;
import java.io.Serializable;

/**
 * Created by Jared on 4/22/2018.
 */
public class SandAnimation extends AnimatedTile implements Serializable {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    /**
     * SandAnimation:
     *
     * The AnimatedTile that represents the footprints entities leave behind in sandy tiles.
     */

    public SandAnimation(Coordinate loc, SpecialText seed) {
        super(loc);
        Color bg = seed.getBkgColor();
        float[] vals = new float[3];
        vals = Color.RGBtoHSB(bg.getRed(), bg.getGreen(), bg.getBlue(), vals);
        hue = vals[0];
        sat = vals[1];
        bri = vals[2];
        briPercentage = 0.5f;
    }

    private float hue;
    private float sat;
    private float bri;
    private float briPercentage;
    private float briAcceleration = 0;

    @Override
    public SpecialText onDisplayUpdate() {
        if (briPercentage < 1){
            briPercentage += briAcceleration;
            briAcceleration += 0.0025f;
            return new SpecialText('.', Color.getHSBColor(hue, sat, bri*briPercentage));
        } else {
            return null;
        }
    }
}

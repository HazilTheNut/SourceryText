package Game.AnimatedTiles;

import Data.Coordinate;
import Data.SerializationVersion;
import Engine.SpecialText;

import java.awt.*;
import java.io.Serializable;

/**
 * Created by Jared on 4/22/2018.
 */
public class ShallowWaterAnimation extends AnimatedTile implements Serializable {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    /**
     * ShallowWaterAnimation:
     *
     * The AnimatedTile that represents splashes in water as entities move through watery tiles.
     */

    public ShallowWaterAnimation(Coordinate loc, SpecialText seed) {
        super(loc);
        Color bg = seed.getBkgColor();
        float[] vals = new float[3];
        vals = Color.RGBtoHSB(bg.getRed(), bg.getGreen(), bg.getBlue(), vals);
        hue = vals[0];
        sat = vals[1];
        bri = vals[2];
    }

    private float hue;
    private float sat;
    private float bri;

    private int frame = -1;

    @Override
    public SpecialText onDisplayUpdate() {
        if (frame == -1) frame = 10;
        frame--;
        if (frame > 7)
            return new SpecialText('.', Color.getHSBColor(hue, sat*0.85f, bri + 0.25f*(1 - bri)));
        else if (frame > 4)
            return new SpecialText('o', Color.getHSBColor(hue, sat*0.90f, bri + 0.15f*(1 - bri)));
        else if (frame > 0)
            return new SpecialText('O', Color.getHSBColor(hue, sat*0.95f, bri + 0.05f*(1 - bri)));
        else
            return null;
    }
}

package Game.AnimatedTiles;

import Data.Coordinate;
import Engine.SpecialText;

import java.awt.*;

/**
 * Created by Jared on 4/22/2018.
 */
public class SandAnimation extends AnimatedTile {

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

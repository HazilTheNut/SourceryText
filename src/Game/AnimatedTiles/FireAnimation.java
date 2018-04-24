package Game.AnimatedTiles;

import Data.Coordinate;
import Engine.SpecialText;

import java.awt.*;

/**
 * Created by Jared on 4/23/2018.
 */
public class FireAnimation extends AnimatedTile {

    public FireAnimation(Coordinate loc) {
        super(loc);
        currentFrame = (loc.getX() * loc.getY()) % 23;
    }

    private final Color[] fireColors = {
        new Color(240, 181, 0), // [0]
        new Color(233, 159, 0), // [1]
        new Color(220, 121, 0), // [2]
        new Color(206, 93, 1),  // [3]
        new Color(192, 55, 0),  // [4]
        new Color(181, 35, 0)   // [5]
    };
    private final SpecialText[] frames = {
        new SpecialText('W', fireColors[0], fireColors[1]),
        new SpecialText('w', fireColors[1], fireColors[2]),
        new SpecialText('m', fireColors[2], fireColors[3]),
        new SpecialText('v', fireColors[3], fireColors[4]),
        new SpecialText('.', fireColors[4], fireColors[5])
    };
    private float currentFrame = 0;

    @Override
    public SpecialText onDisplayUpdate() {
        float framesPerSecond = 7;
        currentFrame += framesPerSecond / 20;
        int frame = (int)currentFrame;
        int hash = ((frame * 17) % 43) % frames.length;
        return frames[hash];
    }
}

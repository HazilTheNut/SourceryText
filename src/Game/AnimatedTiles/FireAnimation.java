package Game.AnimatedTiles;

import Data.Coordinate;
import Data.SerializationVersion;
import Engine.SpecialText;

import java.awt.*;
import java.io.Serializable;
import java.util.Random;

/**
 * Created by Jared on 4/23/2018.
 */
public class FireAnimation extends AnimatedTile implements Serializable {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    /**
     * FireAnimation:
     *
     * The AnimatedTile that represents fire.
     */

    public FireAnimation(Coordinate loc) {
        super(loc);
        Random rand = new Random();
        currentFrame = (int)(rand.nextFloat() * 59); //Start off on a random frame
    }

    private final Color[] fireColors = { //Frames are pre-defined
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
    private float currentFrame;

    @Override
    public SpecialText onDisplayUpdate() {
        float framesPerSecond = 7;
        currentFrame += framesPerSecond / 20; //The game's display runs at 20fps
        int frame = (int)currentFrame;
        int hash = ((frame * 17) % 43) % frames.length; //Makes the fire feel random without resorting to generating a lot of random numbers.
        return frames[hash];
    }
}

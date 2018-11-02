package Game.Tags;

import Data.SerializationVersion;

/**
 * Created by Jared on 4/1/2018.
 */
public class RangeTag extends Tag{

    /**
     * RangeTag:
     *
     * Defines the range of ranged weapons
     */

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    private int range;

    public RangeTag(int range){
        this.range = range;
        setName(String.format("Range: %1$d", range));
    }

    public int getRange() {
        return range;
    }

    public static final int RANGE_DEFAULT = 15;

    @Override
    public Tag copy() {
        return new RangeTag(range);
    }
}

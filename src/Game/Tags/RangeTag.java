package Game.Tags;

import Data.SerializationVersion;

/**
 * Created by Jared on 4/1/2018.
 */
public class RangeTag extends Tag{

    /**
     * DamageTag:
     *
     * Amplifies damage dealt by an incremental amount.
     */

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    public int range;

    public RangeTag(int range){
        this.range = range;
        setName(String.format("Range: %1$d", range));
    }

    public int getRange() {
        return range;
    }
}

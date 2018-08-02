package Game.Entities;

public class Marker extends Entity {
    /**
     * Marker:
     *
     * An Entity with no additional instructions, aside from being non-solid.
     */

    @Override
    public boolean isSolid() {
        return false;
    }
}

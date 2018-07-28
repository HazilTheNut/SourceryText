package Data;

public class LevelScriptMask {
    /**
     * LevelScriptMask:
     *
     * A 2-D matrix of booleans that act as a 'mask" for level scripts to use.
     *
     * It contains:
     * > scriptId : The LevelScript ID this mask is meant for.
     * > name   : The name of the mask
     * > mask   : The contents of the mask
     */

    private int scriptId;
    private String name;
    private boolean[][] mask;

    public boolean[][] getMask() {
        return mask;
    }

    public String getName() {
        return name;
    }

    public void setMask(boolean[][] mask) {
        this.mask = mask;
    }

    public void setName(String name) {
        this.name = name;
    }
}

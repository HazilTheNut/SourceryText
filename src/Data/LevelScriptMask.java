package Data;

import Engine.Layer;

import java.io.Serializable;

public class LevelScriptMask implements Serializable {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

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

    public LevelScriptMask(int scriptId, String name, Layer backdrop){
        this.scriptId = scriptId;
        this.name = name;
        mask = new boolean[backdrop.getCols()][backdrop.getRows()];
    }

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

    public int getScriptId() {
        return scriptId;
    }
}

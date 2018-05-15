package Data;

import java.io.Serializable;

/**
 * Created by Jared on 3/16/2018.
 */
public class EntityArg implements Serializable {

    /**
     * EntityArg:
     *
     * A pair of Strings, one denoting the name of the argument and the other denoting its value.
     *
     * For example, an EntityArg could be expressed as ["maxHealth","35"], whose
     *   Name is "maxHealth" &
     *   Value being "35"
     *   ...and together represents "This entity should have a maximum health of 35"
     */

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    private String argName;
    private String argValue;

    public EntityArg(String name, String value){
        argName = name;
        argValue = value;
    }

    public String getArgName() {
        return argName;
    }

    public String getArgValue() {
        return argValue;
    }

    public void setArgValue(String value) { argValue = value; }

    public EntityArg copy(){
       return new EntityArg(argName, argValue);
    }
}

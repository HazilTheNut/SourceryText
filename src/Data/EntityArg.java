package Data;

import java.io.Serializable;

/**
 * Created by Jared on 3/16/2018.
 */
public class EntityArg implements Serializable {

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
}

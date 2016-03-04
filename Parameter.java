import storageManager.Disk;
import storageManager.MainMemory;
import storageManager.SchemaManager;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by anderson on 12/3/15.
 */
public class Parameter {
    public HashMap<String, List<Statement>> value;
    SchemaManager schemaManager;
    MainMemory mem;
    Disk disk;

    public Parameter(HashMap<String, List<Statement>> parameter) {
        value = parameter;
    }

    public Parameter(Parameter old) {
        schemaManager = old.schemaManager;
        mem = old.mem;
    }

    public Parameter() {
        value = new HashMap<String, List<Statement>>();
    }

}

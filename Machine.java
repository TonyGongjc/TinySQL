import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by anderson on 12/3/15.
 */
public class Machine {
    protected static HashMap<String, Machine> stringMachineHashMap;
    public Parameter execute(Parameter parameter) {
        String state = parameter.value.get("INITIAL").get(0).getAttribute();
        Parameter nextParameter = new Parameter(parameter);
        HashMap<String, List<Statement>> argu = new HashMap<String, List<Statement>>();
        List<Statement> list = parameter.value.get("INITIAL").get(0).getBranches();
        argu.put(state, list);
        nextParameter.value = argu;
        if(parameter.schemaManager == null) System.out.print("FuckU!!!");
        Machine machine = stringMachineHashMap.get(state);
        //System.out.print(state);
        machine.execute(nextParameter);
        return null;
    }
}

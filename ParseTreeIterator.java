import storageManager.Disk;
import storageManager.MainMemory;
import storageManager.SchemaManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by anderson on 12/2/15.
 */
public class ParseTreeIterator {
    /**
     * More StateMachine than a Iterator
     */
    HashMap<String, Machine> stateMachineMap = new HashMap<String, Machine>();
    MainMemory mem;
    Disk disk;
    SchemaManager schema_manage;
    public ParseTreeIterator(MainMemory mem, Disk disk, SchemaManager schema_manage) {
        this.mem = mem;
        this.disk = disk;
        this.schema_manage = schema_manage;
        stateMachineMap.put("SELECT", new Select());
        stateMachineMap.put("CREATE", new Create());
        stateMachineMap.put("DROP", new Drop());
        stateMachineMap.put("INSERT", new Insert());
        stateMachineMap.put("INITIAL", new Machine());
        stateMachineMap.put("DELETE", new DELETE());
        Machine.stringMachineHashMap = stateMachineMap;
    }

    /**
     * Execute one line of command
     * Take input in AST
     */
    public void execute(Statement statement) {
        long start = System.currentTimeMillis();
        double elapsedTime = disk.getDiskTimer();
        long elapsedIO = disk.getDiskIOs();
        HashMap<String, List<Statement>> argu = new HashMap<String, List<Statement>>();
        List<Statement> list = new ArrayList<Statement>();
        list.add(statement);
        argu.put("INITIAL", list);
        Machine machine = stateMachineMap.get("INITIAL");
        Parameter parameter = new Parameter(argu);
        parameter.schemaManager = schema_manage;
        parameter.disk = disk;
        parameter.mem = mem;

        machine.execute(parameter);

        long elapsedTimeMillis = System.currentTimeMillis()-start;
        System.out.print("Computer elapse time = " + elapsedTimeMillis + " ms" + "\n");
        System.out.print("Calculated elapse time = " + (disk.getDiskTimer()-elapsedTime) + " ms" + "\n");
        System.out.println("Calculated Disk I/Os = " + (disk.getDiskIOs()-elapsedIO) + "\n");
    }

}

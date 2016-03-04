import storageManager.*;

import java.util.*;

/**
 * Created by anderson on 12/3/15.
 */
public class Insert extends Machine {
    @Override
    public Parameter execute(Parameter parameter) {
        assert parameter.schemaManager != null;
        assert parameter.mem != null;

        List<Statement> list = parameter.value.get("INSERT");
        List<String> colList;
        ArrayList<String> valueList = new ArrayList<String>();
        String relationName = null;
        Parameter col = new Parameter();
        List<Statement> cols = null;

        for (Statement subStatement: list) {
            if (subStatement.getAttribute().equalsIgnoreCase("RELATION"))
                relationName = subStatement.getBranches().get(0).getAttribute();
            else if (subStatement.getAttribute().equalsIgnoreCase("COL")) {
                cols =subStatement.getBranches();
            } else if (subStatement.getAttribute().equalsIgnoreCase("VALUES")) {
                Relation relation = parameter.schemaManager.getRelation(relationName);
                Tuple newTuple = relation.createTuple();
                assert cols != null;
                int i = 0;
                for (Statement field : cols) {
                    assert field.getAttribute().equalsIgnoreCase("COL_ID");
                    assert field.getBranches().size() == 1;
                    assert newTuple.getSchema().getFieldType(field.getBranches().get(0).getAttribute()) != null;
                    assert subStatement.getBranches().get(i).getAttribute().equalsIgnoreCase("VALUE");
                    String value = subStatement.getBranches().get(i).getBranches().get(0).getAttribute();
                    if(newTuple.getSchema().getFieldType(field.getBranches().get(0).getAttribute()).equals(FieldType.INT)) {
                        newTuple.setField(field.getBranches().get(0).getAttribute(), Integer.parseInt(value));
                    } else {
                        newTuple.setField(field.getBranches().get(0).getAttribute(), value);
                    }
                    i += 1;
                }
                appendTupleToRelation(relation, parameter.mem, 0, newTuple);
            } else if (subStatement.getAttribute().equalsIgnoreCase("SELECT")) {
                /**
                 * Leave blank for case INSERT FROM SELECT
                 */
                Relation tempRelation = Api.selectHandler(parameter.schemaManager, parameter.mem, "SELECT * FROM course", 1);
                String[] tp = {"sid","homework","project","exam","grade"};
                ArrayList<String> tempList = new ArrayList<>(Arrays.asList(tp));
                Api.insertFromSel(parameter.schemaManager, parameter.mem, parameter.schemaManager.getRelation(relationName), tempList,tempRelation);
            }
        }
        System.out.println("INSERT COMPLETE");
        //col.value.put("COL", parameter.value.get())
        //colList = super.stringMachineHashMap.get("COL").execute()

        return null;
    }

    private static void appendTupleToRelation(Relation relation_reference, MainMemory mem, int memory_block_index, Tuple tuple) {
        Block block_reference;
        if (relation_reference.getNumOfBlocks()==0) {
            // System.out.print("The relation is empty" + "\n");
            // System.out.print("Get the handle to the memory block " + memory_block_index + " and clear it" + "\n");
            block_reference=mem.getBlock(memory_block_index);
            block_reference.clear(); //clear the block
            block_reference.appendTuple(tuple); // append the tuple
            relation_reference.setBlock(relation_reference.getNumOfBlocks(),memory_block_index);
        } else {
            relation_reference.getBlock(relation_reference.getNumOfBlocks()-1,memory_block_index);
            block_reference=mem.getBlock(memory_block_index);
            if (block_reference.isFull()) {
                // System.out.print("(The block is full: Clear the memory block and append the tuple)" + "\n");
                block_reference.clear(); //clear the block
                block_reference.appendTuple(tuple); // append the tuple
                // System.out.print("Write to a new block at the end of the relation" + "\n");
                relation_reference.setBlock(relation_reference.getNumOfBlocks(),memory_block_index); //write back to the relation
            } else {
                // System.out.print("(The block is not full: Append it directly)" + "\n");
                block_reference.appendTuple(tuple); // append the tuple
                // System.out.print("Write to the last block of the relation" + "\n");
                relation_reference.setBlock(relation_reference.getNumOfBlocks()-1,memory_block_index); //write back to the relation
            }
        }
    }
}

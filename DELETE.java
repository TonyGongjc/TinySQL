import storageManager.Relation;
import storageManager.Tuple;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by anderson on 12/7/15.
 */
public class DELETE extends Machine{
    public Parameter execute(Parameter parameter) {
        List<Statement> argeList = parameter.value.get("DELETE");
        assert argeList != null;

        Statement relation = null;
        Statement exp = null;
        for (Statement argument : argeList) {
            if (argument.getAttribute().equalsIgnoreCase("RELATION")) relation = argument;
            if (argument.getAttribute().equalsIgnoreCase("EXPRESSION")) exp = argument;
        }

            assert relation != null;
            if (exp == null) {
                Relation rela = parameter.schemaManager.getRelation(relation.getBranches().get(0).getAttribute());
                rela.deleteBlocks(0);
            } else {
                System.out.println("Reach here\n");
                Relation rela = parameter.schemaManager.getRelation(relation.getBranches().get(0).getAttribute());
                Expression expr = new Expression(exp);
                //Test.traversal(expr.statement, 0);
                int blocks = rela.getNumOfBlocks();
                for (int i = 0; i < blocks; i++) {
                    boolean modified = false;
                    rela.getBlock(i, 0);
                    ArrayList<Tuple> tuples = parameter.mem.getBlock(0).getTuples();
                    for (int j = 0; j < tuples.size(); j++) {
                        if (expr.evaluateBoolean(tuples.get(j))) {
                            //System.out.println(tuples.get(j));
                            parameter.mem.getBlock(0).invalidateTuple(j);
                            modified = true;
                        }
                    }
                    if (modified) {
                        rela.setBlock(i, 0);
                    }
                }
            }
        return null;
    }
}

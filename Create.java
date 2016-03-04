import storageManager.Field;
import storageManager.FieldType;
import storageManager.Relation;
import storageManager.Schema;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by anderson on 12/3/15.
 */
public class Create extends Machine {
    @Override
    public Parameter execute(Parameter parameter) {
        ArrayList<String> fieldName = new ArrayList<String>();
        ArrayList<FieldType> fieldType = new ArrayList<FieldType>();
        Statement relation = parameter.value.get("CREATE").get(0);
        assert relation.getAttribute().equalsIgnoreCase("RELATION");
        String relationName = relation.getBranches().get(0).getAttribute();

        List<Statement> col_details = parameter.value.get("CREATE").get(1).getBranches();
        for (Statement statement: col_details) {
            assert statement.getAttribute().equalsIgnoreCase("COL_DETAIL");
            fieldName.add(statement.getBranches().get(0).getBranches().get(0).getAttribute());
            String type = statement.getBranches().get(1).getBranches().get(0).getAttribute();
            if(type.equals("INT")) {
                fieldType.add(FieldType.INT);
            } else if (type.equals("STR20")) {
                fieldType.add(FieldType.STR20);
            } else {
                assert false;
            }
        }
        Schema schema = new Schema(fieldName, fieldType);
        if(parameter.schemaManager == null) System.out.println("Fuck!!!");
        Relation newRelation = parameter.schemaManager.createRelation(relationName, schema);
        System.out.println("CREATE: successfully created relation "+relationName);
        return null;
    }
}

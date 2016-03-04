import java.util.List;

/**
 * Created by anderson on 12/3/15.
 */
public class Drop extends Machine {
    @Override
    public Parameter execute(Parameter parameter) {
        List<Statement> arguList = parameter.value.get("DROP");
        assert arguList != null;
        String relationName = arguList.get(0).getBranches().get(0).getAttribute();
        parameter.schemaManager.deleteRelation(relationName);
        System.out.println("DROP: Successfully drop relation "+relationName+"\n");
        return null;
    }
}

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Created by anderson on 12/1/15.
 *
 */
public class Statement {
    private String attribute;
    private List<Statement> branches;
    HashSet<String> containsTable;
    public Statement(String attribute) {
        this.attribute = attribute;
        branches = new ArrayList<Statement>();
    }

    public Statement(String value, boolean isEnd) {
        this.attribute = value;
        branches = null;
    }

    public void setBranches(List<Statement> branches) {
        this.branches = branches;
    }

    public String getAttribute() {
        return attribute;
    }

    public List<Statement> getBranches() {
        return branches;
    }
}

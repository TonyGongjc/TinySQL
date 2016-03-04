import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by anderson on 12/6/15.
 */
class CrossRelation {
    Set<String> subRelation;
    int tupleNum;
    int fieldNum;
    int blockNum;
    int cost = Integer.MAX_VALUE;
    @Override
    public int hashCode() {
        return subRelation.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return subRelation.equals(obj);
    }

    public CrossRelation(Set<String> s, int block, int tuple) {
        this.blockNum = block;
        this.tupleNum = tuple;
        subRelation = new HashSet<>(s);
        //joinBy = new ArrayList<>();
    }
    public List<CrossRelation> joinBy;
}
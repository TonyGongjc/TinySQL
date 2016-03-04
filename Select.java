import storageManager.*;

import java.lang.reflect.Array;
import java.util.*;

/**
 * Created by anderson on 12/3/15.
 * I would't deny that there is extra memory use in SELECT function.
 * But the extra memory are reduced to O(n) where n is the size of main memory
 *
 */
public class Select extends Machine {
    @Override
    public Parameter execute(Parameter parameter) {
        HashMap<String, List<Statement>> map = new HashMap<String, List<Statement>>();
        Statement statement = parameter.value.get("SELECT").get(0);
        Statement col = null, from = null, order = null;
        Expression expr = null;
        SchemaManager schemaManager = parameter.schemaManager;
        MainMemory mem = parameter.mem;

        for (Statement s: parameter.value.get("SELECT")) {
            //Test.traversal(s, 0);
            if(s.getAttribute().equalsIgnoreCase("COL")) col = s;
            if(s.getAttribute().equalsIgnoreCase("FROM")) from = s;
            if(s.getAttribute().equalsIgnoreCase("EXPRESSION")) expr = new Expression(s);
            if(s.getAttribute().equalsIgnoreCase("ORDER")) order = s;
        }

        assert from != null;
        assert col != null;

        /**
         * This is a patch
         */
        Statement cols = col;
        if(cols.getBranches().get(0).getAttribute().equalsIgnoreCase("DISTINCT")) {
            cols = cols.getBranches().get(0);
        }
        ArrayList<String> fieldList = new ArrayList<>();
        for (Statement s: cols.getBranches()) {
            assert s.getAttribute().equals("COL_ID");
            StringBuilder fieldName = new StringBuilder();
            for (Statement subField: s.getBranches()) {
                fieldName.append(subField.getAttribute()+".");
            }
            fieldName.deleteCharAt(fieldName.length()-1);
            fieldList.add(fieldName.toString());
        }
        /**
         * End of patch
         */


        /**
         * This Part is for select from single Relation.
         */
        if (from.getBranches().size() == 1) {
            // select from single table
            assert from.getBranches().get(0).getAttribute().equalsIgnoreCase("RELATION");
            String relationName = from.getBranches().get(0).getBranches().get(0).getAttribute();
            Relation relation = schemaManager.getRelation(relationName);
            /**
             * This part is for the case that all table could be fit in main memory.
             */
            if(relation.getNumOfBlocks() <= mem.getMemorySize()) {
                // All blocks of the relation could be fit into main memory
                relation.getBlocks(0,0,relation.getNumOfBlocks());
                ArrayList<Tuple> tuples;
                tuples = mem.getTuples(0, relation.getNumOfBlocks());
                if (expr != null) {
                    ArrayList<Tuple> where = new ArrayList<>();
                    for (Tuple tuple: tuples) {
                        if(expr.evaluateBoolean(tuple)) {
                            where.add(tuple);
                        }
                    }
                    tuples = where;
                }

                if (order != null || col.getBranches().get(0).getAttribute().equalsIgnoreCase("DISTINCT")) {
                    //Make them in order
                    if(tuples.size() == 0) {
                        System.out.println("Empty Table");
                        return null;
                    }
                    Algorithms.sortInMemory(tuples, order==null?null:order.getBranches().get(0).getBranches().get(0).getAttribute());
                    //if(order == null) System.out.println("Fuck");
                    if(col.getBranches().get(0).getAttribute().equalsIgnoreCase("DISTINCT")) {
                        Algorithms.removeDuplicate(tuples, fieldList);
                    }
                }
                System.out.println("========================");
                if (col.getBranches().get(0).getBranches().get(0).getAttribute().equals("*")) {
                    //System.out.println(tuples.get(0).toString(true));
                    try {
                        for (String s : tuples.get(0).getSchema().getFieldNames()) {
                            System.out.print(s + "  ");
                        }
                        System.out.println();

                        for (Tuple t : tuples) {
                            System.out.println(t);
                        }
                    }catch (Exception exp) {
                        System.out.println("No tuples");
                    }
                } else {
                    for (Statement field: col.getBranches()) {
                        System.out.print(field.getBranches().get(0).getAttribute() + "  ");
                    }
                    System.out.println();
                    for (Tuple t: tuples) {
                        for (Statement field: col.getBranches()) {
                            if (t.getSchema().getFieldType(field.getBranches().get(0).getAttribute())==FieldType.INT) {
                                System.out.print(t.getField(field.getBranches().get(0).getAttribute()).integer + "   ");
                            } else {
                                System.out.print(t.getField(field.getBranches().get(0).getAttribute()).str + "   ");
                            }
                        }
                        System.out.println();
                    }
                }
                System.out.println("========================");
            }
            /**
             * The whole table could not fit in main memory.
             */
            else {
                ArrayList<String> fields = new ArrayList<>();
                boolean distinct = false;
                if (col.getBranches().get(0).getAttribute().equalsIgnoreCase("DISTINCT")) {
                    distinct = true;
                    col = col.getBranches().get(0);
                }
                for (Statement field: col.getBranches()) {
                    fields.add(field.getBranches().get(0).getAttribute());
                }
                if (order == null && !distinct) {
                    // basic select operation with/without where.
                    System.out.println("Basic select operation with/without where");
                    basicSelect(mem, schemaManager, relationName, fields, expr);
                } else {
                    //select from one table in order/distinct
                    System.out.println("Select from one table in order/distinct");
                    String orderField = order == null? null:order.getBranches().get(0).getBranches().get(0).getAttribute();
                    advancedSelect(mem, schemaManager, relationName, fields, expr, orderField, distinct);
                }
            }
            /**
             * Single Relation DONE!
             */
        }
        /**
         *  This part is for select from multi-Relation
         */
        else {
            boolean distinct = false;
            if(col.getBranches().get(0).getAttribute().equalsIgnoreCase("DISTINCT")) {
                distinct = true;
                col.setBranches(col.getBranches().get(0).getBranches());
            }

            if(expr != null && expr.statement.getBranches().get(0).getAttribute().equals("=")) {
                Statement eqs = expr.statement.getBranches().get(0);
                if(eqs.getBranches().get(0).getAttribute().equalsIgnoreCase("COL_ID")
                        &&eqs.getBranches().get(1).getAttribute().equalsIgnoreCase("COL_ID")) {
                    String table1 = eqs.getBranches().get(0).getBranches().get(0).getAttribute();
                    String table2 = eqs.getBranches().get(1).getBranches().get(0).getAttribute();
                    String field0 = eqs.getBranches().get(0).getBranches().get(1).getAttribute();
                    String field1 = eqs.getBranches().get(1).getBranches().get(1).getAttribute();
                    if(field0.equals(field1)) {
                        System.out.println("Natural join optimization is applied");
                        Relation r = Api.executeNaturalJoin(schemaManager, mem, table1, table2, field0, 1);
                        Algorithms.mergeField(expr.statement);
                        Algorithms.mergeField(col);
                        ArrayList<String> fields = new ArrayList<>();

                        for (Statement ids: col.getBranches()) {
                            fields.add(ids.getBranches().get(0).getAttribute());
                        }
                        if(!distinct && order == null) {
                            Api.filter(schemaManager, mem, r, expr, fields, 0);
                            return null;
                        }
                        Relation ra = Api.filter(schemaManager, mem, r, expr, fields, 1);
                        if(distinct && order == null) {
                            if (fields.get(0).equals("*")) {
                                fields = ra.getSchema().getFieldNames();
                            }
                            Api.executeDistinct(schemaManager, mem, ra, fields, 0);
                            return null;
                        }
                        if(!distinct && order!=null) {
                            Algorithms.mergeField(order);
                            ArrayList<String> orderField = new ArrayList<>();
                            orderField.add(order.getBranches().get(0).getBranches().get(0).getAttribute());
                            Api.executeOrder(schemaManager, mem, ra, orderField,0);
                            return null;
                        }
                        if(distinct && order != null) {
                            if (fields.get(0).equals("*")) {
                                fields = ra.getSchema().getFieldNames();
                            }
                            Algorithms.mergeField(order);
                            ArrayList<String> orderField = new ArrayList<>();
                            orderField.add(order.getBranches().get(0).getBranches().get(0).getAttribute());
                            Api.executeOrder(schemaManager, mem, Api.executeDistinct(schemaManager, mem, ra, fields, 1), orderField,0);
                            return null;
                        }

                        return null;
                    }
                }
            }

            System.out.println("Execute Select in multi-relation");
                /**
                 * This is the part that cannot apply natural join while may have to chance to optimize by
                 * change join order.
                 */
                //System.out.println("Execute Select in multi-relation2");



                System.out.println("No natural join optimization for this command");
                ArrayList<String> relationList = new ArrayList<>();
                for (Statement relation: from.getBranches()) {
                    assert relation.getAttribute().equalsIgnoreCase("RELATION");
                    relationList.add(relation.getBranches().get(0).getAttribute());
                }
                System.out.println(relationList);
                if (!distinct&&order==null&&col.getBranches().get(0).getBranches().get(0).getAttribute().equals("*")&&expr==null) {
                    //System.out.println("reach here");
                    MultiRelationCrossJoin(schemaManager, mem, relationList, 0);
                    return null;
                }
                Relation relationAfterCross = MultiRelationCrossJoin(schemaManager, mem, relationList, 1);


                // order, distinct, where
            Algorithms.mergeField(col);

            ArrayList<String> fields = new ArrayList<>();
                // distinct and order doesn't support *
                for (Statement ids: col.getBranches()) {
                    fields.add(ids.getBranches().get(0).getAttribute());
                }

                if(expr != null) {
                    Algorithms.mergeField(expr.statement);
                    if(!distinct&&order==null) {
                        Api.filter(schemaManager, mem, relationAfterCross, expr, fields, 0);
                        return null;
                    }else {
                        relationAfterCross = Api.filter(schemaManager, mem, relationAfterCross, expr, fields, 1);
                    }
                }

                // if(expr == null && )

                if(distinct) {
                    if (fields.get(0).equals("*")) {
                        fields = relationAfterCross.getSchema().getFieldNames();
                    }
                    if(order == null) {
                        Api.executeDistinct(schemaManager, mem, relationAfterCross, fields, 0);
                        return null;
                    } else {
                        relationAfterCross = Api.executeDistinct(schemaManager, mem, relationAfterCross, fields, 1);
                    }
                }

                if(order != null) {
                    fields = new ArrayList<>();
                    fields.add(order.getBranches().get(0).getBranches().get(0).getAttribute());
                    Api.executeOrder(schemaManager, mem, relationAfterCross, fields, 0);
                    return null;
                }

                if (expr == null && !fields.get(0).equals("*")) {
                    int total = relationAfterCross.getNumOfBlocks();
                    for (int i = 0; i < total; i++) {
                        relationAfterCross.getBlock(i, 0);
                        ArrayList<Tuple> tuples = mem.getBlock(0).getTuples();
                        for (Tuple tp: tuples) {
                            for(String f: fields){
                                System.out.print(tp.getField(f).toString() +"  ");
                            }
                            System.out.println();
                        }
                    }
                }
        }

        return null;
    }


    public static Relation MultiRelationCrossJoin(SchemaManager schemaManager, MainMemory mem, ArrayList<String> relationName, int mode) {
        //cross join plan
        int memsize = mem.getMemorySize();
        if (relationName.size() == 2) {
            /**
             * This is the part that two table natural join.
             */
            return Api.executeCrossJoin(schemaManager, mem, relationName, mode);
        } else {
            //run a DP algorithm to determine the order of join.
            HashMap<Set<String> ,CrossRelation> singleRelation = new HashMap<>();
            for (String name: relationName) {
                HashSet<String> set = new HashSet<>();
                set.add(name);
                Relation relation = schemaManager.getRelation(name);
                CrossRelation temp = new CrossRelation(set, relation.getNumOfBlocks(), relation.getNumOfTuples());
                temp.cost = relation.getNumOfBlocks();
                temp.fieldNum = relation.getSchema().getNumOfFields();
                singleRelation.put(set, temp);
            }
            //List of HashMap should be DP table
            List<HashMap<Set<String> ,CrossRelation>> costRelationList = new ArrayList<>();
            costRelationList.add(singleRelation);
            for (int i = 1; i < relationName.size(); i++) {
                costRelationList.add(new HashMap<Set<String> ,CrossRelation>());
            }

            Set<String> finalGoal = new HashSet<>(relationName);
            CrossRelation cr = Algorithms.findOptimal(costRelationList, finalGoal, memsize);
            Algorithms.travesal(cr, 0);
            if (mode == 0) {
                helper(cr, mem, schemaManager, 0);
            } else {
                return helper(cr, mem, schemaManager, 1);
            }

            /**
             * A lot to be done
             */
            return null;
        }
    }
    public static Relation helper(CrossRelation cr, MainMemory mem, SchemaManager schemaManager, int mode) {
        //mode 0 display, mode 1 output
        if(cr.joinBy == null||cr.joinBy.size()<2) {
            List<String> relation = new ArrayList<>(cr.subRelation);
            assert relation.size() == 1;
            return schemaManager.getRelation(relation.get(0));
        } else {
            assert cr.joinBy.size() == 2;
            if(mode == 0) {
                String subRelation1 = helper(cr.joinBy.get(0), mem, schemaManager, 1).getRelationName();
                String subRelation2 = helper(cr.joinBy.get(1), mem, schemaManager, 1).getRelationName();
                ArrayList<String> relationName = new ArrayList<>();
                relationName.add(subRelation1);
                relationName.add(subRelation2);
                return Api.executeCrossJoin(schemaManager, mem, relationName, 0);
            } else {
                String subRelation1 = helper(cr.joinBy.get(0), mem, schemaManager, 1).getRelationName();
                String subRelation2 = helper(cr.joinBy.get(1), mem, schemaManager, 1).getRelationName();
                ArrayList<String> relationName = new ArrayList<>();
                relationName.add(subRelation1);
                relationName.add(subRelation2);
                /*
                System.out.println(subRelation1);
                System.out.println(subRelation2);
                System.out.println("-------------");
                System.out.println(schemaManager.getRelation(subRelation1).getSchema().getFieldNames());
                System.out.println("-------------");
                System.out.println(schemaManager.getRelation(subRelation2).getSchema().getFieldNames());
                System.out.println("-------------");
                */
                return Api.executeCrossJoin(schemaManager, mem, relationName, 1);
            }
        }
    }


    private void print(Tuple tuple, List<String> fieldList) {
        if (fieldList.get(0).equals("*")) {
            System.out.println(tuple);
            return;
        }
        for (String field: fieldList) {
            System.out.print((tuple.getSchema().getFieldType(field)==FieldType.INT?
                    tuple.getField(field).integer:tuple.getField(field).str) + "   ");
        }
        System.out.println();
    }

    private void printTitle(Tuple tuple, List<String> fieldList) {
        if (fieldList.get(0).equals("*")) {
            for (String fieldNames: tuple.getSchema().getFieldNames()) {
                System.out.print(fieldNames + "   ");
            }
            System.out.println();
        }
        else {
            for (String str: fieldList) {
                System.out.print(str + "    ");
            }
            System.out.println();
        }
    }

    public void advancedSelect(MainMemory mem, SchemaManager schemaManager, String relationName,
                               ArrayList<String> field, Expression exp, String orderBy, boolean distinct) {
        Relation relation = schemaManager.getRelation(relationName);
        /**
         * If where condition exists, apply where and generate a new relation
         */
        if (exp != null) {
            Schema schema = relation.getSchema();
            Relation tempRelation = schemaManager.createRelation(relationName+"temp", schema);
            int tempRelationCurrentBlock = 0;
            Block tempBlock = mem.getBlock(1);
            tempBlock.clear();
            int count = 0;
            for (int i = 0; i < relation.getNumOfBlocks(); i++) {
                relation.getBlock(i, 0);
                ArrayList<Tuple> tupes = mem.getBlock(0).getTuples();
                for (Tuple tupe: tupes) {
                    if(exp.evaluateBoolean(tupe)) {
                        if(!tempBlock.isFull()) tempBlock.appendTuple(tupe);
                        else {
                            mem.setBlock(1, tempBlock);
                            tempRelation.setBlock(tempRelationCurrentBlock, 1);
                            tempRelationCurrentBlock += 1;
                            tempBlock.clear();
                            tempBlock.appendTuple(tupe);
                        }
                    } /*else {
                        System.out.print("Dumped   ");
                        System.out.println(tupe);
                    }*/
                }
            }

            if(!tempBlock.isEmpty()) {
                //System.out.println("reachHere");
                mem.setBlock(1, tempBlock);
                tempRelation.setBlock(tempRelationCurrentBlock, 1);
                tempBlock.clear();
            }
            relation = tempRelation;
        }

        System.out.println("******" + relation.getNumOfTuples() + "*******");
        /**
         * This part ends here
         */

        if(relation.getNumOfBlocks() <= mem.getMemorySize()) {
            relation.getBlocks(0, 0, relation.getNumOfBlocks());
            ArrayList<Tuple> tuples = mem.getTuples(0, relation.getNumOfBlocks());
            Algorithms.sortInMemory(tuples, orderBy);
            if(distinct) {
                Algorithms.removeDuplicate(tuples, field);
            }
            printTitle(tuples.get(0), field);
            for (Tuple tuple: tuples) {
                print(tuple, field);
            }
        } else {
            System.out.println("Two pass condition");
            ArrayList<String> order = new ArrayList<>();
            if(orderBy != null) {
                order.add(orderBy);
            }
            if(field.get(0).equals("*")) {
                field = relation.getSchema().getFieldNames();
            }
            if(distinct && orderBy!=null) {
                relation = Api.executeDistinct(schemaManager, mem, relation, field, 1);
                Api.executeOrder( schemaManager, mem, relation, order, 0);
            }else if (distinct) {
                Api.executeDistinct(schemaManager, mem, relation, field, 0);
            }else if (orderBy != null) {
                Api.executeOrder(schemaManager, mem, relation, order, 0);
            }
        }
    }

    private Relation basicSelect(MainMemory mem, SchemaManager schemaManager, String relationName, List<String> field, Expression exp) {
        int currentBlockCount = 0;
        Relation relation = schemaManager.getRelation(relationName);
        boolean show = false;
        while (currentBlockCount < relation.getNumOfBlocks()) {
            int readBlocks = relation.getNumOfBlocks()-currentBlockCount > mem.getMemorySize()?
                    mem.getMemorySize(): relation.getNumOfBlocks()-currentBlockCount;
            relation.getBlocks(currentBlockCount, 0, readBlocks);
            ArrayList<Tuple> tuples = mem.getTuples(0, readBlocks);
            if(!show) {
                show = true;
                if(field.get(0).equals("*")) {
                    for (String fieldNames: tuples.get(0).getSchema().getFieldNames()) {
                        System.out.print(fieldNames + "   ");
                    }
                    System.out.println();
                }
                else {
                    for (String name: field) System.out.print(name + "  ");
                    System.out.println();
                }
            }
            for(Tuple tuple: tuples) {
                if(exp == null) print(tuple, field);
                else {
                    if (exp.evaluateBoolean(tuple)) print(tuple, field);
                }
            }
            currentBlockCount += readBlocks;
        }
        return null;
    }

}

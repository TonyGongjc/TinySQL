import storageManager.FieldType;
import storageManager.Tuple;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

/**
 * Created by anderson on 12/3/15.
 */
public class Expression {
    class Temp {
        String type;
        String tempString;
        int tempInteger;
        public boolean equals(Temp t2) {
            if (!this.type.equalsIgnoreCase(t2.type)) return false;
            if(this.type.equals("INT")) {
                return this.tempInteger == t2.tempInteger;
            } else {
                return this.tempString.equals(t2.tempString);
            }
        }
    }

    Statement statement;
    public Expression(Statement statement) {
        this.statement = statement;
    }

    public boolean evaluateBoolean(Tuple tuple) {
        switch (statement.getAttribute()) {
            case "EXPRESSION":
                return new Expression(statement.getBranches().get(0)).evaluateBoolean(tuple);
            case "AND": {
                /**
                 * This is the first version, still lot of things to be done <>Push selection done</>
                 */
                return new Expression(statement.getBranches().get(0)).evaluateBoolean(tuple)
                        &&new Expression(statement.getBranches().get(1)).evaluateBoolean(tuple);
            }
            case "OR": {
                return new Expression(statement.getBranches().get(0)).evaluateBoolean(tuple)
                        ||new Expression(statement.getBranches().get(1)).evaluateBoolean(tuple);
            }
            case "=": {
                Expression left = new Expression(statement.getBranches().get(0));
                Expression right = new Expression(statement.getBranches().get(1));
                return left.evaluateUnknown(tuple).equals(right.evaluateUnknown(tuple));
            }
            case ">": {
                return new Expression(statement.getBranches().get(0)).evaluateInt(tuple)
                        >new Expression(statement.getBranches().get(1)).evaluateInt(tuple);
            }
            case "<": {
                return new Expression(statement.getBranches().get(0)).evaluateInt(tuple)
                        <new Expression(statement.getBranches().get(1)).evaluateInt(tuple);
            }
            case "NOT": {
                return !new Expression(statement.getBranches().get(0)).evaluateBoolean(tuple);
            }
            default: try {
                throw new Exception("Unknown Operator");
            }catch (Exception err) {
                err.printStackTrace();
            }
        }
        return false;
    }

    public int evaluateInt(Tuple tuple) {
        switch (statement.getAttribute()) {
            case "+": {
                return new Expression(statement.getBranches().get(0)).evaluateInt(tuple)
                        + new Expression(statement.getBranches().get(1)).evaluateInt(tuple);
            }
            case "-": {
                return new Expression(statement.getBranches().get(0)).evaluateInt(tuple)
                        - new Expression(statement.getBranches().get(1)).evaluateInt(tuple);
            }
            case "*": {
                return new Expression(statement.getBranches().get(0)).evaluateInt(tuple)
                        * new Expression(statement.getBranches().get(1)).evaluateInt(tuple);
            }
            case "/": {
                return new Expression(statement.getBranches().get(0)).evaluateInt(tuple)
                        / new Expression(statement.getBranches().get(1)).evaluateInt(tuple);
            }
            case "COL_ID": {
                StringBuilder fieldName = new StringBuilder();
                for (Statement name: statement.getBranches()) {
                    fieldName.append(name.getAttribute()+".");
                }
                fieldName.deleteCharAt(fieldName.length()-1);
                String name = fieldName.toString();
                return tuple.getField(name).integer;
            }
            case "INT": {
                return Integer.parseInt(statement.getBranches().get(0).getAttribute());
            }
        }

        return 0;
    }

    public Temp evaluateUnknown(Tuple tuple) {
        Temp temp = new Temp();
        if (statement.getAttribute().equalsIgnoreCase("STRING")) {
            temp.type = "STRING";
            temp.tempString = statement.getBranches().get(0).getAttribute();
        } else if (statement.getAttribute().equalsIgnoreCase("INT")) {
            temp.type = "INT";
            temp.tempInteger = Integer.parseInt(statement.getBranches().get(0).getAttribute());
        } else if (statement.getAttribute().equalsIgnoreCase("COL_ID")) {
            StringBuilder fieldName = new StringBuilder();
            for (Statement name: statement.getBranches()) {
                fieldName.append(name.getAttribute()+".");
            }
            fieldName.deleteCharAt(fieldName.length()-1);
            String name = fieldName.toString();
            FieldType type = tuple.getSchema().getFieldType(name);
            if (type == FieldType.INT) {
                temp.type = "INT";
                temp.tempInteger = tuple.getField(name).integer;
            } else {
                temp.type = "STRING";
                temp.tempString = tuple.getField(name).str;
            }
        } else {
            temp.type = "INT";
            temp.tempInteger = evaluateInt(tuple);
        }
        return temp;
    }

}

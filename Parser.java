import java.util.*;
/**
 * Created by anderson on 12/1/15.
 * Simple Parser for SQL Statement.
 * Parse one line of tinySQL command.
 */
public class Parser {
    HashMap<String, Integer> priority;
    public Parser() {
        priority = new HashMap<String, Integer>();
        priority.put("NOT", 2);
        priority.put("OR", 0);
        priority.put("AND",1);
        priority.put("=", 3);
        priority.put(">", 3);
        priority.put("<", 3);
        priority.put("+", 4);
        priority.put("-", 4);
        priority.put("*", 5);
        priority.put("/", 5);
    }

    /**
     * Never, ever try to read it!!!
     * It is a piece of shit!!!
     */
    public Statement parse(String command) {
        System.out.println("Now parsing command "+ command +"...");
        return parse(command.split(" "), "INITIAL");
    }

    public Statement parse(String[] command, String type) {
        Statement ret = null;
        if(type.equalsIgnoreCase("INITIAL")) {
            if(command[0].equalsIgnoreCase("SELECT")) {
                //System.out.print("here");
                ret = parse(command, "SELECT");
            }
            /**
             * Other command except SELECT NEED TO BE IMPLEMENTED
              */
            if(command[0].equalsIgnoreCase("CREATE")) {
                ret = parse(command, "CREATE");
            }
            if(command[0].equalsIgnoreCase("INSERT")) {
                ret = parse(command, "INSERT");
            }
            if(command[0].equalsIgnoreCase("DROP")) {
                ret = new Statement("DROP");
                ret.getBranches().add(leaf(command[2], "RELATION"));
            }
            if(command[0].equalsIgnoreCase("DELETE")) {
                ret = parse(command, "DELETE");
            }
        }

        if(type.equalsIgnoreCase("DELETE")) {
            Statement returnStatement = new Statement("DELETE");
            String relation = command[2];
            returnStatement.getBranches().add(leaf(relation,"RELATION"));
            if(command.length > 3 && command[3].equalsIgnoreCase("WHERE")) {
                returnStatement.getBranches().add(parse(Arrays.copyOfRange(command, 4, command.length), "WHERE"));
            }
            ret = returnStatement;
        }

        if(type.equalsIgnoreCase("INSERT")) {
            int value = 0;
            int select = 0;
            for (int i = 0; i<command.length; i++) {
                if (command[i].equals("VALUES")) value = i;
                if (command[i].equals("SELECT")) select = i;
            }
            if(value > 0) {
                Statement returnStatement = new Statement(type);
                returnStatement.getBranches().add(leaf(command[2], "RELATION"));
                returnStatement.getBranches().add(parse(Arrays.copyOfRange(command, 3, value),"COL"));
                returnStatement.getBranches().add(parse(Arrays.copyOfRange(command,value+1,command.length),"VALUES"));
                ret = returnStatement;
            }

            if(select > 0) {
                Statement returnStatement = new Statement(type);
                returnStatement.getBranches().add(leaf(command[2], "RELATION"));
                returnStatement.getBranches().add(parse(Arrays.copyOfRange(command, 3, select),"COL"));
                returnStatement.getBranches().add(parse(Arrays.copyOfRange(command,select,command.length),"VALUES"));
                ret = returnStatement;
            }
        }

        if(type.equalsIgnoreCase("VALUES")) {
            if(command[0].equalsIgnoreCase("SELECT")) {
                return parse(command, "SELECT");
            } else {
                Statement returnStatement = new Statement(type);
                for (String s: command) {
                    String t = trim(s);
                    returnStatement.getBranches().add(leaf(t, "VALUE"));
                }
                ret = returnStatement;
            }
        }

        if(type.equalsIgnoreCase("DROP")) {
            Statement returnStatement = new Statement(type);
            returnStatement.getBranches().add(leaf(command[2], "RELATION"));
            ret = returnStatement;
        }

        if(type.equalsIgnoreCase("SELECT")) {
            Statement returnStatement = new Statement(type);
            int from = 0, where = 0, orderBy = 0;
            for(int i = 1; i < command.length; i++) {
                if (command[i].equalsIgnoreCase("FROM")) from = i;
                if (command[i].equalsIgnoreCase("WHERE")) where = i;
                if (command[i].equalsIgnoreCase("ORDER")) orderBy = i;
            }
            if(from > 0) {
                Statement colStatement = parse(Arrays.copyOfRange(command,1,from),"COL");
                returnStatement.getBranches().add(colStatement);
            }

            if(where > 0) {
                Statement fromStatement = parse(Arrays.copyOfRange(command,from+1, where),"FROM");
                Statement whereStatement = parse(Arrays.copyOfRange(command, where+1,
                        orderBy>0?orderBy:command.length),"WHERE");
                returnStatement.getBranches().add(fromStatement);
                returnStatement.getBranches().add(whereStatement);
            } else {
                Statement fromStatement = parse(Arrays.copyOfRange(command,from+1,
                        orderBy>0?orderBy:command.length),"FROM");
                returnStatement.getBranches().add(fromStatement);
            }

            if(orderBy > 0) {
                //System.out.print("order" + "\n");
                Statement orderStatement = parse(Arrays.copyOfRange(command, orderBy+2, command.length), "ORDER");
                returnStatement.getBranches().add(orderStatement);
            }
            ret = returnStatement;
        }

        if(type.equalsIgnoreCase("ORDER")) {
            Statement returnStatement = new Statement("ORDER");
            returnStatement.getBranches().add(leaf(command[0], "COL_ID"));
            ret = returnStatement;
        }

        if(type.equalsIgnoreCase("COL")) {
            Statement returnStatement = new Statement(type);
            if(command[0].equalsIgnoreCase("DISTINCT")) {
                returnStatement.getBranches().add(new Statement("DISTINCT"));
                Statement col = returnStatement.getBranches().get(0);
                for(int i=1; i<command.length; i++) {
                    String s = command[i];
                    if(s.length() > 0)
                        col.getBranches().add(leaf(s.charAt(s.length()-1)==','?s.substring(0,s.length()-1):s, "COL_ID"));
                }
            } else {
                for (String s : command) {
                    if(s.length() > 0) {
                        String t = s;
                        if(t.charAt(0) == '(') t = t.substring(1, t.length());
                        if(t.charAt(t.length()-1)==')'||t.charAt(t.length()-1)==',') t = t.substring(0, t.length()-1);
                        returnStatement.getBranches().add(leaf(t, "COL_ID"));
                    }
                }
            }
            ret = returnStatement;
        }

        if(type.equalsIgnoreCase("FROM")) {
            Statement returnStatement = new Statement(type);
            for (int i=0; i< command.length; i++) {
                String s = command[i];
                if(s.charAt(s.length()-1)==',') s = s.substring(0, s.length()-1);
                returnStatement.getBranches().add(leaf(s, "RELATION"));
            }
            ret = returnStatement;
        }

        if(type.equalsIgnoreCase("WHERE")) {
            Statement returnStatement = new Statement("EXPRESSION");
            returnStatement.getBranches().add(eval(command));
            ret = returnStatement;
        }

        if(type.equalsIgnoreCase("CREATE")) {
            Statement returnStatement = new Statement(type);
            returnStatement.getBranches().add(leaf(command[2],"RELATION"));
            returnStatement.getBranches().add(parse(Arrays.copyOfRange(command,3,command.length),"CREATE_COL"));
            ret = returnStatement;
        }

        if(type.equalsIgnoreCase("CREATE_COL")) {
            Statement returnStatement = new Statement(type);
            for (int i=0; i<command.length/2; i++) {
                returnStatement.getBranches().add(parse(Arrays.copyOfRange(command, 2*i, 2*i+2),"COL_DETAIL"));
            }
            ret = returnStatement;
        }

        if(type.equalsIgnoreCase("COL_DETAIL")) {
            Statement returnStatement = new Statement(type);
            String s = command[0];
            String t = command[1];
            if(s.charAt(0)=='(') s = s.substring(1);
            if(s.charAt(s.length()-1)==','||s.charAt(s.length()-1)==')') s = s.substring(0,s.length()-1);
            if(t.charAt(t.length()-1)==','||t.charAt(t.length()-1)==')') t = t.substring(0,t.length()-1);

            returnStatement.getBranches().add(leaf(s, "COL_ID"));
            returnStatement.getBranches().add(leaf(t, "TYPE"));
            ret = returnStatement;
        }

        return ret;
    }

    public Statement leaf(String s, String type) {
        if (type.equalsIgnoreCase("COL_ID")) {
            Statement statement = new Statement("COL_ID");
            String[] id = s.split("\\.");
            for (String t : id) {
                statement.getBranches().add(new Statement(t, true));
            }
            return statement;
        } else if (type.equalsIgnoreCase("RELATION")) {
            Statement statement = new Statement("RELATION");
            statement.getBranches().add(new Statement(s, true));
            return statement;
        } else {
            Statement statement = new Statement(type);
            statement.getBranches().add(new Statement(s, true));
            return statement;
        }
    }

    public String trim(String t) {
        String s = t;
        if (s.length() == 0) return null;
        if (s.charAt(0)=='(') s = s.substring(1);
        if (s.charAt(0)=='"') s = s.substring(1);
        if (s.charAt(s.length()-1)==')') s = s.substring(0, s.length()-1);
        if (s.charAt(s.length()-1)==',') s = s.substring(0, s.length()-1);
        if (s.charAt(s.length()-1)=='"') s = s.substring(0, s.length()-1);
        return s;
    }

    public Statement eval(String[] tokens) {
        Stack<Statement> stack = new Stack<Statement>();
        int i = 0;
        while (i < tokens.length) {
            if(tokens[i].equals("NOT")) {
                stack.push(new Statement(tokens[i]));
            } else if (priority.containsKey(tokens[i])) {
                if(stack.size() >= 3) {
                    //boolean end = false;
                    Statement last = stack.pop();
                    if (priority.get(tokens[i]) >= priority.get(stack.peek().getAttribute())) {
                        stack.push(last);
                        stack.push(new Statement(tokens[i]));
                    } else {
                        while (stack.size()>0 && priority.get(stack.peek().getAttribute()) > priority.get(tokens[i])) {
                            Statement operator = stack.pop();
                            if (operator.getAttribute().equals("NOT")) {
                                operator.getBranches().add(last);
                                last = operator;
                                continue;
                            }
                            Statement anotherOperand = stack.pop();
                            operator.getBranches().add(anotherOperand);
                            operator.getBranches().add(last);
                            last = operator;
                        }
                        stack.push(last);
                        stack.push(new Statement(tokens[i]));
                    }
                } else {
                    stack.push(new Statement(tokens[i]));
                }
            } else if (isInt(tokens[i])) {
                stack.push(leaf(tokens[i], "INT"));
            } else if (tokens[i].charAt(0) == '"') {
                stack.push(leaf(tokens[i].substring(1,tokens[i].length()-1),"STRING"));
            } else if (tokens[i].equals("(")) {
                int count = 0;
                int start = i + 1;
                i += 1;
                while (count!=0||!tokens[i].equals(")")) {
                    if(tokens[i].equals("(")) count += 1;
                    if(tokens[i].equals(")")) count -= 1;
                    i += 1;
                }
                String[] tokensInClause = Arrays.copyOfRange(tokens ,start, i);
                stack.push(eval(tokensInClause));
                i += 1;
                continue;
            } else if (tokens[i].equals("[")) {
                int count = 0;
                int start = i + 1;
                i += 1;
                while (count!=0||!tokens[i].equals("]")) {
                    if(tokens[i].equals("[")) count += 1;
                    if(tokens[i].equals("]")) count -= 1;
                    i += 1;
                }
                String[] tokensInClause = Arrays.copyOfRange(tokens ,start, i);
                stack.push(eval(tokensInClause));
                i += 1;
                continue;
            }
            else {
                stack.push(leaf(tokens[i], "COL_ID"));
            }
            i++;
        }

        if(stack.size() >= 3) {
            Statement operant = stack.pop();
            while (stack.size() >= 2) {
                Statement operator = stack.pop();
                if(operator.getAttribute().equals("NOT")) {
                    operator.getBranches().add(operant);
                    operant = operator;
                    continue;
                }
                operator.getBranches().add(stack.pop());
                operator.getBranches().add(operant);
                operant = operator;
            }
            return operant;
        }else {
            return stack.peek();
        }
    }

    public static boolean isInt(String s) {
        try {
            Integer.parseInt(s);
            return true;
        }catch (NumberFormatException err) {
            return false;
        }
    }
}

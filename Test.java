import storageManager.*;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

/**
 * Created by anderson on 12/1/15.
 */
public class Test {
    public static void main(String[] args) {
        //Parser parser = new Parser();
        //traversal(parser.parse("DELETE FROM course WHERE grade = \"E\""), 0);
        //testMerge();
        //testOptimize();
        //test1();
        MainMemory mem=new MainMemory();
        Disk disk=new Disk();
        SchemaManager schema_manager=new SchemaManager(mem,disk);
        Parser parser = new Parser();
        ParseTreeIterator iterator = new ParseTreeIterator(mem, disk, schema_manager);

        Scanner in = new Scanner(System.in);
        while (true) {
            System.out.println("enter file to start a session with input file, or enter line to start a session with command line input");
            String s = in.nextLine();
            if(s.equalsIgnoreCase("Line")) {
                System.out.println("Please input command");
                iterator.execute(parser.parse(in.nextLine()));
            } else if (s.equalsIgnoreCase("File")) {
                System.out.println("Please input file name");
                String fileName = in.nextLine();
                testFinal(fileName, iterator, parser);
            }
        }
        //test1();

        //testFinal();
    }

    public static void test1(){
        System.out.println("===== Start Test =====");
        Parser parser = new Parser();
        /*
        String[] r = "INSERT INTO course (sid, homework, project, exam, grade) VALUES (16, 0, 0, 0, \"E\")".split(" ");
        String[] t = "INSERT INTO course (sid, homework, project, exam, grade) SELECT * FROM course\n".split(" ");
        String[] u = "CREATE TABLE course2 (sid INT, exam INT, grade STR20)".split(" ");
        String[] s = "SELECT * FROM course, course2 WHERE course.sid = course2.sid AND course.exam = 100 AND course2.exam = 100".split(" ");
        String v = "SELECT * FROM course WHERE grade = \"E\"";*/

        String[] str2 = {
                "CREATE TABLE t1 (c INT)",
                "CREATE TABLE t2 (c INT)",
                "CREATE TABLE t3 (c INT)",
                "CREATE TABLE t4 (c INT)",
                "CREATE TABLE t5 (c INT)",
                "CREATE TABLE t6 (c INT)",
                "INSERT INTO t1 (c) VALUES (0)",
                "INSERT INTO t1 (c) VALUES (1)",
                "INSERT INTO t1 (c) VALUES (2)",
                "INSERT INTO t1 (c) VALUES (3)",
                "INSERT INTO t1 (c) VALUES (4)",
                "INSERT INTO t2 (c) VALUES (0)",
                "INSERT INTO t2 (c) VALUES (1)",
                "INSERT INTO t2 (c) VALUES (2)",
                "INSERT INTO t2 (c) VALUES (3)",
                "INSERT INTO t3 (c) VALUES (0)",
                "INSERT INTO t3 (c) VALUES (1)",
                "INSERT INTO t3 (c) VALUES (2)",
                "INSERT INTO t3 (c) VALUES (3)",
                "INSERT INTO t4 (c) VALUES (0)",
                "INSERT INTO t4 (c) VALUES (1)",
                "INSERT INTO t5 (c) VALUES (0)",
                "INSERT INTO t5 (c) VALUES (0)",
                "INSERT INTO t6 (c) VALUES (0)"
        };

        String[] str = {
                "INSERT INTO course2 (sid, exam, grade) VALUES (5, 99, \"B\")",
                "INSERT INTO course2 (sid, exam, grade) VALUES (3, 98, \"C\")",
                "INSERT INTO course2 (sid, exam, grade) VALUES (2, 96, \"B\")",
                "INSERT INTO course2 (sid, exam, grade) VALUES (3, 98, \"E\")",
                "INSERT INTO course2 (sid, exam, grade) VALUES (2, 99, \"B\")",
                "INSERT INTO course2 (sid, exam, grade) VALUES (2, 99, \"B\")",
                "INSERT INTO course1 (sid, exam, grade) VALUES (3, 98, \"C\")",
                "INSERT INTO course1 (sid, exam, grade) VALUES (2, 96, \"B\")",
                "INSERT INTO course1 (sid, exam, grade) VALUES (3, 91, \"C\")",
                "INSERT INTO course1 (sid, exam, grade) VALUES (2, 99, \"A\")",
                "INSERT INTO course1 (sid, exam, grade) VALUES (3, 92, \"C\")",
                "INSERT INTO course1 (sid, exam, grade) VALUES (2, 99, \"D\")",
                "INSERT INTO course1 (sid, exam, grade) VALUES (3, 88, \"C\")"
        };


        MainMemory mem=new MainMemory();
        Disk disk=new Disk();
        SchemaManager schema_manager=new SchemaManager(mem,disk);
        disk.resetDiskIOs();
        disk.resetDiskTimer();

        //traversal(statement, 0);

        //Parser parser = new Parser();
        ParseTreeIterator iterator = new ParseTreeIterator(mem, disk, schema_manager);
        iterator.execute(parser.parse("CREATE TABLE course1 (sid INT, exam INT, grade STR20)"));
        iterator.execute(parser.parse("CREATE TABLE course2 (sid INT, exam INT, grade STR20)"));

        for (String s: str) {
            Statement statement = parser.parse(s);
            iterator.execute(statement);
        }

        iterator.execute(parser.parse("SELECT * FROM course1, course2 WHERE course1.sid = course2.sid"));

        //iterator.execute(parser.parse("SELECT * FROM t1, t2, t3, t4, t5, t6"));
        //iterator.execute(parser.parse("DELETE FROM t1"));
        //iterator.execute(parser.parse("SELECT * FROM t1"));
        //System.out.println(schema_manager.getRelation("t2").getNumOfBlocks());
        //iterator.execute(parser.parse("DELETE FROM t2 WHERE c = 1"));
        //iterator.execute(parser.parse("SELECT * FROM t2"));
        //iterator.execute(parser.parse("SELECT * FROM t3"));


        //iterator.execute(parser.parse("SELECT * FROM course2 WHERE sid > 0 AND grade = \"C\" AND exam = 98 ORDER BY exam"));
        //iterator.execute(statement2);
        //iterator.execute(statement);
    }

    public static void traversal(Statement statement, int level) {
        for (int i=0; i<level; i++) System.out.print("  ");
        if(statement == null) System.out.println("null");
        System.out.println(statement.getAttribute());
        /*
        if(statement.containsTable!=null) {
            for (String str: statement.containsTable) {
                System.out.print(str + " ");
            }
        }
        */
        System.out.println();
        if(statement.getBranches()!=null) {
            for (Statement sub: statement.getBranches()) {
                traversal(sub, level+1);
            }
        }
    }

    public static void test() {
        System.out.print("=======================Initialization=========================" + "\n");

        // Initialize the memory, disk and the schema manager
        MainMemory mem=new MainMemory();
        Disk disk=new Disk();
        // System.out.print("The memory contains " + mem.getMemorySize() + " blocks" + "\n");
        // System.out.print(mem + "\n" + "\n");
        SchemaManager schema_manager=new SchemaManager(mem,disk);

        disk.resetDiskIOs();
        disk.resetDiskTimer();

        // Another way to time
        long start = System.currentTimeMillis();

        List<String> commands = new ArrayList<String>();
        /**
         * add commands to list, whatever method.
         */
        Parser parser = new Parser();
        ParseTreeIterator iterator = new ParseTreeIterator(mem, disk, schema_manager);

        for(String command: commands) {
            Statement statement = parser.parse(command.split(" "), "INITIAL");
            iterator.execute(statement);
        }

    }

    public static void testMerge() {
        MainMemory mem=new MainMemory();
        Disk disk=new Disk();
        SchemaManager schema_manager=new SchemaManager(mem,disk);
        Parser parser = new Parser();
        Statement here = parser.parse("SELECT DISTINCT course.grade, course2.grade FROM course, course2 WHERE course.sid = course2.sid AND [ course.exam > course2.exam OR course.grade = \"A\" AND course2.grade = \"A\" ] ORDER BY course.exam");
        traversal(here, 0);
        Algorithms.mergeField(here);
        System.out.println("================");
        traversal(here, 0);
    }

    public static void testOptimize() {
        MainMemory mem=new MainMemory();
        Disk disk=new Disk();
        SchemaManager schema_manager=new SchemaManager(mem,disk);
        Parser parser = new Parser();
        Statement here = parser.parse("SELECT DISTINCT course.grade, course2.grade FROM course, course2 WHERE course.sid = course2.sid AND [ course.exam > course2.exam OR course.grade = \"A\" AND course2.grade = \"A\" ] ORDER BY course.exam");
        Expression expression = new Expression(here.getBranches().get(2));
        Algorithms.tableInExpression(expression.statement);
        traversal(expression.statement, 0);

    }

    public static void testFinal(String fileName, ParseTreeIterator iterator, Parser parser) {

        File file = new File(fileName);
        List<String> commandList = Api.fileReader(file);
        for (String s: commandList) {
            iterator.execute(parser.parse(s));
        }
    }
}

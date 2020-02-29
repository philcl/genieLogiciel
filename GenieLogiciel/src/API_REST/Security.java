package API_REST;

import java.util.ArrayList;

public class Security {

    /**
     * @param str string to test sql security
     * @return str if no problem else return null
     */
    public static String test(String str) {
        String test = str.toUpperCase();
        if(test.contains("SELECT") || test.contains("FROM") || test.contains("WHERE") || test.contains("DELETE") || test.contains("UPDATE") || test.contains("INSERT") || test.contains("DROP"))
            return null;
        else
            return str;
    }

    public static ArrayList<String> testArray(ArrayList<String> array) {
        String test;
        for(String str : array) {
            test = str.toUpperCase();
            if(test.contains("SELECT") || test.contains("FROM") || test.contains("WHERE") || test.contains("DELETE") || test.contains("UPDATE") || test.contains("INSERT") || test.contains("DROP"))
                return null;
        }
        return array;
    }
}

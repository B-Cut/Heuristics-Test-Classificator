package cael.uff;

import java.util.ArrayList;
import java.util.List;

public class Utils {
    public static boolean containsCaseInsensitive(String base, String query){
        return base.toLowerCase().contains(query.toLowerCase());
    }

    public static boolean containsCaseInsensitive(String base, String[] comparators){
        for (String comparator : comparators){
            if(base.toLowerCase().contains(comparator.toLowerCase())){
                return true;
            }
        }
        return false;
    }

    public static String commonPackagePath(String package1, String package2){
        if(package1.equals(package2)) return package1;



        String[] longestPath = package1.split("\\.");
        String[] shortestPath = package2.split("\\.");

        if (shortestPath.length > longestPath.length){
            String[] temp = longestPath;
            longestPath = shortestPath;
            shortestPath = temp;
        }

        List<String> commonPath = new ArrayList<String>();

        for(int i = 0; i < shortestPath.length; i++){
            if(shortestPath[i].equals(longestPath[i])){
                commonPath.add(shortestPath[i]);
            } else {
                break;
            }
        }

        return String.join(".", commonPath);
    }
}

package cael.uff;

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
}

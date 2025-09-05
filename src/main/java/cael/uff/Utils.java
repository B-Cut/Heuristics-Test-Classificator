package cael.uff;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    public static String commonPackagePath(String basePackage, String package1, String package2){
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

        String ret = String.join(".", commonPath);
        if(ret.equals(basePackage)) return "";
        return ret;
    }

    public static List<String> getM2Contents(){
        Path m2Dir = Paths.get(System.getProperty("user.home") + "/.m2");
        try (Stream<Path> stream = Files.walk(m2Dir)) {
             return stream
                    .filter(file -> !Files.isDirectory(file))
                    .map(Path::toAbsolutePath)
                    .map(Path::toString)
                    .filter(str -> str.endsWith(".jar"))
                    .collect(Collectors.toList());

        } catch (IOException e) {
            throw new RuntimeException("Could not find .m2 directory in user home", e);
        }
    }

}

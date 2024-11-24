package test.resources;
import java.util.ArrayList;

class FunctionClassifierBase {
    public ArrayList<String> methodOne(){
        ArrayList<String> list = new ArrayList<String>();
        
        list.isEmpty();
        
        return list;
    } 

    public ArrayList<String> methodTwo(String a, String b){
        ArrayList<String> list =  new ArrayList<>();
        list.add(a);
        list.add(b);
        return list;
    } 
}
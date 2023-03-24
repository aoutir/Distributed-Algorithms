package cs451;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class Test {
    public static void main(String[] args){
       /**
        * 
        System.out.println("Hello");
        Integer i1 = Integer.valueOf(1);
        Integer i2 = Integer.valueOf(1);
        System.out.println(i1.toString());
        System.out.println(i1.equals(i2)) ;

        LinkedList<Integer> list1 = new LinkedList<Integer>();
        list1.add(i1);
        list1.add(i2);

        LinkedList<Integer> list2 = new LinkedList<Integer>();
        list1.add(i1);
        list1.add(i2);

        System.out.println(list1.equals(list2)) ;

         */ 

         List<Integer> intList = Arrays.asList(1, 2, 3);
         String result = intList.stream()
           .map(n -> String.valueOf(n))
           .collect(Collectors.joining(" ", "", ""));
      
         System.out.println(result);

    }   
}

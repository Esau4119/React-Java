import java.sql.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class Demo{}

public class StreamDemo {
    public static void main(String[] args) {
        List<Demo> baseList = List.of(new Demo(), new Demo());
        System.out.println("Base List");
        System.out.println(baseList);

        //does not addect original
        List<Demo> newList = baseList.stream() // collection -> stream
                .collect(Collectors.toList()); //stream -> collection
        System.out.println("New List: ");
        System.out.println(newList);
        System.out.println(baseList == newList); // always returns new list object
        System.out.println(baseList.get(0) == newList.get(0));

        Stream<Demo> stream = baseList.stream(); // collection -> strea
        List<Demo> newCollection2 = stream.collect(Collectors.toList()); // stream -> collection

        List<Integer> mapDemo = List.of(1,2,3);
        List<Integer> output = mapDemo1(mapDemo);
        System.out.println(output);

        var myStreamDemo = new StreamDemo();
        List<Integer> output1 = mapDemo.stream()
                .map(n -> myStreamDemo.myObjIncrement(n))
                .map(n -> myIncrement(n))
                .map(n -> n+1)
                .map(StreamDemo:: myIncrement)
                .map(StreamDemo:: myObjIncrement)
                .collect(Collectors.toList());
        System.out.println(output1);
    }
    static List<Integer> mapDemo1(List<Integer> mapDemo){
        List<Integer> returnValue = new ArrayList<>();
        for(int i = 0; i < mapDemo.size(); i++){
            Integer n = mapDemo.get(i);
           // returnValue.add(n+1);
            returnValue.add(myIncrement(n));
        }
        return returnValue;
    }
    static Integer myObjIncrement (Integer n ){
        return n+1;
    }
    static Integer myIncrement (Integer n ){
        return n+1;
    }
}
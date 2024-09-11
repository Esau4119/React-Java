
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class FilterDemo {
    public static void main(String[] args) {
        var list  = List.of(1,2,3,4,5);
        var filtered1 = filter1(list);
        System.out.println(list);
        System.out.println(filtered1);

        Predicate<Integer> myPredicate = n -> n % 2 ==0;
        var filtered2 = list.parallelStream()
                .filter(myPredicate)
                .filter(i -> i % 2 ==0)
                .map(i -> {

                    if(i == 2){
                        return 100;
                    }
                    return i;
                })
                .collect(Collectors.toList());
        System.out.println(filtered2);
    }

    static List<Integer> filter1(List<Integer> nums){
        List<Integer> filterdList = new ArrayList<>();
        for(int i = 0; i < nums.size(); i++){
            Integer n = nums.get(i);
            // returnValue.add(n+1);
            if (n % 2 == 0) {

                filterdList.add(n);
            }
        }
        return filterdList;
    }
}

import java.util.List;
import java.util.stream.Collectors;

class myDemo{
    int a;

    public myDemo(int a) {
        this.a = a;
    }
    public void doStuff(){
        System.out.println(a);
    }
}

public class ForEachDemo {
    public static void main(String[] args) {
        var list = List.of(1,2,3);
        list.stream()
                .map(i -> new myDemo(i))
                .forEach(i -> i.doStuff());
        List<String> strings = list.stream()
               .map(i -> i + 1)
                .map(i -> i.toString())
               // .map(i -> i + 1)
                .collect(Collectors.toList());
        System.out.println(strings);
    }

}

import javax.sound.midi.Soundbank;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.sql.SQLOutput;


@Retention(RetentionPolicy.RUNTIME)
@interface TestRunner{}

public class CustomTester {

    public static void main(String[] args) {
        Method[] methods = CustomTester.class.getMethods();
        int totalTests = 0;
        int totalPass = 0;

        // run tests
        var testObject = new CustomTester();// does go no heap
        for(Method method: methods){
            if(method.isAnnotationPresent(TestRunner.class)){
                totalTests++;

                try{
                    method.invoke(testObject);// manually run a method with invoke
                    totalPass++;
                }catch (Exception e){
                    System.out.println( e.getCause());
                }
            }
        }

        System.out.println("Total tests: "+
                totalTests+ " total pass " + totalPass);
    }

    @TestRunner
    public void test1(){
        System.out.println("test 1  !");
        assertEquals(1,1);
    }
    @TestRunner
    public void test2(){
        System.out.println("test 2  !");
        assertEquals(1,2);
    }

    public void assertEquals(int a, int b){
        if (a != b ){
            throw new RuntimeException("Numbers do not match! Got: "
                    + a + " expected: "+b);
        }
    }
}

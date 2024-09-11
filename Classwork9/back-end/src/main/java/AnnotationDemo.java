import java.lang.reflect.Field;
@MyAnnotation
public class AnnotationDemo {
    @MyAnnotation
   public int a =1 ;
    @MyAnnotation
    public int b =2 ;

    @MyAnnotation
    public static void main(String [] args) throws IllegalAccessException {
        Field[] fields = AnnotationDemo.class.getFields();
        var demoObject = new AnnotationDemo();
        System.out.println(demoObject.a);
        for(Field field: fields){
            System.out.println(field.getName());
            System.out.println(field.getInt(demoObject));
            if(field.isAnnotationPresent(MyAnnotation.class)){
                System.out.println(field.getName()+ " has custom annotation");
            }
        }


    }
    @MyAnnotation
    public void dostuff(){
        @MyAnnotation
                int b;
    }



}

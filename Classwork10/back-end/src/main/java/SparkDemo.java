import static spark.Spark.*;

public class SparkDemo {

  public static void main(String[] args) {
    port(1234);

    get("/hello", (req, res) -> "asd");
  }
}

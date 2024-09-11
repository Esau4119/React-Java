import static spark.Spark.*;

class zzzSparkDemo {

  public static void main(String[] args) {
    port(1234);

    get("/hello", (req, res) -> "asd");
  }
}

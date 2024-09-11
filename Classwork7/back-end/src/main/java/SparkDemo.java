import com.google.gson.Gson;

import static spark.Spark.*;

// input dto
class DataTransferObject{
  String firstName;
  String lastName;
}

//output dto
class ResponseDto{
  String message;

  public ResponseDto(String message){
    this.message = message;
  }
}

public class SparkDemo {

  public static void main(String[] args) {
    port(1234);
    Gson gson = new Gson();


    get("/hello", (req, res) -> "asd");

    post("/hello", (req, res) -> "This is a post");

    get("/", (req, res) -> "This is the root path");

    path("/api", () -> {

      get("/a", (req, res) -> "A");

      get("/b", (req, res) -> "B");

      // User input
      // query params (GET Request)

      get("/queryTest", (req, res) -> {
        String name = req.queryParams("name");
        if ( name == null || name.length() == 0){
          return "hello, you forgot to add your name";
        }
        return "hello " + name;
      });

        // body (Post requests)
      post("/postTest", (req,res) -> {
        String bodyString = req.body(); // http body string
        System.out.println(bodyString);
        var dto = gson.fromJson(bodyString,DataTransferObject.class); // from string/json object to java object
        String message = "hello " + dto.firstName + " " + dto.lastName;// do any logic
        var responseDto = new ResponseDto(message);// format response
        return gson.toJson(responseDto);// return the object
      });
    });

  }
}

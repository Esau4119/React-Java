import static com.mongodb.client.model.Filters.eq;
import static spark.Spark.*;

import com.google.gson.Gson;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import java.util.HashMap;
import java.util.Map;
import org.bson.Document;

class LoginDto{
  String username;
  String password;
}

class LoginResponseDto{
  Boolean isLoggedIn;
  String error;

  public LoginResponseDto(Boolean isLoggedIn, String error) {
    this.isLoggedIn = isLoggedIn;
    this.error = error;
  }
}

public class SparkDemo {

  public static String findUser(MongoCollection<Document> a, LoginDto b){
    MongoCursor<Document> cursor = a.find().iterator();
    try{
      //iterates  validates if there is an existing username
      while(cursor.hasNext()){
        Document doc = cursor.next();
        //System.out.println(doc);
        if(doc.getString("username").equals(b.username)){
         return doc.getString("username");
        }
      }
    }finally{
      cursor.close();
    }

    return null;
  }
  public static String findPass(MongoCollection<Document> a, LoginDto b){
    MongoCursor<Document> cursor = a.find().iterator();
    try{

      //iterates through collection and validates both password and username
      while(cursor.hasNext()){
        Document doc = cursor.next();
        //System.out.println(doc);
        //System.out.println(doc.getString("password").equals(b.password) +"\t"+ doc.getString("username").equals(b.username));
        if(doc.getString("password").equals(b.password) && doc.getString("username").equals(b.username)){
          return doc.getString("password");
        }
      }
    }catch (Exception e){
      System.out.println(e);
    }
    finally{
      cursor.close();
    }
    return "";

  }

  public static void main(String[] args) {
    port(1234);

    Map<String,String> users = new HashMap<>(); // TODO remove this and use mongo collection isntead
    // open connection
    MongoClient mongoClient = new MongoClient("localhost", 27017);
    // get ref to database
    MongoDatabase db = mongoClient.getDatabase("UsersDatabase");
    // get ref to collection
    MongoCollection<Document> usersCollection = db.getCollection("usersCollection");


    Gson gson = new Gson();
    post("/logIn", (req, res) -> {
      String body = req.body();
      System.out.println(body);
      LoginDto loginDto = gson.fromJson(body, LoginDto.class);
      // TODO swap the users data store with usersCollection
      if (findUser(usersCollection,loginDto)!= null) {
        if (findPass(usersCollection,loginDto).equals(loginDto.password)) {
          System.out.println("All good");
          return gson.toJson(new LoginResponseDto(true, null));
        } else {
          System.out.println("Invalid password");
          return gson.toJson(new LoginResponseDto(false, "Invalid password"));
        }
      } else {

        // Creates new Document and adds in new user information into mongo collection
        Document document = new Document()
                .append("username", loginDto.username)
                .append("password", loginDto.password);
        usersCollection.insertOne(document);
        return gson.toJson(new LoginResponseDto(true, null));
      }
    });
  }
}

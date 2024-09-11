import static spark.Spark.*;

import com.google.gson.Gson;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;


import java.util.ArrayList;
import java.util.List;

public class SparkDemo {
  static class MessageDto{
    String userName;
    String message;

    public MessageDto(String userName,String message) {
      this.message = message;
      this.userName = userName;
    }
  }

  public static void main(String[] args) {
        // open connection
    MongoClient mongoClient = new MongoClient("localhost", 27017);
    // get ref to database
    MongoDatabase db = mongoClient.getDatabase("MyDatabase");
    // get ref to collection
    MongoCollection<Document> collection = db.getCollection("Classwork8");

    // Start server
    port(1234);
    Gson gson = new Gson();
    get("/hello", (req, res) -> "asd");
    post("/submitMessage", (req,res) -> {
      String body = req.body();
      System.out.println(body);
      MessageDto messageDto = gson.fromJson(body,MessageDto.class);
      Document document = new Document()
              .append("userName", messageDto.userName)
              .append("message", messageDto.message);
      collection.insertOne(document);
      return"Got Message";
    });

    get("/getMessages", (req, res) ->{
      List<MessageDto> allMessages = new ArrayList<>();
      MongoCursor<Document> cursor = collection.find().iterator();
      try{
        while(cursor.hasNext()){
        Document doc = cursor.next();
        allMessages.add(new MessageDto(doc.getString("userName"),doc.getString("message")));
        }
      }finally{
        cursor.close();
      }

      return gson.toJson(allMessages);
    });
  }
}

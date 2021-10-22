package nearsoft.academy.bigdata.recommendation;

import java.io.*;
import java.util.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

public class MovieRecommender {

  private int totalReviews = 0;
  private int totalProducts = 0;
  private int totalUsers = 0;

  private Hashtable<String, Integer> users = new Hashtable<>();
  private BidiMap<String, Integer> products = new DualHashBidiMap<>();

  public MovieRecommender(String moviesPath) {
    try {  

      File file = new File(moviesPath);
      FileReader fr = new FileReader(file); 
      BufferedReader br = new BufferedReader(fr); 

      File csvFile = new File("movies.csv");
      FileWriter fw = new FileWriter(csvFile);

      String line;  

      String score = null;
      String productId = null;
      String userId = null;

      while((line = br.readLine()) != null){  
        if (line.startsWith("review/score")) {
          score = line.split(" ")[1];
          totalReviews++;
        }
        else if (line.startsWith("product/productId")) {
          productId = line.split(" ")[1];
          if (!products.containsKey(productId)) {
            totalProducts++;
            products.put(productId, totalProducts);
          }
        }
        else if (line.startsWith("review/userId")) {
          userId = line.split(" ")[1];
          if (!users.containsKey(userId)) {
            totalUsers++;
            users.put(userId, totalUsers);
          }
        }
        if (productId != null && userId != null && score != null) {

          StringBuilder row = new StringBuilder();
          row.append(String.valueOf(users.get(userId)) + "," 
              + String.valueOf(products.get(productId)) + ","
              + score);
          row.append("\n");
          fw.write(row.toString());

          productId = null;
          userId = null;
          score = null;
        }
      }  
      fr.close();
      fw.close();
    }  
    catch(IOException e){  
      e.printStackTrace();  
    } 
  }

  public int getTotalReviews(){
    return totalReviews;
  }

  public int getTotalProducts(){
    return totalProducts;
  }

  public int getTotalUsers(){
    return totalUsers;
  }

  public List<String> getRecommendationsForUser(String userId) {

    List<String> recommendations = new ArrayList<>();

    try {
      File datasetFile = new File("movies.csv");
      DataModel model = new FileDataModel(datasetFile);
      UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
      UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);
      UserBasedRecommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);

      recommendations = recommender.recommend(users.get(userId), 3)
        .stream()
        .map(item -> products.getKey((int) item.getItemID()))
        .collect(Collectors.toList());
    } 
    catch (IOException | TasteException e) {
      e.printStackTrace();
    }
    return recommendations;
  }
}
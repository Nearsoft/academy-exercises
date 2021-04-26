package nearsoft.academy.bigdata.recommendation;

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

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// Algorithm:
// === Create movies.csv ===
// 1. Iterate every line of the file movies.txt.
// 2. Search for the productId in movies.txt.
//    When found, store it in a BidiMap, key: original, value: generated.
//    If the product is new, increment products counter.
// 3. Search for the userId, store in a Map, key: original, value: generated.
//    If the user is new, increment the users counter.
// 4. Search for the score, simply store in a variable.
//    Increment the scores counter.
// 5. After a productId, userId and score are found, write a single line to
//    movies.csv: userId,productId,score, where userId and productId use the
//    generated values, not the ones read from the file directly.
//
// === Get movie recommendations ===
// 6. Setup Mahout recommender as per the tutorial.
// 7. Fetch 3 recommendations from the recommender.

public class MovieRecommender {

  private final Map<String, Integer> users = new HashMap<>();
  private final BidiMap<String, Integer> products = new DualHashBidiMap<>();

  private int totalUsers = 0;
  private int totalProducts = 0;
  private int totalReviews = 0;

  private final String CSV_FILENAME = "movies.csv";

  public MovieRecommender(String moviesFilePath) {

    String productIdPrefix = "product/productId";
    String userIdPrefix = "review/userId";
    String reviewScorePrefix = "review/score";

    try(PrintWriter writer = new PrintWriter(
            CSV_FILENAME, StandardCharsets.UTF_8)) {

      Stream<String> lines = Files.lines(Paths.get(moviesFilePath));

      BufferedReader reader = new BufferedReader(
              new InputStreamReader(new FileInputStream(moviesFilePath)));
      String productId = null;
      String userId = null;
      String score = null;

      String line;
      while ((line = reader.readLine()) != null) {
        if (line.startsWith(productIdPrefix)) {
          productId = extractValue(line);

          if (!products.containsKey(productId)) {
            products.put(productId, totalProducts++);
          }
          continue;
        }
        if (line.startsWith(userIdPrefix)) {
          userId = extractValue(line);

          if (!users.containsKey(userId)) {
            users.put(userId, totalUsers++);
          }
          continue;
        }
        if (line.startsWith(reviewScorePrefix)) {
          score = extractValue(line);
          totalReviews++;
        }

        if (Objects.nonNull(productId) && Objects.nonNull(userId) && Objects.nonNull(score)) {
          writer.printf("%s,%s,%s\n", users.get(userId), products.get(productId), score);
          productId = userId = score = null;
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private String extractValue(String line) {
    return line.split("\\s")[1];
  }

  public int getTotalReviews() {
    return totalReviews;
  }

  public int getTotalProducts() {
    return totalProducts;
  }

  public int getTotalUsers() {
    return totalUsers;
  }

  public List<String> getRecommendationsForUser(String userId) {

    List<String> recommendations = new ArrayList<>();

    try {
      File datasetFile = new File(CSV_FILENAME);
      DataModel model = new FileDataModel(datasetFile);
      UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
      UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);
      UserBasedRecommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);
      recommendations = recommender.recommend(users.get(userId), 3)
              .stream()
              .map(item -> products.getKey((int) item.getItemID()))
              .collect(Collectors.toList());
    } catch (TasteException | IOException e) {
      e.printStackTrace();
    }

    return recommendations;
  }
}

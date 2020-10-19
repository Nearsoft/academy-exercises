package nearsoft.academy.bigdata.recommendation;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class MovieRecommender {

    String currentProduct = null;
    String currentUser = null;
    String currentValue = null;
    int totalMovies = 0;
    HashMap<String, Integer> users;
    HashMap<String, Integer> products;
    HashMap<Integer, String> invertedProducts;
    //Create a new csv file to push data with specified path

    public static void main(String[] args) throws IOException {
        MovieRecommender recommenderTest = new MovieRecommender("data/movies.txt");
    }

    MovieRecommender(String pathFile) throws IOException {
        users = new HashMap<String, Integer>();
        products = new HashMap<String, Integer>();
        readFile(pathFile);
    }

    private void readFile(String pathFile) throws IOException {

        String productID = "product/productId: ";
        String userID = "review/userId: ";
        String valueID = "review/score: ";
        String fileName = pathFile;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(fileName), StandardCharsets.UTF_8))) {
            String line;
            String output = "data/output.csv";
            Path path = Paths.get(output);
            try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            while ((line = br.readLine()) != null) {  //Checking if the line contains a specific string to match
                if (line.contains(productID)) {
                    totalMovies++;
                    currentProduct = line.replace(productID, ""); //B003AI2VGA
                    if (!products.containsKey(currentProduct)) {
                        products.put(currentProduct, products.size());
                        invertedProducts.put(products.size(), currentProduct);
                    }
                } else if (line.contains(userID)) {
                    currentUser = line.replace(userID, "");
                    if (!users.containsKey(currentUser)) {
                        users.put(currentUser, users.size());
//                        System.out.println(users.size());
                    }
                } else if (line.contains(valueID)) {
                    currentValue = line.replace(valueID, ""); // 5.0
//                  System.out.println(currentProduct + currentUser + currentValue);
                    writer.write(users.get(currentUser) + "," + products.get(currentProduct) + "," + currentValue + "\n");
                }
            }
            }catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getTotalReviews() {
        return totalMovies;
    }

    public int getTotalProducts() {
        return products.size();
    }

    public int getTotalUsers() {
        return users.size();
    }

    public List<String> getRecommendationsForUser(String user) {
        List<String> recommendations = new ArrayList<String>();
        int userId = users.get(user);
        try {
            DataModel model = new FileDataModel(new File("src/data/movies.csv"));
            UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
            UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);
            UserBasedRecommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);

            List<RecommendedItem> rec = recommender.recommend(userId, 3);
            for (RecommendedItem recommendation : rec) {
                recommendations.add(invertedProducts.get((int) recommendation.getItemID()));
            }
            return recommendations;

        } catch (IOException te) {
            te.printStackTrace();
        } catch (TasteException e) {
            e.printStackTrace();
        }
        return null;
    }
}

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
    int totalProducts = 0;
    int totalReviews = 0;
    int totalUsers = 0;
    int productAmount;
    int userAmount;
    HashMap<String, Integer> users;
    HashMap<String, Integer> products;
    HashMap<Integer, String> revertProducts;
    //Create a new csv file to push data with specified path

    MovieRecommender(String pathFile) throws IOException {
        users = new HashMap<String, Integer>();
        products = new HashMap<String, Integer>();
        revertProducts = new HashMap<Integer, String>();
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
                    if (line.startsWith(productID)) {
                        currentProduct = line.replace(productID, ""); //B003AI2VGA
                        totalMovies++;
                        if (!products.containsKey(currentProduct)) {
                            totalProducts++;
                            productAmount = totalProducts;
                            products.put(currentProduct, totalProducts);
                            revertProducts.put(totalProducts, currentProduct);
                        } else {
                            productAmount = products.get(currentProduct);
                        }
                    } else if (line.startsWith(userID)) {
                        currentUser = line.replace(userID, "");
                        if (!users.containsKey(currentUser)) {
                            totalUsers++;
                            userAmount = totalUsers;
                            users.put(currentUser, totalUsers);
//                        System.out.println(users.size());
                        } else {
                            userAmount = users.get(currentUser);
                        }
                    } else if (line.startsWith(valueID)) {
                        currentValue = line.replace(valueID, ""); // 5.0
//                  System.out.println(currentProduct + currentUser + currentValue);
                        writer.write(userAmount + "," + productAmount + "," + currentValue + "\n");
                        totalReviews++;
                    }
                }
                br.close();
                writer.close();
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
        List<String> recs = new ArrayList<String>();
        int userId = users.get(user);
        try {
            DataModel model = new FileDataModel(new File("data/output.csv"));
            UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
            UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);
            UserBasedRecommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);

            List<RecommendedItem> rec = recommender.recommend(userId, 3);
            for (RecommendedItem recommendation : rec) {
                recs.add(revertProducts.get((int) recommendation.getItemID()));
            }
            return recs;

        } catch (IOException te) {
            te.printStackTrace();
        } catch (TasteException e) {
            e.printStackTrace();
        }
        return null;
    }
}
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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

public class MovieRecommender {

    BufferedReader br;
    BufferedWriter bw;
    int movies = 0;
    String currentProduct;
    String currentScore;
    String currentUser;
    HashMap<String, Integer> users;
    HashMap<String, Integer> products;

    public MovieRecommender(String path) throws IOException {
        FileInputStream file = null;
        try {
            file = new FileInputStream(path);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        GZIPInputStream gzip = new GZIPInputStream(file);
        Reader reader = new InputStreamReader(gzip);
        File reviews = new File("dataset.csv");
        FileWriter fw = new FileWriter(reviews);
        br = new BufferedReader(reader);
        bw = new BufferedWriter(fw);
        users = new HashMap<>();
        products = new HashMap<>();
        readFile();
    }

    public int getTotalReviews() {
        return movies;
    }

    private void readFile() throws IOException {
        String line;

        while ((line = br.readLine()) != null) {
            if (line.matches("product\\/productId: ([\\D\\d]+)")) {
                movies++;
                String productId = line.substring(19);
                currentProduct = productId;
                if (!products.containsKey(productId))
                    products.put(productId, products.size());
            } else if (line.matches("review\\/userId: ([\\D\\d]+)")) {
                String userId = line.substring(15);
                currentUser = userId;
                if (!users.containsKey(userId))
                    users.put(userId, users.size());
            } else if (line.matches("review\\/score: ([\\D\\d]+)")) {
                String score = line.substring(14);
                currentScore = score;
                writeFile();
            }
        }
    }

    private void writeFile() throws IOException {
        if (currentScore != null && currentProduct != null && currentUser != null) {
            bw.write(users.get(currentUser) + "," + products.get(currentProduct) + "," + currentScore + "\n");
            currentScore = null;
            currentProduct = null;
            currentUser = null;
        }
    }

    public <K, V> Stream<K> keys(Map<K, V> map, V value) {
        return map
                .entrySet()
                .stream()
                .filter(entry -> value.equals(entry.getValue()))
                .map(Map.Entry::getKey);
    }

    public int getTotalUsers() { return users.size(); }

    public int getTotalProducts() { return products.size(); }

    List<String> getRecommendationsForUser(String userKey) throws IOException, TasteException {
        List<String> recsForUser = new LinkedList<>();
        DataModel model = new FileDataModel(new File("dataset.csv"));
        int userId = users.get(userKey);

        UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
        UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);
        UserBasedRecommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);
        List<RecommendedItem> recommendations = recommender.recommend(userId, 3);

        for (RecommendedItem recommendation : recommendations) {
            int productId = (int) recommendation.getItemID();
            Stream<String> productKeyStream = keys(products, productId);
            String productKey = productKeyStream.findFirst().get();
            recsForUser.add(productKey);
        }
        return recsForUser;
    }
}
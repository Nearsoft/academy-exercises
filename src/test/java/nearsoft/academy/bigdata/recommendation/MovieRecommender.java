package nearsoft.academy.bigdata.recommendation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

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

class MovieRecommender {
    String filePath;
    int totalUsers;
    int totalReviews;
    int totalProducts;
    Hashtable<String, Integer> users;
    Hashtable<String, Integer> products;
    Hashtable<Integer, String> productsById;

    MovieRecommender(String fileUrl) throws IOException {
        this.filePath = fileUrl;
        this.totalUsers = 0;
        this.totalReviews = 0;
        this.totalProducts = 0;
        this.users = new Hashtable<String, Integer>();
        this.products = new Hashtable<String, Integer>();
        this.productsById = new Hashtable<Integer, String>();

        processFile();
    }

    public void processFile() throws IOException {
        FileInputStream file = new FileInputStream(this.filePath);
        GZIPInputStream gzipInput = new GZIPInputStream(file);
        Reader decoder = new InputStreamReader(gzipInput);
        BufferedReader reader = new BufferedReader(decoder);

        Pattern usersRegex = Pattern.compile("review\\/userId: ([\\D\\d]+)");
        Pattern reviewsRegex = Pattern.compile("review\\/score: ([\\D\\d]+)");
        Pattern productsRegex = Pattern.compile("product\\/productId: ([\\D\\d]+)");

        Matcher match;
        boolean matches;
        
        String currentLine = reader.readLine();

        FileWriter writer = new FileWriter("movies.csv");

        String userId = "";
        String reviewId = "";
        String productId = "";

        while (currentLine != null) {

            match = usersRegex.matcher(currentLine);
            matches = match.matches();

            if (matches) {
                userId = currentLine.split(" ")[1];

                if (users.get(userId) == null) {
                    this.totalUsers++;
                    users.put(userId, this.totalUsers);
                }
            }

            match = reviewsRegex.matcher(currentLine);
            matches = match.matches();

            if (matches) {
                reviewId = currentLine.split(" ")[1];
                this.totalReviews++;
            }

            match = productsRegex.matcher(currentLine);
            matches = match.matches();

            if (matches) {
                productId = currentLine.split(" ")[1];

                if (products.get(productId) == null) {
                    this.totalProducts++;
                    products.put(productId, this.totalProducts);
                    productsById.put(this.totalProducts, productId);
                }
            }

            if (userId != "" && reviewId != "" && productId != "") {
                writer.write(users.get(userId) + "," + products.get(productId) + "," + reviewId + "\n");
                userId = "";
                reviewId = "";
                productId = "";
            }

            currentLine = reader.readLine();
        }

        reader.close();
        writer.close();
    }

    public int getTotalUsers() { return this.totalUsers; }

    public int getTotalReviews() { return this.totalReviews; }

    public int getTotalProducts() { return this.totalProducts; }

    public List<String> getRecommendationsForUser(String userId) throws IOException, TasteException {
        DataModel model = new FileDataModel(new File("movies.csv"));
        UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
        UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);
        UserBasedRecommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);

        List<String> recommendations = new ArrayList<String>();

        for (RecommendedItem recommendation : recommender.recommend(users.get(userId), 3)) {
            recommendations.add(productsById.get((int)(recommendation.getItemID())));
        }

        return recommendations;
    }
}
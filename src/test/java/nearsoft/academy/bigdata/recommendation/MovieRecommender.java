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
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.zip.GZIPInputStream;

public class MovieRecommender {
    // class attributes

    int totalUsers;
    int totalReviews;
    int totalProducts;

    // hash tables used for assigning a numeric value to a specific alphanumeric key

    Hashtable<String, Integer> users;
    Hashtable<Integer, String> productsById;
    Hashtable<String, Integer> products;

    String fileUrl;

    // constructor
    MovieRecommender(String path) throws IOException {
        this.fileUrl = path;

        this.totalUsers = 0;
        this.totalReviews = 0;
        this.totalProducts = 0;

        this.users = new Hashtable<String, Integer>();
        this.productsById = new Hashtable<Integer, String>();
        this.products = new Hashtable<String, Integer>();

        processData();
    }

    public int getTotalReviews() {
        return this.totalReviews;
    }

    public int getTotalProducts() {
        return this.totalProducts;
    }

    public int getTotalUsers() {
        return this.totalUsers;
    }

    public List<String> getRecommendationsForUser(String User) throws IOException, TasteException {
        DataModel model = new FileDataModel(new File("data/movies.csv"));
        UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
        UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);
        UserBasedRecommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);
        List<String> recommendations = new ArrayList<String>();

        // returns the necessary format with alphanumeric values and prints the recommendations
        // in the csv file format
        for (RecommendedItem recommendation : recommender.recommend(this.users.get(User), 3)) {
            System.out.println(recommendation);
            recommendations.add(this.productsById.get((int)(recommendation.getItemID())));
        }
        return recommendations;
    }

    private void processData() throws IOException {
        // open the compressed file and then open the text file
        InputStream fileStream = new FileInputStream(this.fileUrl); //"data/movies.txt.gz"
        InputStream gzipStream = new GZIPInputStream(fileStream);
        Reader decoder = new InputStreamReader(gzipStream);

        // open text file to loop through it
        BufferedReader br = new BufferedReader(decoder);

        // create csv file
        BufferedWriter bw = new BufferedWriter(new FileWriter("data/movies.csv"));

        String line;

        String userId = "";
        String productId = "";
        String score = "";

        // Loop through the lines in the text file and extract the necessary information
        while ((line = br.readLine()) != null) {
            if (line.contains("product/productId")) {
                productId = line.split(" ")[1]; // alphanumeric

                // check if that element is not in the hash table already
                if (this.products.get(productId) == null) {
                    this.totalProducts += 1;
                    this.productsById.put(this.totalProducts, productId);
                    this.products.put(productId, this.totalProducts);
                }
            } else if (line.contains("review/userId")) {
                userId = line.split(" ")[1];

                // check if that element is not in the hash table already
                if (this.users.get(userId) == null) {
                    this.totalUsers += 1;
                    this.users.put(userId, this.totalUsers);
                }
            } else if (line.contains("review/score")) {
                score = line.split(" ")[1];
                this.totalReviews += 1;
            }
            // If we have all required values to write the csv file
            // we proceed to write them in the required format of
            // only numbers
            if ((userId != "") && (productId != "") && (score != "")){
                bw.write(this.users.get(userId) + "," + this.products.get(productId) + "," + score + "\n");
                userId = "";
                productId = "";
                score = "";
            }
        }
        br.close();
        bw.close();
    }
}

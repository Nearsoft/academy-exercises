package nearsoft.academy.bigdata.recommendation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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


public class MovieRecommender {
    private static String DATA_PATH = System.getProperty("user.dir") + "/src/main/data/";
    private static String CSV_DATA_PATH = DATA_PATH + "/data.csv";
    private static String USERS_INDEX_PATH = DATA_PATH + "users.csv";
    private static String PRODUCTS_INDEX_PATH = DATA_PATH + "products.csv";

    private static int NUM_OF_REGISTRABLE_ITEMS = 3;

    private Map<String, Integer> products = new HashMap<String,Integer>();
    private Map<Integer, String> productsReverse = new HashMap<Integer,String>();
    private Map<String, Integer> users = new HashMap<String,Integer>();
    private String dataFilePath;
    private int totalUsers = 0;
    private int totalProducts = 0;
    private int totalReviews = 0;

    private DataModel model;
    private UserSimilarity similarity;
    private UserNeighborhood neighborhood;
    private UserBasedRecommender recommender;

    public MovieRecommender(String dataFilePath) throws IOException, TasteException {
        this.dataFilePath = dataFilePath;

        try {
            readFile();
        } catch (IOException error) {
            error.printStackTrace();
        }


        this.model = new FileDataModel(new File(CSV_DATA_PATH));
        this.similarity = new PearsonCorrelationSimilarity(this.model);
        this.neighborhood = new ThresholdUserNeighborhood(0.1, this.similarity, this.model);
        this.recommender = new GenericUserBasedRecommender(this.model, this.neighborhood, this.similarity);

        this.totalProducts = this.model.getNumItems();
        this.totalUsers = this.model.getNumUsers();
    }

    private void readFile () throws IOException {
        // Those methods allow us read the ".txt.gz" file without unzip it.
        FileInputStream file = new FileInputStream(this.dataFilePath);
        GZIPInputStream gzip = new GZIPInputStream(file);
        InputStreamReader isr = new InputStreamReader(gzip);
        BufferedReader br = new BufferedReader(isr);

        List<String> dataParts = new ArrayList<String>();
        String line;

        FileWriter fileWriter = new FileWriter(CSV_DATA_PATH); // Create a csv file

        while ((line = br.readLine()) != null) {
            String[] registrables = {"product/productId:", "review/userId:", "review/score:"}; // Indicate only the data we need (user id, movie & score)

            String[] parts = line.split(" ");

            Boolean idRequiredField = Arrays.asList(registrables).contains(parts[0]);

            if (idRequiredField) {
                dataParts.add(parts[1]);
            }

            if (dataParts.size() == NUM_OF_REGISTRABLE_ITEMS) {
                String userId = dataParts.get(1);
                String productId = dataParts.get(0);
                String score = dataParts.get(2);

                int productIdxVal = (this.products.containsKey(productId) ? this.products.get(productId) : totalProducts);
                int userIdxVal = (this.users.containsKey(userId) ? this.users.get(userId) : totalUsers);

                String mixedData = userIdxVal + "," + productIdxVal + "," + score + "\n";
                fileWriter.write(mixedData);

                this.setOccurrences(productId, userId);

                dataParts = new ArrayList<String>();
            }
        }

        fileWriter.close();
        br.close();
    }

    private void setOccurrences (String productId, String userId) {

        this.totalReviews++; // Count reviews

        // Count products
        if (!this.products.containsKey(productId)) {
            this.products.put(productId, totalProducts);
            this.productsReverse.put(totalProducts, productId);

            try {
                FileWriter fr = new FileWriter(PRODUCTS_INDEX_PATH, true);
                fr.write(productId + "," + totalProducts + "\n");
                fr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            totalProducts++;
        }


        if (!this.users.containsKey(userId)) {// Count users
            this.users.put(userId, totalUsers);

            try {
                FileWriter fr = new FileWriter(USERS_INDEX_PATH, true);
                fr.write(userId + "," + totalUsers + "\n");
                fr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            totalUsers++;
        }
    }

    private void retrieveData () throws IOException {
        this.model = new FileDataModel(new File(CSV_DATA_PATH));

        BufferedReader productReader = new BufferedReader(new FileReader(PRODUCTS_INDEX_PATH));// Read products
        String product;

        while ((product = productReader.readLine()) != null) {
            String[] productParts = product.split(",");
            String productId = productParts[0];
            int idx = Integer.parseInt(productParts[1]);

            this.products.put(productId, idx);
            this.productsReverse.put(idx, productId);
        }
        productReader.close();

        BufferedReader usersReader = new BufferedReader(new FileReader(USERS_INDEX_PATH));// Read users
        String user;

        while ((user = usersReader.readLine()) != null) {
            String[] userParts = user.split(",");
            String userId = userParts[0];
            int idx = Integer.parseInt(userParts[1]);

            this.users.put(userId, idx);
        }
        usersReader.close();

        BufferedReader reviewsReader = new BufferedReader(new FileReader(CSV_DATA_PATH));// Count reviews

        while (reviewsReader.readLine() != null) {
            this.totalReviews++;
        }

        reviewsReader.close();
    }

    public int getTotalReviews() {
        return this.totalReviews;
    }

    public int getTotalProducts () {
        return this.totalProducts;
    }

    public int getTotalUsers () {
        return this.totalUsers;
    }

    public List<String> getRecommendationsForUser (String userID) throws TasteException {
        List<String> recommendations = new ArrayList<String>();

        long user = users.get(userID);

        List<RecommendedItem> recommendationss = this.recommender.recommend(user, 3);

        for (RecommendedItem recommendation : recommendationss) {
            int idOfProduct = (int) recommendation.getItemID();
            recommendations.add(productsReverse.get(idOfProduct));
        }

        return recommendations;
    }
}
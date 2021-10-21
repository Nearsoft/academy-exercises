package nearsoft.academy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
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
    private static String ROOT_PATH = System.getProperty("user.dir");
    private static String CSV_DATA_PATH = ROOT_PATH + "/src/main/resources/data.csv";
    // private static String USER_INDEX = ROOT_PATH + "/src/main/resources/users.csv";
    // private static String PRODUCT_INDEX = ROOT_PATH + "/src/main/resources/products.csv";

    private String dataFilePath;
    private Map<String, Integer> products = new HashMap<String,Integer>();
    private Map<Integer, String> productsReverse = new HashMap<Integer,String>();
    private Map<String, Integer> users = new HashMap<String,Integer>();
    private int totalUsers = 0;
    private int totalProducts = 0;
    private int totalReviews = 0;

    // private ArrayList<MovieRecommender.User> users = new ArrayList<MovieRecommender.User>();

    public MovieRecommender(String dataFilePath) {
        this.dataFilePath = dataFilePath;

        File processedData = new File(CSV_DATA_PATH);

        // if (!processedData.exists()) {
        try {
            if (processedData.exists()) { processedData.delete(); }
            processFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // } else {
            // analizeData();
        // }

    }

    private void processFile () throws IOException {
        System.out.println("Processing data...\n");
        long start1 = System.nanoTime(); 

        FileInputStream file = new FileInputStream(this.dataFilePath);
        GZIPInputStream gzip = new GZIPInputStream(file);
        InputStreamReader isr = new InputStreamReader(gzip);
        BufferedReader br = new BufferedReader(isr);
        
        List<String> dataParts = new ArrayList<String>();
        String line;

        FileWriter fileWriter = new FileWriter(CSV_DATA_PATH);

        while ((line = br.readLine()) != null) {
            String[] elements = {"product/productId:", "review/userId:", "review/score:"};

            String[] parts = line.split(" ");

            Boolean idRequiredField = Arrays.asList(elements).contains(parts[0]);

            if (idRequiredField) {
                dataParts.add(parts[1]);
            }

            if (dataParts.size() == 3) {
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
        long end1 = System.nanoTime(); 
        System.out.println("Elapsed Time in seconds: " + ((end1 - start1) * 0.000000001));
    }

    // private void analizeData() {
    //     System.out.println("Analyzing data...\n");
    //     try {
    //         FileReader fileReader = new FileReader(new File(CSV_DATA_PATH));
    //         BufferedReader br = new BufferedReader(fileReader);
    //         String line = "";
            
    //         while ((line = br.readLine()) != null) {
    //             this.setOccurrences(line);
    //         }

    //     } catch (IOException e) {
    //         e.printStackTrace();
    //     }
    // }

    private void setOccurrences (String productId, String userId) {
        // Count reviews
        this.totalReviews++;

        // Count products
        if (!this.products.containsKey(productId)) {
            this.products.put(productId, totalProducts);
            this.productsReverse.put(totalProducts, productId);
            this.totalProducts++;
        }

        // Count users
        if (!this.users.containsKey(userId)) {
            this.users.put(userId, totalUsers);
            this.totalUsers++;
        }
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

        try {
            DataModel model = new FileDataModel(new File(CSV_DATA_PATH));

            UserSimilarity similarity = new PearsonCorrelationSimilarity(model);

            UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);

            UserBasedRecommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);

            long user = users.get(userID);

            System.out.println("User: " + userID + ": " + user);

            List<RecommendedItem> recommendationss = recommender.recommend(user, 3);

            System.out.println("Number of recomendations: " + recommendations.size());

            for (RecommendedItem recommendation : recommendationss) {
                int idOfProduct = (int) recommendation.getItemID();
                recommendations.add(productsReverse.get(idOfProduct));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return recommendations;
    }

    // public static void main(String[] args) throws IOException, TasteException {
    //     String ROOT_PATH = System.getProperty("user.dir");

    //     MovieRecommender mr = new MovieRecommender(ROOT_PATH + "/src/main/resources/movies.txt.gz");

    //     System.out.println("Total reviews: " + mr.getTotalReviews() + "\n");
    //     System.out.println("Total users: " + mr.getTotalUsers() + "\n");
    //     System.out.println("Total products: " + mr.getTotalProducts() + "\n");
    //     mr.getRecommendationsForUser("A141HP4LYPWMSR");
    // }

    class User {
        public long index;
        public String id;
    }

    class Product {
        public long index;
        public String id;
    }
    
}

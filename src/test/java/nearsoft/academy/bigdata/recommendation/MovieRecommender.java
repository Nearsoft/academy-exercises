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
import java.util.regex.Matcher;
import java.util.zip.GZIPInputStream;
import java.util.regex.Pattern;

public class MovieRecommender {
    // dataset path
    String path;

    // Counters
    int totalReviews;
    int totalProducts;
    int totalUsers;

    // Hash tables to store unique data
    Hashtable<String, Integer> products; // Stores products by String ID
    Hashtable<String, Integer> users; // Stores users by String ID
    Hashtable<Integer, String> productsByInt; // Stores products by Int ID to convert the product reccomender String ID

    MovieRecommender(String path) throws IOException {
        // Initialize variables
        this.path = path;
        this.totalReviews = 0;
        this.totalUsers = 0;
        this.totalProducts = 0;
        this.products = new Hashtable<String, Integer>();
        this.users = new Hashtable<String, Integer>();
        this.productsByInt = new Hashtable<Integer, String>();

        processDataSet();
    }

    /**
     * Process the dataset
     * --------------------------------------------------------
     * Counts total of products, users and reviews
     * Assigns a sequential integer to each user and product
     * Stores products and users into a hashtable
     * Writes a CSV file ordered by userID.
     *  - Contains userID, productID, product score
     *  - Will be used to build the model for the recommender
     *
     * @throws IOException
     */
    public void processDataSet() throws IOException {

        // Uncompress the dataset file and create a reader for it
        FileInputStream fileIn = new FileInputStream(this.path);
        GZIPInputStream gZIPInputStream = new GZIPInputStream(fileIn);
        Reader decoder = new InputStreamReader(gZIPInputStream);

        BufferedReader reader = new BufferedReader(decoder);

        // Stores the current line while reading
        String line;

        // Regex patters to get products, users and product score
        Pattern productsPattern = Pattern.compile("product\\/productId: ([\\D\\d]+)");
        Pattern usersPattern = Pattern.compile("review\\/userId: ([\\D\\d]+)");
        Pattern scorePattern = Pattern.compile("review\\/score: ([\\D\\d]+)");

        // Pattern matcher
        Matcher match;
        // Flag to know if the patter match the current line
        boolean matches;

        // Read first line
        line = reader.readLine();

        // Writer for the CSV file
        FileWriter writer = new FileWriter("movies.csv");

        String productId = "";
        String userId = "";
        String score = "";

        // Read the dataset file
        while (line != null ){

            // Users matching
            match = usersPattern.matcher(line);
            matches = match.matches();
            if(matches) {
                // gets the ID
                userId = line.split(" ")[1];

                // Stores new users into the hashtable
                // Increments the users counter
                if(users.get(userId) == null){
                    this.totalUsers++;
                    users.put(userId, this.totalUsers);
                }

            }

            // Products matching
            match = productsPattern.matcher(line);
            matches = match.matches();
            if(matches) {
                // gets the ID
                productId = line.split(" ")[1];

                // Stores new users into the hashtable
                // Increments the products counter
                if(products.get(productId) == null){
                    this.totalProducts++;
                    products.put(productId, this.totalProducts);

                    // Stores the products by int ID
                    productsByInt.put(this.totalProducts, productId);
                }
            }

            // Score matching
            match = scorePattern.matcher(line);
            matches = match.matches();
            if(matches) {
                // Increments the reviews counter
                this.totalReviews ++;
                // gets the ID
                score = line.split(" ")[1];
            }

            // Writes a new line into the CSV file when the 3 variables are set
            // Reinitialize the variables for the next review
            if(!userId.equals("") && !productId.equals("") && !score.equals("")) {
                writer.write(users.get(userId) + "," + products.get(productId) + "," + score + "\n");
                userId = "";
                productId = "";
                score = "";
            }

            line = reader.readLine();
        }

        reader.close();
        writer.close();
    }

    /**
     * Return the total of reviews on the data set
     * @return
     */
    public int getTotalReviews(){
        return this.totalReviews;
    }

    /**
     * Return the total of products on the data set
     * @return
     */
    public int getTotalProducts(){
        return this.totalProducts;
    }

    /**
     * Return the total of users on the data set
     * @return
     */
    public int getTotalUsers(){
        return this.totalUsers;
    }

    /**
     * Return 3 recommendations for a determined user
     * ---------------------------------------------------
     * Uses mahout recommender
     * @param user
     * @return
     * @throws IOException
     * @throws TasteException
     */
    public List<String> getRecommendationsForUser(String user) throws IOException, TasteException {
        // Creates recommender with the CSV file
        DataModel model = new FileDataModel(new File("movies.csv"));
        UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
        UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);
        UserBasedRecommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);

        // Stores the recommendations result as String
        List<String> recommendationsStr = new ArrayList<String>();

        // Stores the recommendations get from the recommender
        List<RecommendedItem> recommendations = recommender.recommend(users.get(user), 3);

        // Add the String product IDs into the String List
        for (RecommendedItem recommendation : recommendations) {
            recommendationsStr.add(productsByInt.get((int)(recommendation.getItemID())));
        }

        return recommendationsStr;
    }
}

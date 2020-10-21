package nearsoft.academy.bigdata.recommendation;
/*
  Big Data Exercises
  MovieRecommender Uses Amazon movie reviews sample data stanford.edu/data/web-Movies.html for a simple movie recommender

  by Mayra Lucero Garcia Ramirez

  Encora Academy 2020B
 */

// imports from mahout to recommendations
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

// imports from java
import java.io.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

public class MovieRecommender {

    /**
     * Variables used through the program
     */
    private String pathFile     = "";   // path of the gz file
    private int totalReviews    = 0;
    private int totalProducts   = 0;
    private int totalUsers      = 0;
    Hashtable<String, Integer> products;
    Hashtable<String, Integer> users;
    String currentUser      =   "";         // to add the current user to the csv file
    String currentProduct   =   "";         // to add the current product to the csv file
    String currentScore     =   "";         // to add the current score to the csv file

    /**
     * @param urlPathToFile receives the path to the gz file
     */
    MovieRecommender(String urlPathToFile) {
        // initialization of variables
        this.pathFile = urlPathToFile;
        this.users = new Hashtable<String, Integer>();
        this.products = new Hashtable<String, Integer>();
        this.totalReviews = 0;
        this.totalProducts = 0;
        this.totalUsers = 0;
        try {
            this.processFile();
        }
        catch (IOException exception) {
            exception.getStackTrace();
        }
    }

    /**
     * Restart variables used for construct the csv file
     */
    private void restartVariables() {
        this.currentUser      = "";
        this.currentProduct   = "";
        this.currentScore     = "";
    }

    /**
     * ProcessFile reads the gz file line by line and search
     * for the users, products, reviews and scores and calls the method
     * writeToDataFile to write on the csv file the current user, product and score
     * @throws IOException
     */
    private void processFile() throws IOException {
        // variables for gzip
        FileInputStream fileIn = new FileInputStream(this.pathFile);
        GZIPInputStream gZIPInputStream = new GZIPInputStream(fileIn);
        Reader decoder = new InputStreamReader(gZIPInputStream);
        BufferedReader br = new BufferedReader(decoder);
        String line;
        // variables for new file for recommendations
        File dataForRecommendations = new File("dataForRecommendations.csv");
        FileWriter fileWriter = new FileWriter(dataForRecommendations);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        // construction of regex for identify users, reviews, products and scores
        // \d matches a digit (equal to [0-9])
        // \D matches any character that's not a digit (equal to [^0-9])
        Pattern productPattern = Pattern.compile("product\\/productId: ([\\D\\d]+)");
        Pattern userPattern = Pattern.compile("review\\/userId: ([\\D\\d]+)");
        Pattern scorePattern = Pattern.compile("review\\/score: ([\\D\\d]+)");
        Matcher matcher;
        // begin of processing
        while ((line = br.readLine()) != null) {
            // product identification
            matcher = productPattern.matcher(line);
            if(matcher.matches()) {
                currentProduct = matcher.group(1);
                // if it is not already in the list of products it is added and the counter is updated
                if(!products.containsKey(currentProduct)) {
                    totalProducts +=1;
                    products.put(currentProduct,totalProducts);
                }
            }
            // user identification
            matcher = userPattern.matcher(line);
            if(matcher.matches()) {
                currentUser = matcher.group(1);
                // if it is not already in the list of users it is added and the counter is updated
                if(!users.containsKey(currentUser)) {
                    totalUsers +=1;
                    users.put(currentUser, totalUsers);
                }
                // it indicates the existence of a review, the counter is updated
                totalReviews+=1;
            }
            // score identification
            matcher = scorePattern.matcher(line);
            if(matcher.matches()) {
                currentScore = matcher.group(1);
            }
            writeToDataFile(bufferedWriter);
        }
        // close the buffers (reader and writer) and the gz file
        bufferedWriter.close();
        br.close();
        gZIPInputStream.close();
    }

    /**
     * Write to data file for recommendations
     * @param bufferedWriter for write to data file
     * @throws IOException
     */
    private void writeToDataFile(BufferedWriter bufferedWriter) throws IOException {
        // checks that all the information is complete
        if(!currentProduct.equals("") && !currentScore.equals("") && !currentUser.equals("")) {
            // format to integers because the data model only accepts numbers
            int idUserForData = users.get(currentUser);
            int idProductForData = products.get(currentProduct);
            // write in csv file
            bufferedWriter.write(idUserForData + "," + idProductForData + "," + currentScore + "\n");
            restartVariables();
        }
    }

    /**
     * Returns the key of the value
     * @param value of the desired key
     * @return the key of the value
     */
    private String getProductId(int value)
    {
        for (String key : products.keySet()) {
            if (products.get(key)==value) {
                return key;
            }
        }
        return null;
    }

    /**
     * @return of the total reviews
     */
    public int getTotalReviews() {
        return totalReviews;
    }

    /**
     * @return of the total number of products
     */
    public int getTotalProducts() {
        return totalProducts;
    }

    /**
     * @return of the total number of users
     */
    public int getTotalUsers() {
        return totalUsers;
    }

    /**
     * This methods constructs a data model and process it with similarity neighborhood to find
     * the recommendations for a user based on its own rating scores.
     * @param user to whom recommendations will be made
     * @return a list of the recommendations of products for the user
     * @throws IOException
     * @throws TasteException
     */
    public List<String> getRecommendationsForUser(String user) throws IOException, TasteException {
        //conversion to the intId used in the csv data
        int userIntId = users.get(user);
        // to handle interaction data
        DataModel model = new FileDataModel(new File("./dataForRecommendations.csv"));
        // the correlation coefficient between their interactions
        UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
        // define which similar users we want to leverage for the recommender
        UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);
        UserBasedRecommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);
        // recommender receives the userID follow by the number of recommendations
        List<RecommendedItem> recommendations = recommender.recommend(userIntId, 3);
        // list of recommendations that is going to be returned
        List<String> recommendationsOutput = new ArrayList<String>();
        for (RecommendedItem recommendation : recommendations) {
            // conversion to the id of the product used in the csv
            int value = (int)recommendation.getItemID();
            // extraction of the key of the product
            String productIdRecommendation = getProductId(value);
            // append the product recommendation to the recommendations list
            recommendationsOutput.add(productIdRecommendation);
        }
        return recommendationsOutput;
    }
}

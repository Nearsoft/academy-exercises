package nearsoft.academy.bigdata.recommendation;
/**
 * Big Data Exercises
 * MovieRecommender Uses Amazon movie reviews sample data stanford.edu/data/web-Movies.html for a simple movie recommender
 *
 * by Mayra Lucero Garcia Ramirez
 *
 * Encora Academy 2020B
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
    private int totalReviews    = 0;    // integer for total of reviews
    private int totalProducts   = 0;    // integer for total of products
    private int totalUsers      = 0;    // integer for total of users

    Hashtable<String, Integer> products;    // Hashtable for products
    Hashtable<String, Integer> users;       // Hashtable for users
    String currentUser      =   "";         // string for current user, to add it to the csv file
    String currentProduct   =   "";         // string for current product, to add it to the csv file
    String currentScore     =   "";         // string for current score, to add it to the csv file

    /**
     *
     * @param urlPathToFile receives the path to the gz file
     */
    MovieRecommender(String urlPathToFile)
    {
        // initialization of variables
        this.pathFile = urlPathToFile;
        this.users = new Hashtable<String, Integer>();
        this.products = new Hashtable<String, Integer>();
        this.totalReviews = 0;
        this.totalProducts = 0;
        this.totalUsers = 0;
        // call to the method that processes the file
        try
        {
            this.processFile();
        }catch (IOException exception)
        {
            exception.getStackTrace();
        }
    }

    /**
     * Restart variables used for construct the csv file
     */
    private void restartVariables()
    {
        this.currentUser      = "";
        this.currentProduct   = "";
        this.currentScore     = "";
    }

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

        // Construction of regex for identify users, reviews, products and scores
        // \d matches a digit (equal to [0-9])
        // \D matches any character that's not a digit (equal to [^0-9])

        // product
        Pattern productPattern = Pattern.compile("product\\/productId: ([\\D\\d]+)");
        // users and reviews
        Pattern userPattern = Pattern.compile("review\\/userId: ([\\D\\d]+)");
        // score for data recommendations
        Pattern scorePattern = Pattern.compile("review\\/score: ([\\D\\d]+)");
        // declaration of matcher
        Matcher matcher;
        // begin of processing
        while ((line = br.readLine()) != null) {
            // Product identification
            matcher = productPattern.matcher(line);
            if(matcher.matches())
            {
                // product
                currentProduct = matcher.group(1);
                // if it is not already in the list of products it is added and the counter update
                if(!products.containsKey(currentProduct))
                {
                    totalProducts +=1;
                    products.put(currentProduct,totalProducts);
                }
            }
            // User identification
            matcher = userPattern.matcher(line);
            if(matcher.matches())
            {
                // user
                currentUser = matcher.group(1);
                // if it is not already in the list of user it is added and the counter update
                if(!users.containsKey(currentUser))
                {
                    totalUsers +=1;
                    users.put(currentUser, totalUsers);
                }
                // it indicates the existence of a review, the counter is updated
                totalReviews+=1;
            }
            // Score identification
            matcher = scorePattern.matcher(line);
            if(matcher.matches()) {
                currentScore = matcher.group(1);
            }
            //write info for recommendations
            writeToDataFile(bufferedWriter);
        }
        // close the buffers and the file
        bufferedWriter.close();
        br.close();
        gZIPInputStream.close();
    }

    /**
     * Wirte to data file for rrecommendations
     * @param bufferedWriter for write to data file
     * @throws IOException
     */
    private void writeToDataFile(BufferedWriter bufferedWriter) throws IOException{
        // checks that all the information is complete
        if(!currentProduct.equals("") && !currentScore.equals("") && !currentUser.equals(""))
        {
            // format to integers because the data model only accepts numbers
            int idUserForData = users.get(currentUser);
            int idProductForData = products.get(currentProduct);
            // write in csv file
            bufferedWriter.write(idUserForData + "," + idProductForData + "," + currentScore + "\n");
            // restart the variables
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
     *
     * @return of the total reviews
     */
    public int getTotalReviews()
    {
        return totalReviews;
    }

    /**
     *
     * @return of the total number of products
     */

    public int getTotalProducts()
    {
        return totalProducts;
    }

    /**
     *
     * @return of the total number of users
     */
    public int getTotalUsers()
    {
        return totalUsers;
    }

    /**
     *
     * @param user to whom recommendations will be made
     * @return a list of the recommendations of products for the user
     * @throws IOException
     * @throws TasteException
     */

    public List<String> getRecommendationsForUser(String user) throws IOException, TasteException {
        //conversion to the intId used for the csv data
        int userIntId = users.get(user);
        // to handle interaction data.
        DataModel model = new FileDataModel(new File("./dataForRecommendations.csv"));
        // the correlation coefficient between their interactions
        UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
        // define which similar users we want to leverage for the recommender
        UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);
        // create our recommender
        UserBasedRecommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);
        // recommender receives the userID follow by the number of recommendations, 3
        List<RecommendedItem> recommendations = recommender.recommend(userIntId, 3);
        // list of recommendations that is going to be returned
        List<String> recommendationsOutput = new ArrayList<String>();
        for (RecommendedItem recommendation : recommendations) {
            // conversion to the id of the product used in the csv
            int value = (int)recommendation.getItemID();
            // extraction of the key of the product
            String productIdRecommendation = getProductId(value);
            // append the product recommendation to the recommendations lista
            recommendationsOutput.add(productIdRecommendation);
        }
        return recommendationsOutput;
    }
}
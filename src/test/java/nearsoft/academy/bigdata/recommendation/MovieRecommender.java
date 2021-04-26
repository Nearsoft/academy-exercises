package nearsoft.academy.bigdata.recommendation;

import java.io.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    //Initialize variables
    Hashtable<String, Integer> users;
    Hashtable<String, Integer> products;
    Hashtable<Integer, String> productById;
    int totalUsers;
    int totalProducts;
    int totalReviews;
    FileReader file;

    //Constructor
    public MovieRecommender(String pathFile) throws IOException {
        this.totalUsers = 0;
        this.totalProducts = 0;
        this.totalReviews = 0;
        this.file = new FileReader(pathFile);
        this.users = new Hashtable<>();
        this.products = new Hashtable<>();
        this.productById = new Hashtable<>();

        processFile();
    }

    public void processFile() throws IOException {

        String userId = "";
        String productId = "";
        String reviewId = "";

        FileWriter csvFile = new FileWriter("data.csv");
        BufferedReader br = new BufferedReader(this.file);

        //Patterns for search the respective data with Regular Expressions
        Pattern usersRegex = Pattern.compile("review\\/userId: ([\\D\\d]+)");
        Pattern reviewsRegex = Pattern.compile("review\\/score: ([0-9]+).([0-9]+)");
        Pattern productsRegex = Pattern.compile("product\\/productId: ([\\D\\d]+)");

        //Initialize a Matcher object for the conditions below
        Matcher matcherProduct, matcherUser, matcherReview;
        //Initialize a string value for the line in the text file
        String Line;

        while ((Line = br.readLine()) != null){

            matcherProduct = productsRegex.matcher(Line);
            matcherUser = usersRegex.matcher(Line);
            matcherReview = reviewsRegex.matcher(Line);

            if(matcherUser.matches()) {
                userId = Line.split(" ")[1];

                if (users.get(userId) == null) {
                    this.totalUsers ++;
                    users.put(userId, this.totalUsers);
                }
            }

            if (matcherReview.matches()) {
                reviewId = Line.split(" ")[1];
                this.totalReviews++;
            }

            if (matcherProduct.matches()) {
                productId = Line.split(" ")[1];

                if (products.get(productId) == null) {
                    this.totalProducts++;
                    products.put(productId, this.totalProducts);
                    productById.put(this.totalProducts, productId);
                }

            }

            // This condition is for write the csv file when the userid, reviewId and product id has a value then
            // we reassign the variables to null values
            if (!userId.equals("") && !reviewId.equals("") && !productId.equals("")){
                csvFile.write(users.get(userId) + "," + products.get(productId) + "," + reviewId + "\n");
                userId = "";
                productId = "";
                reviewId = "";
            }

        }

        csvFile.close();
    }

    public int getTotalReviews(){
        return this.totalReviews;
    }
    public int getTotalProducts(){
        return this.totalProducts;
    }
    public int getTotalUsers(){
        return this.totalUsers;
    }

    public List<String> getRecommendationsForUser(String userID) throws IOException, TasteException {

        DataModel model = new FileDataModel(new File("data.csv"));
        UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
        UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);
        UserBasedRecommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);

        List<String> recommendations = new ArrayList<>();

        //For the 3 items in recommender.recommend(...) we add the values in the index from the hashtable productById in
        // the recommendations list
        for (RecommendedItem recommendation : recommender.recommend(users.get(userID), 3)) {
            recommendations.add(productById.get((int)(recommendation.getItemID())));
        }

        return recommendations;
    }

}

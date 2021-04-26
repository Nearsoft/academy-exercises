package nearsoft.academy.bigdata.recommendation;

import java.io.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.TaggedIOException;
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
    Hashtable<String, Integer> users;
    Hashtable<String, Integer> products;
    Hashtable<Integer, String> productById;
    int totalUsers;
    int totalProducts;
    int totalReviews;
    FileReader file;

    public MovieRecommender(String pathFile) throws IOException, FileNotFoundException{
        this.totalUsers = 0;
        this.totalProducts = 0;
        this.totalReviews = 0;
        this.file = new FileReader(pathFile);
        this.users = new Hashtable<String, Integer>();
        this.products = new Hashtable<String, Integer>();
        this.productById = new Hashtable<Integer, String>();

        processFile();
    }

    public void processFile() throws IOException, FileNotFoundException {

        String userId = "";
        String productId = "";
        String reviewId = "";

        FileWriter csvfile = new FileWriter("data.csv");
        //Scanner readerFile = new Scanner(this.file);
        BufferedReader br = new BufferedReader(this.file);

        Pattern usersRegex = Pattern.compile("review\\/userId: ([\\D\\d]+)");
        Pattern reviewsRegex = Pattern.compile("review\\/score: ([0-9]+).([0-9]+)");
        Pattern productsRegex = Pattern.compile("product\\/productId: ([\\D\\d]+)");

        Matcher matcherProduct, matcherUser, matcherReview;
        String Line;
        //while (readerFile.hasNextLine()){
        while ((Line = br.readLine()) != null){
            //String Line = readerFile.nextLine();
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
            if (!userId.equals("") && !reviewId.equals("") && !productId.equals("")){
                csvfile.write(users.get(userId) + "," + products.get(productId) + "," + reviewId + "\n");
                userId = "";
                productId = "";
                reviewId = "";
            }

        }

        //503readerFile.close();
        csvfile.close();
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

        List<String> recommendations = new ArrayList<String>();

        for (RecommendedItem recommendation : recommender.recommend(users.get(userID), 3)) {
            recommendations.add(productById.get((int)(recommendation.getItemID())));
        }

        return recommendations;
    }

}

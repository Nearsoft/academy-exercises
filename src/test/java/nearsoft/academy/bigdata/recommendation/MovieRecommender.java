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
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

public class MovieRecommender {
    private int countReviews;
    private int countUsers;
    private int countProducts;

    HashMap<String, Integer> HashProducts = new HashMap<>();
    HashMap<String, Integer> HashUsers = new HashMap<>();

    //constructor
    public MovieRecommender(String s) throws IOException{
        this.countReviews = 0;
        this.countProducts = 0;
        this.countUsers = 0;
        String currentProduct;
        String currentUser;

        GZIPInputStream input = new GZIPInputStream(new FileInputStream(s));
        Reader decompressor = new InputStreamReader(input);
        BufferedReader reader = new BufferedReader(decompressor);

        File MoviesCVS = new File("movies.csv");
        FileWriter fileWriter = new FileWriter(MoviesCVS);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

        Pattern PatternProduct = Pattern.compile("^product/productId:\\s([\\d\\D]*)$");
        Pattern PatternUser = Pattern.compile("^review/userId:\\s([\\d\\D]*)$");
        Pattern PatternReview = Pattern.compile("^review/score:\\s(\\d.\\d)$");

        String currentLine;
        while((currentLine = reader.readLine()) != null){

            Matcher MatchProduct = PatternProduct.matcher(currentLine);
            Matcher MatchUser = PatternUser.matcher(currentLine);
            Matcher MatchReview = PatternReview.matcher(currentLine);

            if(MatchProduct.matches()) {
                this.countReviews ++;
                String ProductID = MatchProduct.group(1);
                if(!HashProducts.containsKey(ProductID)){
                    this.countProducts ++;
                    HashProducts.put(ProductID, this.countProducts);
                }

                currentProduct = HashProducts.get(ProductID).toString();

                while(!MatchUser.matches()){
                    currentLine = reader.readLine();
                    MatchUser = PatternUser.matcher(currentLine);
                }

                String userID = MatchUser.group(1);
                if(!HashUsers.containsKey(userID)){

                    this.countUsers += 1;
                    HashUsers.put(userID, this.countUsers);

                }
                currentUser = HashUsers.get(userID).toString();

                while(!MatchReview.matches()){
                    currentLine = reader.readLine();
                    MatchReview = PatternReview.matcher(currentLine);
                }

                String Review = MatchReview.group(1);
                bufferedWriter.write(currentUser + "," + currentProduct + "," + Review + "\n");
            }
        }

        bufferedWriter.close();
        fileWriter.close();
        decompressor.close();
        reader.close();
    }

    public int getTotalReviews(){
        return this.countReviews;
    }

    public int getTotalProducts(){
        return this.countProducts;
    }

    public int getTotalUsers(){
        return this.countUsers;
    }

    private String getProductID(int ID) {
        for (String key : HashProducts.keySet()) {
            if (HashProducts.get(key)==ID) {
                return key;
            }
        }
        return null;
    }

    public List<String> getRecommendationsForUser(String user) throws IOException, TasteException {
        List<String> recommendations = new ArrayList<>();

        int userId = HashUsers.get(user);

        DataModel model = new FileDataModel(new File("movies.csv"));
        UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
        UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);
        UserBasedRecommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);

        List<RecommendedItem> Recommendations = recommender.recommend(userId, 3);
        for (RecommendedItem recommendation : Recommendations) {
            int value = (int)recommendation.getItemID();
            String productIdRecommendation = getProductID(value);
            recommendations.add(productIdRecommendation);
        }

        return recommendations;
    }
}

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
import java.util.zip.GZIPInputStream;

public class MovieRecommender {
    int totalReviews = 0;
    int totalProducts = 0;
    int totalUsers = 0;

    HashMap<String, Integer> products = new HashMap<String, Integer>();
    HashMap<String, Integer> users = new HashMap<String, Integer>();

    public MovieRecommender(String path) throws  IOException {
        GZIPInputStream inputFile = new GZIPInputStream(new FileInputStream(path));
        Reader decoder = new InputStreamReader(inputFile);
        BufferedReader bReader = new BufferedReader(decoder);
        String currentLine;

        File result = new File("movies.csv");
        FileWriter fWriter = new FileWriter(result);
        BufferedWriter bWriter = new BufferedWriter(fWriter);

        String productList = "";
        String userList = "";
        String scoreList = "";

        int totalProducts = 0;
        int totalUsers = 0;
        int totalReviews = 0;


        while ((currentLine = bReader.readLine()) != null){
            if (currentLine.startsWith("product/productId:")){
                totalReviews++;
                String productId = currentLine.split(" ")[1];

                if (!products.containsKey(productId)){
                    products.put(productId, totalProducts++);
                }
                productList = products.get(productId).toString();

            } else if (currentLine.startsWith("review/userId:")) {
                String userID = currentLine.split(" ")[1];

                if (!users.containsKey(userID)){
                    users.put(userID,totalUsers++);
                }
                userList = users.get(userID).toString();

            } else if (currentLine.startsWith("review/score:")){
                scoreList = currentLine.split(" ")[1];
                String finalList = String.format("%s,%s,%s\n", userList, productList, scoreList);
                bWriter.write(finalList);
            }
        }

        bReader.close();
        bWriter.close();

        this.totalReviews = totalReviews;
        this.totalProducts = products.size();
        this.totalUsers = users.size();
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


    public List<String> getRecommendationsForUser(String userId) throws IOException, TasteException {

        List<String> results = new ArrayList<String>();

        int user = users.get(userId);

        DataModel model = new FileDataModel(new File("movies.csv"));
        UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
        UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);
        UserBasedRecommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);

        List<RecommendedItem> recommendations = recommender.recommend(user, 3);

        for (RecommendedItem recommendation : recommendations) {
            for (String key : this.products.keySet()) {
                if (this.products.get(key)==recommendation.getItemID()) {
                    results.add(key);
                }
            }

        }
        return results;
    }
}
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


    HashMap<String, Integer> productsMap = new HashMap<String, Integer>();
    HashMap<String, Integer> usersMap = new HashMap<String, Integer>();
    HashMap<Integer, String> inverseProductsMap = new HashMap<Integer, String>();



    public MovieRecommender(String path) throws  IOException {


        GZIPInputStream in = new GZIPInputStream(new FileInputStream(path));
        Reader decoder = new InputStreamReader(in);
        BufferedReader br = new BufferedReader(decoder);
        String line;

        File result = new File("reviewData.csv");
        FileWriter fw = new FileWriter(result);
        BufferedWriter bw = new BufferedWriter(fw);


        String productListCSV = "";
        String userListCSV = "";
        String scoreListCSV = "";

        int numberOfProducts = 1;
        int numberOfUsers = 0;
        int numberOfReviews = 0;


        while ((line = br.readLine()) != null){
            if (line.startsWith("product/productId:")){
                numberOfReviews++;
                String [] temporalLine = line.split(" ");
                String productId = temporalLine[1];

                if (!productsMap.containsKey(productId)){
                    productsMap.put(productId, numberOfProducts);
                    inverseProductsMap.put(numberOfProducts,productId);
                    numberOfProducts++;
                }
                productListCSV = productsMap.get(productId).toString();

            } else if (line.startsWith("review/userId:")) {
                String [] temporalLine = line.split(" ");
                String userID = temporalLine[1];

                if (!usersMap.containsKey(userID)){
                    usersMap.put(userID,numberOfUsers++);
                }
                userListCSV = usersMap.get(userID).toString();


            } else if (line.startsWith("review/score:")){
                String [] temporalLine = line.split(" ");
                scoreListCSV = temporalLine[1];
                String finalList = String.format("%s,%s,%s\n", userListCSV, productListCSV, scoreListCSV);
                bw.write(finalList);
            }
        }

        br.close();
        bw.close();




        this.totalReviews = numberOfReviews;
        this.totalProducts = productsMap.size();
        this.totalUsers = usersMap.size();

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

        int user = usersMap.get(userId);

        DataModel model = new FileDataModel(new File("reviewData.csv"));
        UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
        UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);
        UserBasedRecommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);

        List<RecommendedItem> recommendations = recommender.recommend(user, 3);

        for (RecommendedItem recommendation : recommendations) {
            results.add(inverseProductsMap.get((int)recommendation.getItemID()));
        }
        return results;

    }
}

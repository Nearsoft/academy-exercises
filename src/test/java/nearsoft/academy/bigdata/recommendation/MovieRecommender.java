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
import java.util.*;

public class MovieRecommender {
    String path;
    String numericPath;
    int totalReviews;
    int totalProducts;
    HashMap<String, Long> userMap = new HashMap();
    HashMap<String, Long> productMap = new HashMap();
    HashMap<Long, String> inverseProductMap = new HashMap();

    public List<String> getRecommendationsForUser(String userId) throws TasteException, IOException {
        List<String> recommendationsStr = new ArrayList<String>();
        DataModel model = new FileDataModel(new File(numericPath));
        UserSimilarity similarity = new PearsonCorrelationSimilarity(model);

        UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);

        UserBasedRecommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);

        //Find user in map
        long numericUserId = userMap.get(userId);

        //Give 3 recommendations to the User userId
        List<RecommendedItem> recommendations = recommender.recommend(numericUserId, 3);
        for (RecommendedItem recommendation : recommendations) {
            recommendationsStr.add( inverseProductMap.get( recommendation.getItemID() ));
        }
        return recommendationsStr;
    }

    public int getTotalReviews() {
        return totalReviews;
    }

    public int getTotalProducts() {
        return totalProducts;
    }

    public int getTotalUsers() {
        return totalUsers;
    }

    int totalUsers;

    public MovieRecommender(String path) throws IOException {
        this.path = path;
        this.numericPath = path.split(".csv")[0] + "numeric.csv";
        int totalProducts;
        BufferedReader br = new BufferedReader(new FileReader(new File(path)));
        String thisLine = null;

        //We get unique values for users and products and initialeze totalUsers and totalProducts
        String[] temp;
        String userId;
        String productId;
        int totalReviews = 0;
        long i = 0;
        long j = 0;
        while ((thisLine = br.readLine()) != null) {
            totalReviews++;
            temp = thisLine.split(",");
            productId = temp[1];
            userId = temp[0];

            //Change AlphaNumeric id for long
            if (!userMap.containsKey(userId)) {
                userMap.put(userId, i);
                i++;
            }
            if (!productMap.containsKey(productId)){
                productMap.put(productId, j);
                inverseProductMap.put(j, productId);
                j++;
            }
        }
        this.totalProducts = productMap.size();
        this.totalUsers = userMap.size();
        this.totalReviews = totalReviews;
       buildCSV("./clean_data.csv");
    }

    public void buildCSV(String path) throws IOException {
        String thisLine = null;
        FileWriter fw = new FileWriter(numericPath);
        BufferedWriter bw = new BufferedWriter(fw);
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(path)));
            String[] subarray = new String[3];
            long i = 0;
            long newUserId = 0;
            long newProductId =0;

            while ((thisLine = br.readLine()) != null) {
                subarray = thisLine.split(",");
                newUserId = userMap.get(subarray[0]);
                newProductId = productMap.get(subarray[1]);
                bw.write(newUserId+","+newProductId+","+subarray[2]+"\n");
            }
            bw.flush();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
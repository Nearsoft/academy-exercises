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
    // Declare variables
    int totalReviews = 0;
    int totalProducts = 0;
    int totalUsers = 0;

    HashMap<String, Integer> productsMap = new HashMap<String, Integer>();
    HashMap<String, Integer> usersMap = new HashMap<String, Integer>();
    HashMap<Integer, String> inverseProductsMap = new HashMap<Integer, String>();

    // MovieRecommender receive a path that is the Dataset 
    public MovieRecommender(String path) throws  IOException {


        GZIPInputStream document = new GZIPInputStream(new FileInputStream(path));
        BufferedReader bReader = new BufferedReader(new InputStreamReader(document));
        String line;
        File result = new File("newData.csv");
        BufferedWriter bWriter = new BufferedWriter(new FileWriter(result));

        String productListCSV = "";
        String userListCSV = "";
        String scoreListCSV = "";

        int numProducts = 0;
        int numUsers = 0;
        int numReviews = 0;

        while ((line = bReader.readLine()) != null){
            if (line.startsWith("product/productId:")){
                numReviews++;
                String [] tempLine = line.split(" ");
                String productId = tempLine[1];

                if (!productsMap.containsKey(productId)){
                    // Fill out 2 Hashes 
                    productsMap.put(productId, numProducts);
                    inverseProductsMap.put(numProducts,productId);
                    numProducts++;
                }
                productListCSV = productsMap.get(productId).toString();

            } else if (line.startsWith("review/userId:")) {
                
                String [] tempLine = line.split(" ");
                String userID = tempLine[1];

                if (!usersMap.containsKey(userID)){
                    usersMap.put(userID,numUsers++);
                }
                userListCSV = usersMap.get(userID).toString();


            } else if (line.startsWith("review/score:")){
                String [] tempLine = line.split(" ");
                scoreListCSV = tempLine[1];

                // Format in the document
                bWriter.write(userListCSV + "," + productListCSV + "," + scoreListCSV + "\n");
            }
        }

        bReader.close();
        bWriter.close();

        this.totalReviews = numReviews;
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

        DataModel model = new FileDataModel(new File("newData.csv"));
        UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
        UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);
        UserBasedRecommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);

        List<RecommendedItem> recommendations = recommender.recommend(usersMap.get(userId), 3);

        for (RecommendedItem recommendation : recommendations) {
            results.add(inverseProductsMap.get((int)recommendation.getItemID()));
        }
        return results;

    }
}
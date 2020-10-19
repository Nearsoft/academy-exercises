package nearsoft.academy.bigdata.recommendation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

    // Attributes
    private DataBuilder data;

    // Constructor
    public MovieRecommender(String srcFile) throws IOException {
        // Build all the data that needs to be build including the csv for datamodel
        data = new DataBuilder(srcFile);
    }

    // This function let us to obtain the AlphaNumerical ID searching by the 
    // Numeric ID. 
    // This due to the recommender, It needs Numeric value for IDs, and the data
    // uses Alphanumerical values for IDs, so, in the dataBuilder we create an 
    // numeric Id and we store the two IDs in the hashmap.
    private String getAlphanumericalID(int value){
        for (String key : this.data.getProducts().keySet()) {
            if (this.data.getProducts().get(key)==value) {
                return key;
            }
        }
        return null;
    }
    
    // Function to get a recommendation for a specific user from the recommender
    List<String> getRecommendationsForUser(String userId) throws IOException, TasteException {
        
        int id = this.data.getUsers().get(userId);
        List<String> recommendations = new ArrayList<String>();

        // Create and train model 
        DataModel model = new FileDataModel(new File("movies.csv"));
        UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
        UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);

        // Create Recommender
        UserBasedRecommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);

        // Fill the recomendations list with the real alphanumerical value
        List<RecommendedItem> recommended = recommender.recommend(id, 3);
        for (RecommendedItem recommendation  : recommended) {
            recommendations.add(getAlphanumericalID((int)recommendation.getItemID()));
        }

        return recommendations;
    }

    // Get some information from the all the data builded 
    int getTotalReviews() {
        return this.data.getTotalReviews();
    }
    int getTotalProducts() {
        return this.data.getTotalProducts();
    }
    long getTotalUsers() {
        return this.data.getTotalUsers();
    }
}
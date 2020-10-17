import java.util.zip.GZIPInputStream;
import java.io.*;
import java.util.*;
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
    private int countReviews = 0;
    private int countUsers = 0;
    private int countProducts = 0;
    private int userAmount;
    private int productAmount;
    HashMap<String, Integer> productHash = new HashMap();
    HashMap<Integer, String> productHashInverse = new HashMap();
    HashMap<String, Integer> userHash = new HashMap();

    public MovieRecommender(String infile) throws IOException, TasteException {
        //Read the txt.gz file
        GZIPInputStream in = new GZIPInputStream(new FileInputStream(infile));
        Reader decoder = new InputStreamReader(in);
        BufferedReader reader = new BufferedReader(decoder);

        //Create a csv for mahout
        File myFile = new File("dataset.csv");
        FileWriter fileWriter = new FileWriter(myFile);
        BufferedWriter writer = new BufferedWriter(fileWriter);

        String line;
        while ((line = reader.readLine()) != null) {
            if(line.startsWith("product/productId")) {
                String productId = line.split(" ")[1]; //get the product id
                if (!productHash.containsKey(productId)) {
                    countProducts++;
                    productAmount = countProducts;
                    productHash.put(productId, countProducts);
                    productHashInverse.put(countProducts, productId);
                }else {
                    productAmount = productHash.get(productId);
                }
            }
            if (line.startsWith("review/userId")) {
                String userId = line.split(" ")[1]; //get the user id
                if(!userHash.containsKey(userId)) {
                    countUsers++;
                    userAmount = countUsers;
                    userHash.put(userId, countUsers);
                } else {
                    userAmount = userHash.get(userId);
                }
            }
            if (line.startsWith("review/score")) {
                String scores = line.split(" ")[1]; //get the score
                writer.write(userAmount + "," + productAmount + "," + scores + "\n");
                countReviews++;
            }
        }
        reader.close();
        writer.close();
        // System.out.println(countReviews + "," + countProducts + "," + countUsers);
    }

    public int getTotalReviews() throws IOException {
        return countReviews;
    }

    public int getTotalProducts() throws IOException {
        return countProducts;
    }

    public int getTotalUsers() throws IOException {
        return countUsers;
    }

    public List<String> getRecommendationsForUser(String userID) throws IOException, TasteException {
        DataModel model = new FileDataModel(new File("dataset.csv"));
        UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
        UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);
        UserBasedRecommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);
        
        List <RecommendedItem> recommendations = recommender.recommend(userHash.get(userID), 3);
        List<String> list = new ArrayList<String>();

        for (RecommendedItem recommendation : recommendations) {
            System.out.println(productHashInverse.get((int)recommendation.getItemID()));
            list.add(productHashInverse.get((int)recommendation.getItemID()));
        }
        return list;
    }

}
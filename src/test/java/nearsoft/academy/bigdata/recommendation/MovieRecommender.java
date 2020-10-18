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
import java.util.zip.GZIPInputStream;

public class MovieRecommender {
    private long totalReviews = 0;
    private int totalUsers = 0;
    private int totalProducts = 0;
    private HashMap<String, Integer> users = new HashMap<String, Integer>();
    private HashMap<String, Integer> products = new HashMap<String, Integer>();
    public MovieRecommender (String dataPath){

        try {
            //Get the dataSet
            InputStream myObj = new GZIPInputStream(new FileInputStream(dataPath));
            //File myObj = new File("dataset/smallset.txt");
            Scanner myReader = new Scanner(myObj);
            //user flag
            boolean userFlag = false;
            //product flag
            boolean productFlag = false;
            //score flag
            boolean scoreFlag = false;
            //Data set for the recommendations
            FileWriter csvWriter = new FileWriter("dataSet.csv");
            //Variables to keep track of the current review
            String currentUser = "";
            String currentProduct = "";
            float currentScore = 0.0f;

            //Read the dataSet
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();

                //Add user
                if(!userFlag){
                    if(data.contains("userId")) {
                        currentUser = data.substring(15);
                        if (users.get(currentUser) == null) {
                            users.put(currentUser, totalUsers);
                            totalUsers++;
                        }
                        userFlag = true;
                    }
                }

                //Add product
                if(!productFlag){
                    if(data.contains("productId")){
                        currentProduct = data.substring(19);
                        if(products.get(currentProduct) == null){
                            products.put(currentProduct, totalProducts);
                            totalProducts++;
                        }
                        productFlag = true;
                    }
                }

                //Get Score
                if(!scoreFlag){
                    if(data.contains("review/score:")){
                        currentScore = Float.parseFloat(data.substring(14));
                        //System.out.println(currentScore);
                        scoreFlag = true;
                    }
                }

                //Add review
                if(data.contains("review/text:")) {
                    totalReviews++;
                    //Update the csv
                    String csvEntry = users.get(currentUser)+","+products.get(currentProduct)+","+currentScore;
                    csvWriter.append(csvEntry);
                    csvWriter.append("\n");
                    //Restart the flags
                    userFlag = false;
                    productFlag = false;
                    scoreFlag = false;
                }

            }

            //Close the reader
            myReader.close();
            //Close the CSV writer
            csvWriter.flush();
            csvWriter.close();

        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public long getTotalReviews(){
        return totalReviews;
    }

    public long getTotalProducts(){
        return totalProducts;
    }

    public long getTotalUsers(){
        return totalUsers;
    }

    public List<String> getRecommendationsForUser(String userID) throws TasteException, IOException {
        List<String> result = new LinkedList<String>();


        DataModel model = new FileDataModel(new File("dataSet.csv"));
        UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
        UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);
        UserBasedRecommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);

        List<RecommendedItem> recommendations = recommender.recommend(users.get(userID), 3);
        for (RecommendedItem recommendation : recommendations) {
            result.add(getProductID((int)recommendation.getItemID()));
        }
        return result;

    }

    private String getProductID(int value){
        for (String key : products.keySet()) {
            if (products.get(key)==value) {
                return key;
            }
        }
        return null;
    }
}

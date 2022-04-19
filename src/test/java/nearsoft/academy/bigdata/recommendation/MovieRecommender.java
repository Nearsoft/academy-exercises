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
import org.junit.Before;
import org.junit.internal.matchers.StringContains;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.zip.GZIPInputStream;

public class MovieRecommender {
    private String originDataPath;
    private String csvFilePath = "movies.csv";
    private int totalReviews = 0;
    private int totalProducts = 0;
    private int totalUsers = 0;
    private HashMap<String, Integer> userIDMap = new HashMap();
    private HashMap<String, Integer> productIDMap = new HashMap();
    private HashMap<Integer, String> inverseProductIDMap = new HashMap();
    private DataModel model;
    private UserSimilarity similarity;
    private UserNeighborhood neighborhood;
    private GenericUserBasedRecommender recommender;

    public MovieRecommender(String path) throws IOException, TasteException {
        this.originDataPath = path;
        preparingData();
        loadModel();
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

    private void preparingData() throws IOException {
        //Extracting data from .gz convert to .csv
        InputStream file = new FileInputStream(this.originDataPath);
        InputStream moviesGzStream = new GZIPInputStream(file);
        Reader read = new InputStreamReader(moviesGzStream);
        BufferedReader txtFile = new BufferedReader(read);
        BufferedWriter csvFile = new BufferedWriter(new FileWriter(this.csvFilePath));

        String product_ID = "";
        String score = "";
        String user_ID = "";

        String line = txtFile.readLine();

        while (line != null){

            if (line.contains("product/productId:" ) || line.contains("review/userId:" ) || line.contains("review/score:" )){
                String lineToWrite = line.split(" ")[1];
                String caseLine = line.split(" ")[0];
                
                switch (caseLine){
                    case "product/productId:":
                        product_ID = lineToWrite;
                        if (this.productIDMap.get(lineToWrite) == null) {
                            this.totalProducts++;
                            this.productIDMap.put(lineToWrite,this.totalProducts);
                            this.inverseProductIDMap.put(this.totalProducts, lineToWrite);
                        }
                        break;

                    case "review/userId:":
                        user_ID = lineToWrite;
                        if (this.userIDMap.get(lineToWrite) == null) {
                            this.totalUsers++;
                            this.userIDMap.put(lineToWrite, this.totalUsers);
                        }
                        break;

                    case "review/score:":
                        score = String.valueOf(lineToWrite.charAt(0));
                        this.totalReviews++;
                        break;
                }
            }


            if (line.isEmpty()) {

                csvFile.write(this.userIDMap.get(user_ID) + "," + this.productIDMap.get(product_ID) + "," + score + "\n");

                product_ID = "";
                score = "";
                user_ID = "";
            }

            line = txtFile.readLine();

        }

        txtFile.close();
        csvFile.close();

    }

    private void loadModel() throws IOException, TasteException {
        File my_file = new File(this.csvFilePath);
        this.model = new FileDataModel(my_file);
        this.similarity = new PearsonCorrelationSimilarity(this.model);
        this.neighborhood = new ThresholdUserNeighborhood(0.1, this.similarity, this.model);
        this.recommender = new GenericUserBasedRecommender(this.model, this.neighborhood, this.similarity);
    }

    public List<String> getRecommendationsForUser(String userID) throws TasteException {
        
        List <String> recommended = new ArrayList<String>() {};
        List<RecommendedItem> recommendations = this.recommender.recommend(this.userIDMap.get(userID), 3);
			
        for (RecommendedItem recommendation : recommendations) {
            recommended.add(this.inverseProductIDMap.get( (int) recommendation.getItemID()));
        }

        return recommended;
    }
}
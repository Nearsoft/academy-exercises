package nearsoft.academy.bigdata.recommendation;

import java.io.*;
import java.util.Hashtable;
import java.util.zip.GZIPInputStream;
import java.util.List;
import java.util.ArrayList;

import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;

public class MovieRecommender {
    String dataPath;
    int totalUsers;
    int totalProducts;
    int totalReviews;

    // Keep track of users and movies in the dataset
    Hashtable<String, Integer> users;
    Hashtable<String, Integer> products2Index;

    // For querying movies
    Hashtable<Integer, String> index2Products;

    DataModel model;
    UserSimilarity similarity;
    UserNeighborhood neighborhood;
    UserBasedRecommender recommender;


    MovieRecommender(String dataPath) throws IOException {
        this.dataPath = dataPath;
        this.totalUsers = 0;
        this.totalProducts = 0;
        this.totalReviews = 0;

        this.users = new Hashtable<String, Integer>();
        this.index2Products = new Hashtable<Integer, String>();
        this.products2Index = new Hashtable<String, Integer>();

        try {
            dataPreprocess();
            loadDataModel();
        } catch (Exception e) {
            e.printStackTrace();
        }
    

    }

    public int getTotalReviews() {
        return this.totalReviews;
    }
    
    public int getTotalUsers() {
        return this.totalUsers;
    }
    
    public int getTotalProducts() {
        return this.totalProducts;
    }

    /**
    * Get the list of the recommendations
     */
    public List<String> getRecommendationsForUser(String user) throws TasteException  {
        List<String> recommendations = new ArrayList<String>(); 

        for (RecommendedItem recommendation : this.recommender.recommend(users.get(user), 3)) {
            //System.out.println(recommendation.getItemID());
            long rec = recommendation.getItemID();
            int rec_index = (int) rec;
            String rec_id = this.index2Products.get(rec_index);
            recommendations.add(rec_id);

        }
        return recommendations;
    }

    /**
    * Load .csv file
    */
    private void loadDataModel() throws IOException, TasteException  {
        this.model = new FileDataModel(new File("data/movies.csv"));
        this.similarity = new PearsonCorrelationSimilarity(this.model);
        this.neighborhood = new ThresholdUserNeighborhood(0.1, this.similarity, this.model);    
        this.recommender = new GenericUserBasedRecommender(this.model, this.neighborhood, this.similarity);
    }
    

    /**
    * Extract data from .gz, iterate through .txt and create a .csv 
    */
    private void dataPreprocess() throws IOException {
        // Extract .gz and open .txt file
        InputStream file = new FileInputStream(this.dataPath);
        InputStream gzStream = new GZIPInputStream(file);
        Reader read = new InputStreamReader(gzStream);

        // Read .txt file 
        BufferedReader txtFile = new BufferedReader(read);

        // Create .csv
        BufferedWriter csvFile = new BufferedWriter(new FileWriter("data/movies.csv"));
        
        String productId = "";
        String score = "";
        String userId = "";

        String line = txtFile.readLine();

        while (line != null) {
            //System.out.println(line);

            if (line.contains("product/productId")) {
                productId = line.split(" ")[1];

                if (this.products2Index.get(productId) == null) {
                    this.totalProducts++;
                    this.index2Products.put(this.totalProducts, productId);
                    this.products2Index.put(productId, this.totalProducts);
                    //System.out.println("Product: " + productId);
                }
            } else if (line.contains("review/userId:")) {
                userId = line.split(" ")[1];

                if (this.users.get(userId) == null) {
                    this.totalUsers++;
                    this.users.put(userId, this.totalUsers);
                    //System.out.println("User: " + userId);
                }
            } else if (line.contains("review/score:")) {
                score = line.split(" ")[1];
                this.totalReviews++;
                //System.out.println("Review: " + score);

            }


            // If we have all the fields for one review
            if ((userId != "") && (productId != "") && (score != "")) {
                csvFile.write(
                    this.users.get(userId) + "," +
                    this.products2Index.get(productId) + "," +
                    score + "\n"
                );
                // System.out.println(
                //     this.users.get(userId) + ", " +
                //     this.products2Index.get(productId) + ", " +
                //     productId + ", " + ", " +
                //     this.totalProducts + ", " +
                //     score + "\n"
                // );

                productId = "";
                score = "";
                userId = "";
            }
            
            line = txtFile.readLine();
        }
        txtFile.close();
        csvFile.close();
        System.out.println("Everything is ok!");

    }
}
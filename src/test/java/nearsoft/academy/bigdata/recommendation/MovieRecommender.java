package nearsoft.academy.bigdata.recommendation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

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
	// Recommender model
	private DataModel model;
	
	// Totals
	private int totalReviews = 0, totalProducts = 0, totalUsers = 0;
	
	// HashMaps for objects
	private Map<String, Integer> products = new HashMap<String, Integer>();
	private Map<String, Integer> users = new HashMap<String, Integer>();
	private Map<Integer, String> productsInverted = new HashMap<Integer, String>();
	
	public MovieRecommender(String path) throws IOException {
		// Get file
		FileInputStream rawFile = new FileInputStream(path);
		// Decompress file
        GZIPInputStream decompressedFile = new GZIPInputStream(rawFile);
        // Pass to stream
        InputStreamReader streamFile = new InputStreamReader(decompressedFile);
        
        // Read streamed file
        BufferedReader txtFile = new BufferedReader(streamFile);
        // Create CSV file
        FileWriter fileWriter = new FileWriter(System.getProperty("user.dir")+"/data/dataset.csv");
        
        
        String[] requiredFields = {"product/productId:", "review/userId:", "review/score:"};
        String productId = "";
        String userId = "";
        String score = "";
        String row;
        while ((row = txtFile.readLine()) != null) {
            
            if(row.contains(requiredFields[0])) {
            	productId = row.split(" ")[1];

                if (this.products.get(productId) == null) {
                    this.totalProducts++;
                    this.products.put(productId, this.totalProducts);
                    this.productsInverted.put(this.totalProducts, productId);
                }
            } else if(row.contains(requiredFields[1])) {
            	userId = row.split(" ")[1];

                if (this.users.get(userId) == null) {
                    this.totalUsers++;
                    this.users.put(userId, this.totalUsers);
                }
            } else if(row.contains(requiredFields[2])) {
            	score = row.split(" ")[1];
                this.totalReviews++;
            }
            
            if ((productId != "") && (userId != "") && (score != "")) {
            	fileWriter.write(
                    this.users.get(userId) + "," +
                    this.products.get(productId) + "," +
                    score + "\n"
                );
                productId = "";
                score = "";
                userId = "";

            }

        }
        fileWriter.close();
        txtFile.close();
        System.out.println("ALL OK");
	}
	
	public List<String> getRecommendationsForUser(String userId) throws IOException, TasteException {
		this.model = new FileDataModel(new File("data/dataset.csv"));

		UserSimilarity simalirity = new PearsonCorrelationSimilarity(this.model);
		UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, simalirity, this.model);
		UserBasedRecommender recommender = new GenericUserBasedRecommender(this.model, neighborhood, simalirity);
		
		List<String> recommendations = new ArrayList<String>();
		
		long user = users.get(userId);

        List<RecommendedItem> recommendationsItems = recommender.recommend(user, 3);

        for (RecommendedItem recommendation : recommendationsItems) {
            int productId = (int) recommendation.getItemID();
            recommendations.add(productsInverted.get(productId));
        }

        return recommendations;
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

}

package nearsoft.academy.bigdata.recommendation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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



public class MovieRecommender{
	private String file_s="";
	private int dataProducts = 0;
	private int dataUsers = 0;
	private int dataReview = 0;	
	HashMap<String, Integer> usersMap = new HashMap<String, Integer>();
	HashMap<String, Integer> productsMap = new HashMap<String, Integer>();
	private String Products = "";
	private String Users = "";
	private String Scores = "";

	public MovieRecommender(String file_sile) throws IOException, NullPointerException {
		this.file_s = file_sile;
		this.dataReview = 0;
		this.dataProducts = 0;
		this.dataUsers = 0;
		this.dataProcess();
	}	

	private void dataProcess() throws IOException {
				
        GZIPInputStream inGZIP = new GZIPInputStream(new FileInputStream(this.file_s));
        BufferedReader bufferReader = new BufferedReader(new InputStreamReader(inGZIP));
        String readingLine;

        FileWriter fileWriter = new FileWriter(new File("movies_data.csv"));
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        
        Pattern productsPattern = Pattern.compile("product\\/productId: ([A-Z0-9]+)");
        Pattern userPattern = Pattern.compile("review\\/userId: ([\\D\\d]+)");
        Pattern scorePattern = Pattern.compile("review\\/score: ([0-9]+).([0-9]+)");
        
        Matcher matcherProduct, matcherUser, matcherScore;
        
        while((readingLine = bufferReader.readLine())!=null) {        	
        	matcherProduct = productsPattern.matcher(readingLine);
        	matcherUser = userPattern.matcher(readingLine);
        	matcherScore = scorePattern.matcher(readingLine);
        	if(matcherProduct.matches()) { Products = matcherProduct.group(1);        		
        		if (!productsMap.containsKey(Products)) {
        			dataProducts++;        			
        			productsMap.put(Products, dataProducts);
				}
        	}else if (matcherUser.matches()) {
        		dataReview++;
        		Users = matcherUser.group(1);
				if(!usersMap.containsKey(Users)) {
					dataUsers++;
					usersMap.put(Users, dataUsers);	
				}
				}else if (matcherScore.matches()) { Scores = matcherScore.group(1); }
        	if (!Users.equals("") && (!Products.equals("") && (!Scores.equals(""))) ) {
        		bufferedWriter.write(usersMap.get(Users) + "," + productsMap.get(Products) + "," + Scores + "\n");
        		Users = "";
			Products = "";
			Scores = "";
			}
        }       
        bufferedWriter.close();
        fileWriter.close();
        bufferReader.close();  
	}
	
	public int getTotalReviews() {
		return this.dataReview;
	}
	public int getTotalProducts() {
		this.dataProducts = productsMap.size();
		return this.dataProducts;
	}
	public int getTotalUsers() {
		return this.dataUsers;
	}
	
	private String getProductID(int value)
        {
        	for (String key : this.productsMap.keySet()) {
            		if (productsMap.get(key)==value) {
        	        return key;
	        	}            
        	}
        return null;
        }
        
	
	public List<String> getRecommendationsForUser(String userID) throws IOException, TasteException, NullPointerException{
		List<String> resultsList = new ArrayList<String>();
		Integer userId = this.usersMap.get(userID);
		DataModel model = new FileDataModel(new File("movies_data.csv"));

        UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
        UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);
        UserBasedRecommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);
        List<RecommendedItem> recommendations = recommender.recommend(userId, 3);
        for (RecommendedItem recommendation : recommendations){
            resultsList.add(getProductID(((int) recommendation.getItemID())));
        }
		return resultsList;
	}
}

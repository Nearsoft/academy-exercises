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
	private String pathF="";
	private int totalProducts = 0;
	private int totalUsers = 0;
	private int totalReviews = 0;	
	HashMap<String, Integer> usersMap = new HashMap<String, Integer>();
	HashMap<String, Integer> productsMap = new HashMap<String, Integer>();
	private String recoverdProduct = "";
	private String recoverdUser = "";
	private String recoverdScore = "";

	public MovieRecommender(String pathFile) throws IOException, NullPointerException {
		this.pathF = pathFile;
		this.totalReviews = 0;
		this.totalProducts = 0;
		this.totalUsers = 0;
		this.dataProcess();
	}	

	private void dataProcess() throws IOException {
				
		//Reading variables
        GZIPInputStream inGZIP = new GZIPInputStream(new FileInputStream(this.pathF));
        BufferedReader bufferReader = new BufferedReader(new InputStreamReader(inGZIP));
        String readingLine;
        //Writing variables
        FileWriter fileWriter = new FileWriter(new File("dataMoviesCSV.csv"));
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        
        //Pattern zone
        Pattern productPattern = Pattern.compile("product\\/productId: ([A-Z0-9]+)");
        Pattern userPattern = Pattern.compile("review\\/userId: ([\\D\\d]+)");
        Pattern scorePattern = Pattern.compile("review\\/score: ([0-9]+).([0-9]+)");
        
        //The matchers make the match between the pattern and the recovered line with the pattern
        Matcher matcherProduct, matcherUser, matcherScore;
        
        while((readingLine = bufferReader.readLine())!=null) {        	
        	matcherProduct = productPattern.matcher(readingLine);
        	matcherUser = userPattern.matcher(readingLine);
        	matcherScore = scorePattern.matcher(readingLine);
        	//We ask if the Recovered String is a Product, User or the Score
        	//Function to make de match and recover de products, users and scores
        	if(matcherProduct.matches()) {
        		recoverdProduct = matcherProduct.group(1);        		
        		if (!productsMap.containsKey(recoverdProduct)) {
        			totalProducts++;        			
        			productsMap.put(recoverdProduct, totalProducts);
				}
        	}else if (matcherUser.matches()) {
        		totalReviews++;
        		recoverdUser = matcherUser.group(1);
				if(!usersMap.containsKey(recoverdUser)) {
					totalUsers++;
					usersMap.put(recoverdUser, totalUsers);	
				}
			}else if (matcherScore.matches()) {
				recoverdScore = matcherScore.group(1);
			}
        	
        	//Writing zone
        	if (!recoverdUser.equals("") && (!recoverdProduct.equals("") && (!recoverdScore.equals(""))) ) {
        		bufferedWriter.write(usersMap.get(recoverdUser) + "," + productsMap.get(recoverdProduct) + "," + recoverdScore + "\n");
        		recoverdUser = "";
            	recoverdProduct = "";
            	recoverdScore = "";
			}
        	
        }       
             
        bufferedWriter.close();
        fileWriter.close();
        bufferReader.close();  
        
        System.out.println("Reseñas: " + totalReviews + " Productos: " + totalProducts + " Usuarios: " + totalUsers);
		
	}
	
	public int getTotalReviews() {
		return this.totalReviews;
	}
	public int getTotalProducts() {
		this.totalProducts = productsMap.size();
		return this.totalProducts;
	}
	public int getTotalUsers() {
		return this.totalUsers;
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
		System.out.println("Entre a la funcion");
		List<String> resultsList = new ArrayList<String>();
		
		Integer userId = this.usersMap.get(userID);
		System.out.println("Me salgo antes de entrar");
		DataModel model = new FileDataModel(new File("dataMoviesCSV.csv"));
		System.out.println("Si lei el csv");

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

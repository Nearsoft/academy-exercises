// this class works with the src/data/movies.txt.gz compressed file as input
// the src/data/movies.txt.gz current file is a provisional one for storage reasons
// so, in order to pass the test properly, it needs to be replaced with the original 3+ GB file
// ... with the correct file, this class generates an intermediate CSV file with clean data, 
// which is about 150 MB big

package movierec;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.ArrayList;
import java.util.Hashtable;
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
	int totalProducts;
    int totalUsers;
    int totalReviews;
    // keep hash tables to be able to 'translate' between numeric and alphanum. IDs
    Hashtable<String,Integer> products;
    Hashtable<String,Integer> users;
    Hashtable<Integer,String> productsByID;
    String myPath;
    String output;
    
    public MovieRecommender(String pathTidyFile) throws Exception {
    	this.totalProducts = 0;
        this.totalUsers = 0;
        this.totalReviews = 0;
        this.products = new Hashtable<String,Integer>();
        this.users = new Hashtable<String,Integer>();
        this.productsByID = new Hashtable<Integer,String>();
        this.myPath = pathTidyFile;
        this.output = "src/data/nice_data.csv";
    	dataWrangling();
    }
    
    // method for cleaning and re-arranging the data so that it is a valid input 
    // for the mahout FileDataModel class
    public void dataWrangling () throws IOException {
		InputStream gzipStream = new GZIPInputStream(new FileInputStream(this.myPath));
        BufferedReader buffered = new BufferedReader(new InputStreamReader(gzipStream));
        FileWriter csvWriter = new FileWriter(this.output);
        
        String auxLine = "";
        String productStr = "product/productId: ";
        String userStr = "review/userId: ";
        String scoreStr = "review/score: ";
        String user = "";
        String product = "";
        String score = "";
        
		while (auxLine != null) {
			if (auxLine.contains(userStr)) {
				user = auxLine.split(" ")[1];
				if(this.users.get(user) == null ) {
					this.totalUsers ++;
					this.users.put(user,this.totalUsers);
				}
			}
			if (auxLine.contains(productStr)) {
				product = auxLine.split(" ")[1];
				if(this.products.get(product) == null ) {
					this.totalProducts ++;
					this.products.put(product,this.totalProducts);
					this.productsByID.put(this.totalProducts,product);
				}
			}
			if (auxLine.contains(scoreStr)) {
				score = auxLine.split(" ")[1];
				this.totalReviews ++;
			}
			if (user != "" && product != "" && score != "") {
				csvWriter.write(this.users.get(user) + "," + this.products.get(product) + "," + score + "\n");
				user = "";
				product = "";
				score = "";
			}
			auxLine = buffered.readLine();
		}
		
		buffered.close();
		csvWriter.close();

    }
    
    // this method gets 3  item recommendations as output for a given user as input
    public List<String> getRecommendationsForUser(String userID) throws IOException, TasteException{
    	DataModel model = new FileDataModel(new File(this.output));
    	UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
    	UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);
    	UserBasedRecommender recommender = 
  			  new GenericUserBasedRecommender(model, neighborhood, similarity);
    	List<String> recommendations = new ArrayList<String>();
    	for (RecommendedItem recommendation : recommender.recommend(this.users.get(userID), 3)) {
    		recommendations.add(this.productsByID.get((int )(recommendation.getItemID())));
    	}
    	return recommendations;
    	
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
}


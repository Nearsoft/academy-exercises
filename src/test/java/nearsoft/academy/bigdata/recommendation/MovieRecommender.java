/*
    IMPORTANT NOTES:
        The program uses TXT instead of .GZ, you have to unzip the info first.
        The program saves the csv data in the same path as this file.
*/

package nearsoft.academy.bigdata.recommendation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;

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
    // Variables
    File file;
    FileWriter csv_writer;
    String review_pattern = "review/userId:";
    String products_pattern = "product/productId";
    String score_pattern = "review/score:";
    List<String> users = new ArrayList<String>();
    List<String> products = new ArrayList<String>();
    int total_reviews = 0;
    long total_time = 0;

    // Constructor
    public MovieRecommender(String file_path) throws IOException{
        // Opens the file given by the path
        file = new File(file_path);
        // Creates the CSV file
        csv_writer = new FileWriter("movies.csv");
        // Creates temporal variables
        String[] temp_string;
        String[] temp_line = {"","",""};
        // Create the stream and scanner to read the file
        FileInputStream inputStream = new FileInputStream(file);
        Scanner scanner = new Scanner(inputStream, "UTF-8");

        // Starts the loop to read all the file
        while (scanner.hasNextLine()) {
            // Saves the current line
            String line = scanner.nextLine();

            // If there was a product id in the line
            if (line.contains(products_pattern)){
                // Split the values between the pattern and the id
                temp_string = line.split(" ");
                // If it's a new product 
                if(products.contains(temp_string[1]) == false){
                    products.add(temp_string[1]);
                }
                // Saves the product on an array to later add it in the csv
                temp_line[1] = temp_string[1];
            }
            if(line.contains(review_pattern)){
                // Split the values between the pattern and the id
                temp_string = line.split(" ");
                // If it's a new user
                if(users.contains(temp_string[1]) == false){
                    users.add(temp_string[1]);
                }
                // Saves the user id on an array to later add it in the csv
                temp_line[0] = temp_string[1];
                // We count a new review if there was a user
                total_reviews += 1;
            }
            if (line.contains(score_pattern)){
                // Split the values between the pattern and the review score
                temp_string = line.split(" ");
                // Saves the score on an array to later add it in the csv
                temp_line[2] = temp_string[1];
            }
            // If there is no more info, another block-review start
            if (line.isEmpty()){
                // Get the postion of the user_id and product_id
                Integer user_id = users.indexOf(temp_line[0]);
                Integer product_id = products.indexOf(temp_line[1]);
                // Convert the int position to string
                String save_user_id = user_id.toString();
                String save_product_id = product_id.toString();
                // Saves the user_id, product_id, and review_score in the CSV
                csv_writer.append(save_user_id + "," + save_product_id + "," + temp_line[2] + "\n");
            }
        }
        // Close the scanner, CSV and free the buffer.
        scanner.close();
        csv_writer.flush();
        csv_writer.close();
    }

	public int getTotalReviews(){
        return this.total_reviews;
    }

    public int getTotalUsers(){
        return users.size();
    }

    public int getTotalProducts(){
        return products.size();
    }

    public List<String> getRecommendationsForUser(String user_id) throws TasteException, IOException{
        List<String> final_recommendations = new ArrayList<String>();
        
        // Creates the model, similarity, neighborhood and recommender from Mahout 
        DataModel model = new FileDataModel(new File("movies.csv"));
        UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
        UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);
        UserBasedRecommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);

        int index = users.indexOf(user_id); // Gets the user id of the CSV
        // Retrieves the recommendations for that user
        List<RecommendedItem> recommendations = recommender.recommend(index, 10000);

        // Add the products alfa numeric id into the final_recommendations list
        for (RecommendedItem recommendation : recommendations) {
            String temp_recomendation = products.get((int)recommendation.getItemID());
            final_recommendations.add(temp_recomendation);
        }

        return final_recommendations;
    }
}

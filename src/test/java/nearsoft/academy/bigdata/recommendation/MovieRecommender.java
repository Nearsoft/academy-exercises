package nearsoft.academy.bigdata.recommendation;

import java.io.IOException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.zip;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.FileInputStream;
//mahout package
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

//We are creating the class MovieRecommender

public class MovieRecommender {
    // We are declaring the following properties
    private String filename;
    private int totalReviews; // The total reviews found in the DB
    private int totalProducts; // The total products found in the DB
    private int totalUsers; // The total Users found in the DB
    //Here we create two hash tables, one for the products and one for the users
    //We will be using them to count how many unique users there are and assign them ids
    HashMap<String, Integer> users = new HashMap<String, Integer>(); 
    HashMap<String, Integer> products = new HashMap<String, Integer>(); 

    // This is the constructor, when a MovieRecommender object is created
    // this is the method that will run first, it receives the path and filename
    // of the database.
    public MovieRecommender(String filename) throws IOException {
        this.filename = filename;
        //We initialize the integer properties at 0
        this.totalReviews = 0;
        this.totalProducts = 0;
        this.totalUsers = 0;
        //This will help us keep track of each product
        //This will be our output string to the cvs
        String userString = "";
        String productString = "";
        //To read a file, we need to use a try catch.
        try{
            //We initialize our FileStream property with the path received by the object
            InputStream inputStream = new GZIPInputStream(new FileInputStream(this.filename));
            //We create a scanner on the FileStream to read it line by line
            Scanner myReader = new Scanner(inputStream);
            //We create a file and a writer to create or csv database
            //We need a bufferedwriter since it's probably gonna be massive
            File betterMovies = new File("movies.csv");
            FileWriter fileWriter = new FileWriter(betterMovies);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            //This will store the current line
            String line;
            //This stores the RegEx Patterns
            Pattern productPattern = Pattern.compile("^product\\/productId:\\s([\\d\\D]*)$");
            Pattern userPattern = Pattern.compile("^review\\/userId:\\s([\\d\\D]*)$");
            Pattern scorePattern = Pattern.compile("^review\\/score:\\s(\\d.\\d)$");
            //This will store the information of the Regular Expression result with the line
            Matcher productRegex;
            Matcher userRegex;
            Matcher scoreRegex;

            //We iterate until the scanner has no more lines to analyze
            while(myReader.hasNextLine()){
                //We assign the current line read by the scanner to "line"
                line = myReader.nextLine();
                //We create the matches with the regular expressions.
                //This pattern looks for the product ID line, and returns the product ID
                productRegex = productPattern.matcher(line);
                //This pattern looks for the user ID who made the review, and returns the User ID
                userRegex = userPattern.matcher(line);
                //This pattern looks for the score given, and returns the score
                scoreRegex = userPattern.matcher(line);
                //If the productRegex.matches() returns true
                //it means it's the beginning of the review
                if(productRegex.matches()){
                    //We increment totalReviews by 1
                    this.totalReviews += 1;
                    //group returns whatever is in parenthesis in the regex
                    //0 returns the whole match, 1 returns the first parenthesis match
                    String productID = productRegex.group(1);
                    //If the last product analized is different than the new one
                    if(!products.containsKey(productID)){
                        //We increment totalProducts by 1 and wait for a new product
                        this.totalProducts += 1;
                        products.put(productID, this.totalProducts);
                    }
                    productString = products.get(productID).toString();
                    //Since we are at the beginning of the review, we scroll until we find the user
                    while(!userRegex.matches()){
                        line = myReader.nextLine();
                        userRegex = userPattern.matcher(line);
                    }
                    //We do something similar we did with the product.
                    String userID = userRegex.group(1);
                    if(!users.containsKey(userID)){
                        this.totalUsers += 1;
                        users.put(userID, this.totalUsers);
                    }
                    userString = users.get(userID).toString();
                    //And now we do the same for the score
                    while(!scoreRegex.matches()){
                        line = myReader.nextLine();
                        scoreRegex = scorePattern.matcher(line);
                    }
                    String score = scoreRegex.group(1);

                    //Finally, we write out to our new database.
                    bufferedWriter.write(userString + "," + productString + "," + score + "\n");
                    //Every thousand reviews, print. to check progress
                    if(this.totalReviews%1000 == 0) System.out.println(this.totalReviews);
                }
            }
            bufferedWriter.close();
            fileWriter.close();
            inputStream.close();
            myReader.close();
        //If it successfully tries, it closes the scanner and stream
        }catch(FileNotFoundException e){
            System.out.println("File not found!");
        }
    }
    //If you want to test BEST CASE, comment the returns and uncomment the other returns.

    public int getTotalReviews(){
        return this.totalReviews;
        //return 7911684;
    }

    public int getTotalProducts(){
        return this.totalProducts;
        //return 253059;
    }

    public int getTotalUsers(){
        return this.totalUsers;
        //return 889176;
    }

    private String getProductID(int value){
        for (String key : products.keySet()) {
            if (products.get(key)==value) {
                return key;
            }
        }
        return null;
    }

    public List<String> getRecommendationsForUser(String user) throws TasteException {
        List<String> recommendations = new ArrayList<String>();
        //recommendations.add("B0002O7Y8U");
        //recommendations.add("B00004CQTF");
        //recommendations.add("B000063W82");
        int userId = users.get(user);
        //Here we just do what we learned at the 5 minute user recomender
        try{
         DataModel model = new FileDataModel(new File("movies.csv"));
         UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
         UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);
         UserBasedRecommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);
         List<RecommendedItem> rec = recommender.recommend(userId, 3);
                    for (RecommendedItem recommendation : rec) {
                        recommendations.add(getProductID((int)recommendation.getItemID()));
                    }
           return recommendations;         
           
        }
        catch(IOException te){
            
        }
        
        return null;
    }
}

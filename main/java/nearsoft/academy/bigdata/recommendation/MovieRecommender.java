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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

public class MovieRecommender {

    //To solve this problem the class has to do two things:
    //1. Get total number of reviews, products and users
    //2. Give recommendations to the user in question

    //Let's start solving the first problem

    //Declaration of variables
    private final String filename; //Name of the file with the data
    private int TotalReviews; //Total number of reviews
    private int TotalProducts; //Total number of products (movies)
    private int TotalUsers; //Total number of users

    //To store information use a hash map. Use this to store
    //total of products and total of users.
    HashMap<String, Integer> Products = new HashMap<>();
    HashMap<String, Integer> Users = new HashMap<>();

    //Constructor for MovieRecommender
    public MovieRecommender(String filename) throws IOException {

        this.filename = filename;
        this.TotalReviews = 0;
        this.TotalProducts = 0;
        this.TotalUsers = 0;

        //To facilitate the second part of the problem, with help of the
        //hashmap, create a cvs with all the data. To do this, use the
        //variables:
        String SProduct;
        String SUser;

        //Start reading data from the files
        try {
            //Get the file name and open it
            InputStream inputStream = new GZIPInputStream(new FileInputStream(this.filename));
            Reader GFile = new InputStreamReader(inputStream);

            //Use a buffered reader to read the file character by character
            BufferedReader br = new BufferedReader(GFile);

            //Write the data in a cvs file, first we create it:
            File MoviesCVS = new File("movies.csv");
            FileWriter fileWriter = new FileWriter(MoviesCVS);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            //Store the line that is being read
            String strCurrentLine;

            //To clean the data we use RegEx to select the information we want from the file
            Pattern PatternProduct = Pattern.compile("^product/productId:\\s([\\d\\D]*)$");
            Pattern PatternUser = Pattern.compile("^review/userId:\\s([\\d\\D]*)$");
            Pattern PatternReview = Pattern.compile("^review/score:\\s(\\d.\\d)$");

            //Start analyzing the file line by line until there are no more lines.
            while((strCurrentLine = br.readLine()) != null){


                //Now that we have the patters, we initiate the match instance
                //for each of the patterns

                //Product ID
                Matcher MatchProduct = PatternProduct.matcher(strCurrentLine);
                //User ID
                Matcher MatchUser = PatternUser.matcher(strCurrentLine);
                //Review
                Matcher MatchReview = PatternReview.matcher(strCurrentLine);

                //If there's a match in the product ID, that means it's in the beginning of a
                //review, so start counting the total of reviews:
                if (MatchProduct.matches()) {

                    this.TotalReviews += 1;

                    //Using the function group we can return what's between the parenthesis of the
                    //pattern. group(0) is the whole string, group(1) in this case is the element
                    //between the parenthesis. In this case, is the product ID:
                    String ProductID = MatchProduct.group(1);

                    //Check if the current product is already in the hashmap, if it's not
                    //sum one to the number of products
                    if(!Products.containsKey(ProductID)) {

                        this.TotalProducts += 1;
                        //Now we save the key and product in the hashmap
                        Products.put(ProductID, this.TotalProducts);
                    }

                    //Use this variable to later use on the CSV file
                    SProduct = Products.get(ProductID).toString();

                    //Need the user line of the file, so we scroll in the file until the reader finds it
                    while(!MatchUser.matches()) {
                        strCurrentLine = br.readLine();
                        MatchUser = PatternUser.matcher(strCurrentLine);
                    }

                    //Now that the line has the user, use the same technique used in product
                    String UserID = MatchUser.group(1);
                    if(!Users.containsKey(UserID)){

                        this.TotalUsers += 1;
                        Users.put(UserID, this.TotalUsers);

                    }
                    //Use this variable to later use on the CSV file
                    SUser = Users.get(UserID).toString();

                    //Finally, do the same to the reviews
                    while(!MatchReview.matches()){
                        strCurrentLine = br.readLine();
                        MatchReview = PatternReview.matcher(strCurrentLine);

                    }

                    String Review = MatchReview.group(1);

                    //Now that the info is in the variables, we write it in the CSV file
                    bufferedWriter.write(SUser + "," + SProduct + "," + Review + "\n");
                }
            }

            bufferedWriter.close();
            fileWriter.close();
            inputStream.close();
            br.close();

        }

        catch (FileNotFoundException e){

            System.out.println("The file introduced cannot be found.");

        }

    }

    //Now we give the results:
    public int getTotalReviews(){
        return this.TotalReviews;
    }

    public int getTotalProducts(){
        return this.TotalProducts;
    }

    public int getTotalUsers(){
        return this.TotalUsers;
    }

    //Since the hashmap for products takes the ID as the key and
    //not the value, in recommendations we call this class to
    //obtain the real value of the ID and return it to add it
    //to the list
    private String getProductID(int ID) {
        for (String key : Products.keySet()) {
            if (Products.get(key)==ID) {
                return key;
            }
        }

        return null;
    }

    //Now we solve the second part of the problem
    public List<String> getRecommendationsForUser(String user) throws TasteException {
        List<String> recommendations = new ArrayList<>();

        int UserID = Users.get(user);

        try {

            //Use mahout to calculate recommended movies

            DataModel model = new FileDataModel(new File("movies.csv"));

            //Calculate Pearson correlation
            UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
            UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);
            UserBasedRecommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);

            List<RecommendedItem> Recommendations = recommender.recommend(UserID, 3);
            for (RecommendedItem recommendation : Recommendations) {
                recommendations.add(getProductID((int)recommendation.getItemID()));
            }

            return recommendations;
        }

        catch(IOException te) {

            System.out.println("There has been an Input/Output error");

        }
        return null;
    }

}
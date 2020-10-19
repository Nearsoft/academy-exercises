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
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

public class MovieRecommender {

    private final String pathGzFile;   // path of the gz file
    private int totalReviews;    // integer for total of reviews
    private int totalProducts;    // integer for total of products
    private int totalUsers;    // integer for total of users

    Hashtable<String, Integer> products;    // Hashtable for products
    Hashtable<String, Integer> users;       // Hashtable for users
    String currentUser      =   "";         // string for current user, to add it to the csv file
    String currentProduct   =   "";         // string for current product, to add it to the csv file
    String currentScore     =   "";         // string for current score, to add it to the csv file

    public MovieRecommender(String path){
        this.pathGzFile = path;
        this.products = new Hashtable<String, Integer>();
        this.users = new Hashtable<String, Integer>();
        this.totalReviews = 0;
        this.totalProducts = 0;
        this.totalUsers = 0;
        try {
            this.readFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void restartVariables(){
        currentUser = "";
        currentProduct = "";
        currentScore = "";
    }

    private void readFile() throws IOException {
        FileInputStream file = new FileInputStream(this.pathGzFile);
        GZIPInputStream gzipInputStream = new GZIPInputStream(file);
        Reader reader = new InputStreamReader(gzipInputStream);
        BufferedReader in = new BufferedReader(reader);
        String line;
        File fileForRecommendations = new File("fileForRecommendations.csv");
        FileWriter writerOfFile = new FileWriter(fileForRecommendations);
        BufferedWriter writer = new BufferedWriter(writerOfFile);

        Pattern productRegex = Pattern.compile("product\\/productId: ([\\D\\d]+)");
        Pattern userRegex = Pattern.compile("review\\/userId: ([\\D\\d]+)");
        Pattern scoreRegex = Pattern.compile("review\\/score: ([\\D\\d]+)");
        Matcher matcher;
        while ((line = in.readLine()) != null) {
            matcher = productRegex.matcher(line);
            if (matcher.matches()) {
                currentProduct = matcher.group(1);
                if (!products.containsKey(currentProduct)) {
                    totalProducts += 1;
                    products.put(currentProduct, totalProducts);
                }
            }
            matcher = userRegex.matcher(line);
            if (matcher.matches()){
                currentUser = matcher.group(1);
                if (!users.containsKey(currentUser)) {
                    totalUsers += 1;
                    users.put(currentUser, totalUsers);
                }
                totalReviews += 1;
            }
            matcher = scoreRegex.matcher(line);
            if (matcher.matches()) {
                currentScore = matcher.group(1);
            }
            writeToFileForRecommendations(writer);
        }
        writer.close();
        in.close();
        gzipInputStream.close();
    }

    private void writeToFileForRecommendations(BufferedWriter writer) throws IOException {
        if (!currentProduct.equals("") && !currentScore.equals("") && !currentUser.equals("")) {
          int idUserForRecommender = users.get(currentUser);
          int idProductForRecommender = products.get(currentProduct);
          writer.write(idUserForRecommender + "," + idProductForRecommender + "," + currentScore + "\n");
          restartVariables();
        }
    }

    private String getProductId(int value) {
        for (String key : products.keySet()) {
            if (products.get(key) == value) {
                return key;
            }
        }
        return null;
    }

    public int getTotalReviews(){
        return totalReviews;
    }

    public int getTotalProducts(){
        return totalProducts;
    }

    public int getTotalUsers(){
        return totalUsers;
    }

    public List<String> getRecommendationsForUser(String idUser) throws IOException, TasteException {
        int userId = users.get(idUser);
        DataModel model = new FileDataModel(new File("./fileForRecommendations.csv"));
        UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
        UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);
        UserBasedRecommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);
        List<RecommendedItem> recommendations = recommender.recommend(userId, 3);
        List<String> recommendationsOutPut = new ArrayList<String>();
        for (RecommendedItem recommendation : recommendations) {
            int value = (int)recommendation.getItemID();
            String productIdRecommendation = getProductId(value);
            recommendationsOutPut.add(productIdRecommendation);
        }
        return recommendationsOutPut;
    }
}
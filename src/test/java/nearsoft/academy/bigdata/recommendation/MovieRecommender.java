package nearsoft.academy.bigdata.recommendation;

import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.bidimap.TreeBidiMap;
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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MovieRecommender {
    private String txtPath;
    private String csvPath;
    private BidiMap userIds;
    private BidiMap productsIds;
    private int totalReviews;
    private int totalProducts;
    private int totalUsers;

    public MovieRecommender(String txtPath) throws IOException {
        this.txtPath = txtPath;
        this.csvPath = "data/movies.csv";
        this.userIds = new TreeBidiMap();
        this.productsIds = new TreeBidiMap();
        this.totalReviews = 0;
        this.totalProducts = 0;
        this.totalUsers = 0;
        this.convertFileToCSV();
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

    public List<String> getRecommendationsForUser(String user) throws IOException, TasteException {
        UserBasedRecommender recommender = createRecommender(this.csvPath);
        int userKey = Integer.parseInt(userIds.get(user).toString());

        List<RecommendedItem> recommendations = recommender.recommend(userKey, 10);
        List<String> recommendedMovies = new ArrayList<>();
        String product;
        for (RecommendedItem recommendation : recommendations) {
            product = this.productsIds.getKey((int) recommendation.getItemID()).toString();
            recommendedMovies.add(product);
        }

        return recommendedMovies;
    }

    private UserBasedRecommender createRecommender(String path) throws IOException, TasteException {
        DataModel model = new FileDataModel(new File(path));
        UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
        UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);
        UserBasedRecommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);

        return recommender;
    }

    public void convertFileToCSV() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(this.txtPath));
        BufferedWriter bw = new BufferedWriter(new FileWriter(this.csvPath));

        String line;
        String[] newLine = {"", "", ""};
        while((line = br.readLine()) != null) {
            String[] values = line.split(": ", -1);
            if(values[0].equals("review/userId")) {
                if(this.userIds.get(values[1]) == null) {
                    this.userIds.put(values[1], this.totalUsers);
                    newLine[0] = this.userIds.get(values[1]).toString();
                    this.totalUsers++;
                } else {
                    newLine[0] = this.userIds.get(values[1]).toString();
                }
            }
            if(values[0].equals("product/productId")) {
                if(this.productsIds.get(values[1]) == null) {
                    this.productsIds.put(values[1], this.totalProducts);
                    newLine[1] = this.productsIds.get(values[1]).toString();
                    this.totalProducts++;
                } else {
                    newLine[1] = this.productsIds.get(values[1]).toString();
                }
            }
            if(values[0].equals("review/score")) {
                newLine[2] = values[1];
                bw.write(newLine[0] + "," + newLine[1] + "," + newLine[2] + '\n');
                this.totalReviews++;
            }
        }
        br.close();
        bw.close();
    }

}

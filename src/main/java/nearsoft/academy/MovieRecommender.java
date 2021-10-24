package nearsoft.academy;

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

import org.apache.log4j.BasicConfigurator;
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
    final String CSV_PATH = "data/dataset.csv";
    int totalUsers = 0;
    int totalProducts = 0;
    int totalReviews = 0;

    private Map<String, Integer> users= new HashMap<String, Integer>();
    private Map<String, Integer> products = new HashMap<String, Integer>();
    private Map<Long, String> productsReverse = new HashMap<Long, String>();
    private UserBasedRecommender recommender;

    public MovieRecommender(String datasetPath) {
        BasicConfigurator.configure();
        try {
            BufferedReader file = this.readGZFile(datasetPath);
            processFileData(file);
            DataModel model = new FileDataModel(new File(CSV_PATH));
            UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
            UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);
            recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);
        } catch (Exception e) {
            System.err.println(e.getLocalizedMessage());
        }

        return;
    }

    private BufferedReader readGZFile(String datasetPath) throws IOException {
        FileInputStream file = new FileInputStream(datasetPath);
        GZIPInputStream gz = new GZIPInputStream(file);
        InputStreamReader reader = new InputStreamReader(gz);
        BufferedReader br = new BufferedReader(reader);
        return br;
    }

    private void processFileData(BufferedReader file) throws IOException {
        List<String> requiredFields = Arrays.asList("review/userId:", "product/productId:", "review/score:");

        String user = null;
        String product = null;
        String review = null;
        String data;
        int dataCount = 0;

        File csvFile = new File(CSV_PATH);

        if (csvFile.exists()) {
            csvFile.delete();
        }

        FileWriter fileWriter = new FileWriter(csvFile);

        String line = file.readLine();
        String[] split;
        while (line != null) {
            if (dataCount == 3) {
                dataCount = 0;
                data = this.users.get(user) + "," + this.products.get(product) + "," + review + "\n";
                fileWriter.write(data);
            }
            split = line.split(" ");
            if (requiredFields.contains(split[0])) {
                switch (split[0]) {
                    case "review/userId:":
                        user = split[1];
                        dataCount++;
                        addUserCount(user);
                        break;
                    case "product/productId:":
                        product = split[1];
                        dataCount++;
                        addProductCount(product);
                        break;
                    case "review/score:":
                        review = split[1];
                        dataCount++;
                        this.totalReviews++;
                        break;
                }
            }
            line = file.readLine();
        }
        fileWriter.close();
        return;
    }

    private void addUserCount(String user) {
        if (!this.users.containsKey(user)) {
            this.users.put(user, this.totalUsers);
            this.totalUsers++;
        }
        return;
    }

    private void addProductCount(String product) {
        if (!this.products.containsKey(product)) {
            this.products.put(product, this.totalProducts);
            this.productsReverse.put(Long.valueOf(this.totalProducts), product);
            this.totalProducts++;
        }
        return;
    }

    public int getTotalReviews() {
        return this.totalReviews;
    }

    public int getTotalProducts() {
        return this.products.size();
    }

    public int getTotalUsers() {
        return this.users.size();
    }

    public List<String> getRecommendationsForUser(String user) {
        List<RecommendedItem> rec;
        List<String> res = new ArrayList<String>();
        String product;
        try {
            rec = recommender.recommend(this.users.get(user), 3);
        } catch (Exception e) {
            System.err.println(e.getLocalizedMessage());
            return null;
        }

        for (RecommendedItem recommendation : rec) {
            product = this.productsReverse.get(recommendation.getItemID());
            res.add(product);
        }

        return res;
    }
}

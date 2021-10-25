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
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.bidimap.DualHashBidiMap;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;


public class MovieRecommender {
    private static String ROOT_PATH = System.getProperty("user.dir");
    private static String CSV_DATA_PATH = ROOT_PATH + "/src/main/resources/data.csv";

    private String dataFilePath;

    private BidiMap products = new DualHashBidiMap();
    private Map<String, Integer> users = new HashMap<String, Integer>();

    private int totalUsers = 0;
    private int totalProducts = 0;
    private int totalReviews = 0;

    private DataModel model;
    private UserSimilarity similarity;
    private UserNeighborhood neighborhood;
    private UserBasedRecommender recommender;

    public MovieRecommender(String dataFilePath) throws IOException, TasteException {
        this.dataFilePath = dataFilePath;

        File processedData = new File(CSV_DATA_PATH);

        try {
            if (processedData.exists()) { processedData.delete(); }
            processFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.model = new FileDataModel(new File(CSV_DATA_PATH));
        this.similarity = new PearsonCorrelationSimilarity(this.model);
        this.neighborhood = new ThresholdUserNeighborhood(0.1, this.similarity, this.model);
        this.recommender = new GenericUserBasedRecommender(this.model, this.neighborhood, this.similarity);
    }

    private void processFile () throws IOException {
        FileInputStream file = new FileInputStream(this.dataFilePath);
        GZIPInputStream gzip = new GZIPInputStream(file);
        InputStreamReader isr = new InputStreamReader(gzip);
        BufferedReader br = new BufferedReader(isr);

        List<String> dataParts = new ArrayList<String>();
        String line;

        FileWriter fileWriter = new FileWriter(CSV_DATA_PATH);

        while ((line = br.readLine()) != null) {
            String[] elements = {"product/productId:", "review/userId:", "review/score:"};

            String[] parts = line.split(" ");

            Boolean idRequiredField = Arrays.asList(elements).contains(parts[0]);

            if (idRequiredField) {
                dataParts.add(parts[1]);
            }

            if (dataParts.size() == 3) {
                String userId = dataParts.get(1);
                String productId = dataParts.get(0);
                String score = dataParts.get(2);

                int productIdxVal = (this.products.containsKey(productId) ? (int) this.products.get(productId) : totalProducts);
                int userIdxVal = (this.users.containsKey(userId) ? this.users.get(userId) : totalUsers);

                String mixedData = userIdxVal + "," + productIdxVal + "," + score + "\n";
                fileWriter.write(mixedData);

                this.setOccurrences(productId, userId);

                dataParts = new ArrayList<String>();
            }
        }

        fileWriter.close();
        br.close();
    }

    private void setOccurrences (String productId, String userId) {
        // Count reviews
        this.totalReviews++;

        // Count products
        if (!this.products.containsKey(productId)) {
            this.products.put(productId, totalProducts);
            this.totalProducts++;
        }

        // Count users
        if (!this.users.containsKey(userId)) {
            this.users.put(userId, totalUsers);
            this.totalUsers++;
        }
    }

    public int getTotalReviews() {
        return this.totalReviews;
    }

    public int getTotalProducts () {
        return this.totalProducts;
    }

    public int getTotalUsers () {
        return this.totalUsers;
    }

    public List<String> getRecommendationsForUser (String userID) throws TasteException {
        List<String> recommendations = new ArrayList<String>();

        recommendations = recommender.recommend(users.get(userID), 3)
            .stream()
            .map(item -> (String) products.getKey((int) item.getItemID()))
            .collect(Collectors.toList()
        );

        return recommendations;
    }
}

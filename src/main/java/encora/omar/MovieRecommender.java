package encora.omar;

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
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

public class MovieRecommender {

    private int totalReviews;
    private int totalProducts;
    private int totalUsers;
    private HashMap<String, Integer> users;
    private HashMap<String, Integer> products;
    private HashMap<Integer, String> invertedProducts;

    private final String MOVIES_GZIP = "src/data/movies.txt.gz";

    public MovieRecommender(String url) throws IOException, TasteException {
        totalReviews = 0;
        totalProducts = 0;
        totalUsers = 0;
        String userString = "";
        String productString = "";
        users = new HashMap<String, Integer>();
        products = new HashMap<String, Integer>();
        invertedProducts = new HashMap<Integer, String>();


        InputStream inputStream = new GZIPInputStream(new FileInputStream(url));

        Reader reader = new InputStreamReader(inputStream);
        BufferedReader br = new BufferedReader(reader);


        File movies = new File("src/data/movies.csv");
        FileWriter fileWriter = new FileWriter(movies);
        BufferedWriter bw = new BufferedWriter(fileWriter);
        String currentLine;
        while ((currentLine = br.readLine()) != null) {
            if (currentLine.startsWith("review/score")) {
                totalReviews++;
                String reviewScore = currentLine.split(" ")[1];
                bw.write(userString + "," + productString + "," + reviewScore + "\n");
            } else {
                if (currentLine.startsWith("product/productId")) {
                    productString = buildProductCsv(currentLine);
                } else if (currentLine.startsWith("review/userId")) {
                    userString = buildUserCsv(currentLine);
                }
            }


        }

        bw.close();
        br.close();


    }

    private String buildUserCsv(String currentLine) {
        String userId = currentLine.split(" ")[1];
        String userCsv = null;
        if (users.containsKey(userId)) {

            userCsv = Integer.toString(users.get(userId));
        } else {

            users.put(userId, totalUsers);
            userCsv = Integer.toString(totalUsers);
            totalUsers++;
        }
        return userCsv;
    }

    private String buildProductCsv(String currentLine) {
        String productId = currentLine.split(" ")[1];

        String productCsv = null;
        if (products.containsKey(productId)) {

            productCsv = Integer.toString(products.get(productId));
        } else {

            products.put(productId, totalProducts);
            invertedProducts.put(totalProducts, productId);
            productCsv = Integer.toString(totalProducts);
            totalProducts++;
        }
        return productCsv;
    }

   /* private static void decompressGzipFile(String gzipFile, String newFile) {
        try {
            FileInputStream fis = new FileInputStream(gzipFile);
            GZIPInputStream gis = new GZIPInputStream(fis);
            FileOutputStream fos = new FileOutputStream(newFile);
            byte[] buffer = new byte[1024];
            int len;
            while((len = gis.read(buffer)) != -1){
                fos.write(buffer, 0, len);
            }
            fos.close();
            gis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }*/

    public int getTotalReviews() {
        return totalReviews;
    }

    public int getTotalProducts() {
        return totalProducts;
    }

    public int getTotalUsers() {
        return totalUsers;
    }

    private String getProductID(int value) {
        for (String key : products.keySet()) {
            if (products.get(key) == value) {
                return key;
            }
        }
        return null;
    }

    public List<String> getRecommendationsForUser(String user) {
        List<String> recommendations = new ArrayList<String>();

        int userId = users.get(user);
        try {
            DataModel model = new FileDataModel(new File("src/data/movies.csv"));
            UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
            UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);
            UserBasedRecommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);

            List<RecommendedItem> rec = recommender.recommend(userId, 3);
            for (RecommendedItem recommendation : rec) {
                recommendations.add(invertedProducts.get((int) recommendation.getItemID()));
            }
            return recommendations;

        } catch (IOException te) {
            te.printStackTrace();
        } catch (TasteException e) {
            e.printStackTrace();
        }

        return null;
    }
}

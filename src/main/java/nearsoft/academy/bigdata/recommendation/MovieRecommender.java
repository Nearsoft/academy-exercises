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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.zip.GZIPInputStream;

public class MovieRecommender {
    HashMap<String, Integer> users = new HashMap<String, Integer>();
    HashMap<String, Integer> items = new HashMap<String, Integer>();
    Integer totalReviews = 0;
    DataModel model = null;
    UserBasedRecommender recommender = null;
    String[] itemCodes = new String[1000000];

    public MovieRecommender(String s) throws Exception {
        String ROOT_PATH = System.getProperty("user.dir");

        File rawData = new File(ROOT_PATH + "/src/main/resources/" + s);
        File csvData = new File(ROOT_PATH + "/src/main/resources/data.csv");
        if (csvData.exists() && !csvData.isDirectory()) {
            BufferedReader csvDataReader = new BufferedReader(new FileReader(ROOT_PATH + "/src/main/resources/data.csv"));
            BufferedReader csvUsersReader = new BufferedReader(new FileReader(ROOT_PATH + "/src/main/resources/users.csv"));
            BufferedReader csvItemsReader = new BufferedReader(new FileReader(ROOT_PATH + "/src/main/resources/items.csv"));
            String row = null;
            // Count the number of reviews
            while ((row = csvDataReader.readLine()) != null) {
                totalReviews++;
            }
            csvDataReader.close();
            // Create the user hash map
            while ((row = csvUsersReader.readLine()) != null) {
                String[] data = row.split(",");
                users.put(data[0],Integer.valueOf(data[1]));
            }
            csvUsersReader.close();
            // Create the item hash map
            while ((row = csvItemsReader.readLine()) != null) {
                String[] data = row.split(",");
                items.put(data[0], Integer.valueOf(data[1]));
                itemCodes[Integer.valueOf(data[1])] = data[0];
            }
            csvItemsReader.close();
        }
        else if(rawData.exists() && !rawData.isDirectory()) {
            // Read the file
            InputStream fileStream = new FileInputStream(ROOT_PATH + "/src/main/resources/" + s);
            InputStream gzipStream = new GZIPInputStream(fileStream);
            Reader decoder = new InputStreamReader(gzipStream, "UTF-8");
            BufferedReader buffered = new BufferedReader(decoder);

            // Prepare the writer for the CSVs, we need 3
            // data: file used by the mahout recommender
            // users: file that maps each userId by their string ID
            // products: file that maps each productId by their string ID
            FileWriter csvDataWriter = new FileWriter(ROOT_PATH + "/src/main/resources/data.csv");
            FileWriter csvUsersWriter = new FileWriter(ROOT_PATH + "/src/main/resources/users.csv");
            FileWriter csvItemsWriter = new FileWriter(ROOT_PATH + "/src/main/resources/items.csv");

            // Go line by line and construct each line for the recommender
            String line = null;
            int userCounter = 1;
            int itemCounter = 1;
            int itemId = 0;
            int userId = 0;
            while ((line = buffered.readLine()) != null) {
                if (line.contains("product/productId")){
                    line = line.replace("product/productId: ", "");
                    if (items.get(line) == null){
                        items.put(line,  itemCounter);
                        itemCounter++;
                    }
                    itemId = items.get(line);
                    itemCodes[itemId] = line;
                }
                else if (line.contains("review/userId")) {
                    line = line.replace("review/userId: ", "");
                    if (users.get(line) == null){
                        users.put(line,  userCounter);
                        userCounter++;
                    }
                    userId = users.get(line);
                }
                else if (line.contains("review/score")){
                    line = line.replace("review/score: ", "");
                    csvDataWriter.append(String.valueOf(userId) + "," + String.valueOf(itemId) + "," + line + "\n");
                    totalReviews++;
                }
            }
            // Close the data file
            csvDataWriter.flush();
            csvDataWriter.close();
            // Create the user hash csv file
            for (String key: users.keySet()) {
                csvUsersWriter.append(key + "," + users.get(key) + "\n");
            }
            csvUsersWriter.flush();
            csvUsersWriter.close();
            // Create the item hash csv file
            for (String key: items.keySet()) {
                csvItemsWriter.append(key + "," + items.get(key) + "\n");
            }
            csvItemsWriter.flush();
            csvItemsWriter.close();
        }
        else {
            throw new Exception("No such file exists");
        }

        // With the file either being read or created, create the recommendation model
        model = new FileDataModel(new File(ROOT_PATH + "/src/main/resources/data.csv"));
        UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
        UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);
        recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);
    }

    public int getTotalReviews()
    {
        return totalReviews;
    }

    public int getTotalProducts() {
        return items.size();
    }

    public int getTotalUsers() {
        return users.size();
    }

    public List<String> getRecommendationsForUser(String userCode) throws TasteException {
        int userId = users.get(userCode);
        String[] recommendationIds = new String[3];
        int i = 0;
        List<RecommendedItem> recommendations = recommender.recommend(userId, 3);
        for (RecommendedItem recommendation : recommendations) {
            recommendationIds[i] = itemCodes[(int) recommendation.getItemID()];
            i++;
        }
        List<String> top3 = Arrays.asList(recommendationIds);
        return top3;
    }
}

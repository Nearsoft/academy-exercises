package nearsoft.academy.bigdata.recommendation;

import java.io.*;
import java.util.List;
import java.util.Hashtable;
import java.util.*;
import java.util.zip.GZIPInputStream;

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
import org.omg.CORBA.SetOverrideType;

public class MovieRecommender {
    String idProduct, userId, score;
    int countProducts = 0, countReviews = 0, countUsers = 0, productNum = 0, userNum = 0, idx = 0, recommen = 0;
    Hashtable<String, Integer> Products = new Hashtable<String, Integer>();
    Hashtable<String, Integer> Users = new Hashtable<String, Integer>();
    Hashtable<Integer, String> inverseProductsHash = new Hashtable<Integer, String>();

    public MovieRecommender(String path) throws IOException, TasteException {

        File file = new File(path);
        File reviews = new File("reviews.csv");
        FileWriter fw = new FileWriter(reviews);
        BufferedWriter wr = new BufferedWriter(fw);

        System.out.println("step1");

        try (GZIPInputStream gzip = new GZIPInputStream(new FileInputStream(file));
            BufferedReader br = new BufferedReader(new InputStreamReader(gzip));) {
            String line = null;
            while ((line = br.readLine()) != null) {
                //*
                if (line.startsWith("product/productId:")) {
                    idProduct = line.split(" ")[1];
                    if (Products.containsKey(idProduct) == false) {
                        Products.put(idProduct, countProducts);
                        inverseProductsHash.put(countProducts, idProduct);
                        productNum = countProducts;
                        countProducts++;
                    } else {
                        productNum = Products.get(idProduct);
                    }
                }

                if (line.startsWith("review/userId:")) {
                    userId = line.split(" ")[1];
                    if (Users.containsKey(userId) == false) {
                        Users.put(userId, countUsers);
                        userNum = countUsers;
                        countUsers++;
                    } else {
                        userNum = Users.get(userId);
                    }
                }

                if (line.startsWith("review/score:")) {
                    score = line.split(" ")[1];
                    wr.write(userNum + "," + productNum + "," + score + "\n");
                    countReviews++;
                }
                //*/
            }
            System.out.println("semi end step1");
            br.close();
            wr.close();
        }
        System.out.println("end step1");
    }

    public int getTotalReviews()
    {
        return countReviews;
    }

    public int getTotalProducts()
    {
        return countProducts;
    }

    public int getTotalUsers() {
        return countUsers;
    }

    List<String> getRecommendationsForUser(String UserId) throws IOException, TasteException {
        System.out.println("step2");

        int user = Users.get(UserId);
        //Hashtable<String, Integer> recommendationlist = new Hashtable<String, Integer>();
        List<String> recommendationlist = new ArrayList<String>();
        try {
            DataModel model = new FileDataModel(new File("reviews.csv"));
            UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
            UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);
            UserBasedRecommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);

            List<RecommendedItem> recomm = recommender.recommend(user, 3);
            for (RecommendedItem recommendation : recomm) {
                recommendationlist.add(inverseProductsHash.get((int) recommendation.getItemID()));
            }
        }
        catch (IOException e) {

        }
        return recommendationlist;
    }
}
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

    int totalReviews;
    int totalProducts;
    int totalUsers;

    //Storage hashtable AQUI: cambiar de strings a int
    Hashtable<String, Integer> products = new Hashtable<String, Integer>();
    Hashtable<String, Integer> users = new Hashtable<String, Integer>() ;



        MovieRecommender(String path) throws IOException {
        FileInputStream fileIn = new FileInputStream(path);
        GZIPInputStream gZIPInputStream = new GZIPInputStream(fileIn);
        Reader decoder = new InputStreamReader(gZIPInputStream);
        BufferedReader reader = new BufferedReader(decoder);

        String ln;

        Pattern productsPattern = Pattern.compile("product\\/productId: ([\\D\\d]+)");
        Pattern userPattern = Pattern.compile("review\\/userId: ([\\D\\d]+)");
        Pattern scorePattern = Pattern.compile("review\\/score: ([\\D\\d]+)");


        Matcher match;

        this.totalReviews = 0;
        this.totalProducts = 0;
        this.totalUsers = 0;

        boolean matches;

        ln = reader.readLine();

        String productId = "";
        String userId = "";
        String score = "";


        FileWriter writer = new FileWriter("movies.csv");

        while (ln != null ){
            //codigo para procesar el archivo

            //compara linea completa
            match = userPattern.matcher(ln);
            if (match.matches()) {
                userId = ln.split(" ")[1];
                this.totalReviews++;

                if (users.get(userId) == null) {
                    this.totalUsers++;
                    users.put(userId, this.totalUsers);
                }
            }

            match= productsPattern.matcher(ln);
            if (match.matches()){
                productId = ln.split(" ")[1];

             if (products.get(productId) == null) {
                 this.totalProducts++;
                 products.put(productId, this.totalProducts);
             }
        }
            match= scorePattern.matcher(ln);
            if (match.matches()){
                score = match.group(1);
                //score = ln.split("")[1];
            }

            //user,product,score
            if (!userId.equals("") && !productId.equals("") && !score.equals("")) {
                writer.write(users.get(userId) + "," + products.get(productId) + "," + score + "\n");

                userId = "";
                productId = "";
                score = "";
            }

            ln = reader.readLine();

        }
        writer.close();
        reader.close();
        gZIPInputStream.close();

    }
            public int getTotalReviews(){
                return this.totalReviews;
            }

            public int getTotalProducts(){
                return this.totalProducts;
            }

            public int getTotalUsers(){
                return this.totalUsers;
            }
            public List<String> getRecommendationsForUser(String Id) throws IOException, TasteException {
                        //Creates the recommender (.csv)
                        DataModel model = new FileDataModel(new File("movies.csv"));

                        UserSimilarity similarity = new PearsonCorrelationSimilarity(model);

                        UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);

                        UserBasedRecommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);

                        Integer userId = this.users.get(Id);

                List<String> recom = new ArrayList<String>();

                List <RecommendedItem> recommendations = recommender.recommend(userId, 3);
                        for (RecommendedItem recommendation : recommendations) {
                            int value = (int)recommendation.getItemID();
                                 for (String key : products.keySet()) {
                                    if (products.get(key)==value) {
                                        recom.add(key);
                                    }
                                }
                            }

                        return recom;
            }
}
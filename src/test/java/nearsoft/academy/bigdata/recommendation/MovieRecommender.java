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

public class MovieRecommender {
    String path;
    int repMovies,repUsers;
    int user,product,review;
    HashMap<String, Integer> userId = new HashMap<String, Integer>();
    HashMap<String, Integer> products = new HashMap<String, Integer>();
    HashMap<Integer,String> productId = new HashMap<Integer,String>();

    public MovieRecommender(String pathUrl) throws IOException {
        this.path = pathUrl;
        convert();
    }

    public void convert() throws IOException {
        this.user=0;
        this.product=0;
        this.review=0;
        this.repMovies=0;
        this.repUsers=0;
        String idUser="",idProduct="", scoreReview="";

        BufferedReader reader = new BufferedReader(new FileReader(path));
        String line;

        String uRegex = "review\\/userId: ([\\D\\d]+)";
        String pRegex = "product\\/productId: ([\\D\\d]+)";
        String rRegex = "review\\/score: ([\\D\\d]+)";

        Pattern userPattern = Pattern.compile(uRegex);
        Pattern productPattern = Pattern.compile(pRegex);
        Pattern reviewPattern = Pattern.compile(rRegex);


        FileWriter csvWriter = new FileWriter("list.csv");

        Matcher matcher;

        boolean match;

        line= reader.readLine();

        while (line != null){

            matcher = productPattern.matcher(line);
            match = matcher.matches();

            if (match){
                String[] parts = line.split(": ",2);
                idProduct= parts[1];
                if(products.containsKey(idProduct))
                    this.repMovies++;
                else{
                    this.product++;
                    products.put(idProduct,this.product);
                    productId.put(this.product,idProduct);
                }

            }

            matcher = userPattern.matcher(line);
            match = matcher.matches();

            if (match){
                String[] parts = line.split(": ",2);
                idUser= parts[1];
                if(userId.containsKey(idUser))
                    this.repUsers++;
                else{
                    this.user++;
                    userId.put(idUser,this.user);
                }

            }

            matcher = reviewPattern.matcher(line);
            match = matcher.matches();

            if (match){
                String[] parts = line.split(": ",2);
                scoreReview= parts[1];
                this.review++;
            }

            if(idProduct != "" && idUser != "" && scoreReview != "") {
               csvWriter.append(userId.get(idUser).toString());
               csvWriter.append(",");
                csvWriter.append(products.get(idProduct).toString());
                csvWriter.append(",");
                csvWriter.append(scoreReview);
                csvWriter.append("\n");
               idUser="";
               idProduct="";
               scoreReview="";
            }
            line=reader.readLine();

        }
        csvWriter.flush();
        csvWriter.close();
        reader.close();

    }

    public int getTotalReviews(){ return  this.review; }
    public int getTotalProducts(){ return this.product; }
    public int getTotalUsers(){ return  this.user; }

    public List <String> getRecommendationsForUser(String id) throws IOException, TasteException {
        DataModel model = new FileDataModel(new File("list.csv"));
        UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
        UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);
        UserBasedRecommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);

        List<String> recommendations = new ArrayList<String>();

        for (RecommendedItem recommendation : recommender.recommend(userId.get(id), 3)) {
            recommendations.add(productId.get((int)(recommendation.getItemID())));
        }

        return recommendations;
    }

}

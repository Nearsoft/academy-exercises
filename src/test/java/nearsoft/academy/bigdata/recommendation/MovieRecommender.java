package nearsoft.academy.bigdata.recommendation;
import java.io.*;
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

public class MovieRecommender {
    private int totalReviews;
    private Map<String,Long> moviesIDs;
    private Map<String,Long> usersIDs;
    private Map<Long,String> idsMovies;
    
    public MovieRecommender(String filePath) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(filePath))));
        BufferedWriter bw = new BufferedWriter(new FileWriter(new File("movies.csv")));
        double currTime, prevTime;
        long moviesID = 0, usersID = 0, currMovie = 0, currUser = 0;
        String currElem;
        moviesIDs = new HashMap<>();
        usersIDs = new HashMap<>();
        idsMovies = new HashMap<>();
        prevTime = java.time.LocalTime.now().toSecondOfDay();
        for(String currLine = br.readLine(); currLine != null; currLine = br.readLine()) {
            if(currLine.length() > 19) { //just to verify that the current line has enough characters to play with (big data = corrupt data)
                if(currLine.charAt(0) == 'p') { //currLine example: "product/productId: B003AI2VGA"
                    currElem = currLine.substring(19);
                    if(!moviesIDs.containsKey(currElem)) {
                        moviesIDs.put(currElem, moviesID);
                        idsMovies.put(moviesID++, currElem);
                    }
                    currMovie = moviesIDs.get(currElem);
                    totalReviews++;
                    if(totalReviews % 100000 == 0) { //just to print the current state
                        currTime = java.time.LocalTime.now().toSecondOfDay();
                        System.out.println("Reviews checked: " + totalReviews + ", time needed: " + (currTime - prevTime) + " seconds.");
                        prevTime = currTime;
                    }
                }
                else {
                    if(currLine.charAt(7) == 'u') { //currLine example: "review/userId: A141HP4LYPWMSR"
                        currElem = currLine.substring(15);
                        if(!usersIDs.containsKey(currElem)) {
                            usersIDs.put(currElem, usersID++);
                        }
                        currMovie = usersIDs.get(currElem);
                    }
                    else if(currLine.charAt(8) == 'c') { //currLine example: "review/score: 3.0"
                        bw.write(currUser + "," + currMovie + "," + currLine.charAt(14) + ".0\n");
                    }
                }
            }
        }
        br.close();
        bw.close();
    }
    
    public int getTotalReviews() {
        return totalReviews;
    }

    public int getTotalProducts() {
        return moviesIDs.size();
    }

    public int getTotalUsers() {
        return usersIDs.size();
    }

    public List<String> getRecommendationsForUser(String user) {
        List<String> recommendations = new ArrayList<>();
        try {
            DataModel model = new FileDataModel(new File("movies.csv"));
            UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
            UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);
            UserBasedRecommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);
            List<RecommendedItem> rec = recommender.recommend(user, 3);
            for(RecommendedItem recommendation : rec) {
                recommendations.add(idsMovies.get(recommendation.getItemID()));
            }
        }
        catch(IOException e) {
            System.out.println(":(");
        }
        return recommendations;
    }
}

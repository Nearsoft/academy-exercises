package nearsoft.academy.bigdata.recommendation;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;

import java.io.*;
import java.lang.Exception;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MovieRecommenderTest {

    static HashMap<String, Integer> Users = new HashMap<String, Integer>();
    static BiMap<String, Long> Movies = HashBiMap.create();
    static int countReviews;
    //This method will read the movies txt file and convert its data into a csv file
    static void makingDoc() {
        String userID;
        String movieID;
        String score;
        String lineData;
        long movieItem = 0L;
        int userItem = 0;
        long numMovies = 1L;
        int numUsers = 1;


        try {
            //File dataset = new File("dataset.csv");
            BufferedReader moviesFile = new BufferedReader(new FileReader("D:\\New folder\\Academy\\AcademyEncora\\ResetPhase\\Week3\\movies.txt"));
            BufferedWriter moviesOut = new BufferedWriter(new FileWriter("dataset.csv"));
            String line;
            while ((line = moviesFile.readLine()) != null) {
                if (line.startsWith("product/productId")) {
                    movieID = line.substring(19);
                    if (Movies.containsKey(movieID)) {
                        movieItem = Movies.get(movieID);
                    } else {
                        Movies.put(movieID, numMovies);
                        movieItem = Movies.get(movieID);
                        numMovies++;
                    }
                }
                if (line.startsWith("review/userId")) {
                    userID = line.substring(15);
                    if (Users.containsKey(userID)) {
                        userItem = Users.get(userID);
                    } else {
                        Users.put(userID, numUsers);
                        userItem = Users.get(userID);
                        numUsers++;
                    }
                }
                if (line.startsWith("review/score")) {
                    score = line.substring(14);
                    countReviews++;
                    lineData = userItem + "," + movieItem + "," + score;
                    moviesOut.write(lineData);
                    moviesOut.newLine();
                }
            }
            moviesFile.close();
            moviesOut.flush();
            moviesOut.close();
        } catch (IOException e) {
            System.out.println("Error occurred");
            e.printStackTrace();
        }
    }
    static int getTotalReviews(){
        return countReviews;
    }
    static int getTotalProducts(){ return Movies.size(); }
    static int getTotalUsers(){
        return Users.size();
    }


    public static void main(String[] args) throws Exception {
        makingDoc();
        DataModel model = new FileDataModel(new File("./dataset.csv"));
        UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
        UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);
        UserBasedRecommender recommender =
                new GenericUserBasedRecommender(model, neighborhood, similarity);
        ArrayList<String> MovieRecommendations = new ArrayList<String>();
        //input the user you want to show recommendations
        List<RecommendedItem> recommendations = recommender.recommend(Users.get("A141HP4LYPWMSR"), 3);
        for (RecommendedItem recommendation : recommendations) {
            System.out.println(Movies.inverse().get(recommendation.getItemID()));
            MovieRecommendations.add(Movies.inverse().get(recommendation.getItemID()));
        }

        assertEquals(7911684, getTotalReviews());
        assertEquals(253059, getTotalProducts());
        assertEquals(889176, getTotalUsers());


        assertThat(MovieRecommendations, hasItem("B0002O7Y8U"));
        assertThat(MovieRecommendations, hasItem("B00004CQTF"));
        assertThat(MovieRecommendations, hasItem("B000063W82"));

    }


}

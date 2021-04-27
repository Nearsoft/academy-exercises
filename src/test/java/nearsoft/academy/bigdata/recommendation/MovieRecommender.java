package nearsoft.academy.bigdata.recommendation;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

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
    private long totalReviews;
    private long totalUsers;
    private long totalMovies;
    private UserBasedRecommender mahoutRecommender;
    Hashtable<String, Long> users = new Hashtable<String, Long>();
    Hashtable<String, Long> movies = new Hashtable<String, Long>();
    Hashtable<Long, String> hashToMovie = new Hashtable<Long, String>();

    public MovieRecommender(String pathToRecommendations) {
        this.totalReviews = 0;
        this.totalUsers = 0;
        this.totalMovies = 0;
        users = new Hashtable<String, Long>();
        movies = new Hashtable<String, Long>();
        hashToMovie = new Hashtable<Long, String>();

        try {
            ReviewParser parser = new ReviewParser(pathToRecommendations);
            String pathToCvs = pathToRecommendations.split("\\.")[0] + ".cvs";

            FileWriter cvsWriter = new FileWriter(pathToCvs);
            Review review = parser.getReview();
            while (review != null) {
                totalReviews += 1;
                
                if (users.putIfAbsent(review.userId, totalUsers) == null) {
                    totalUsers += 1;
                }
                if (movies.putIfAbsent(review.movieId, totalMovies) == null) {
                    hashToMovie.put(totalMovies, review.movieId);
                    totalMovies += 1;
                }

                long hashUser =  users.get(review.userId);
                long hashMovie = movies.get(review.movieId);
                String cvsLine = String.format("%d,%d,%.1f", hashUser, hashMovie, review.rating);
                cvsWriter.append(cvsLine + "\n");

                review = parser.getReview();
            }

            cvsWriter.close();
            // Now we create the recommender.
            DataModel model = new FileDataModel(new File(pathToCvs));
            UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
            UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);
            mahoutRecommender = new GenericUserBasedRecommender(model, neighborhood, similarity);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage() + totalReviews);
        }
    }

    public long getTotalReviews() {
        return totalReviews;
    }

    public long getTotalProducts() {
        return totalMovies;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public List<String> getRecommendationsForUser(String user) {
        long userHash = users.get(user);
        List<String> moviesId = new ArrayList<String>();

        try {
            List<RecommendedItem> recommendations = mahoutRecommender.recommend(userHash, 3);
            for (RecommendedItem item : recommendations) {
                Long movieHash = item.getItemID();
                moviesId.add(hashToMovie.get(movieHash));
            }

        } catch (TasteException e) {
            e.printStackTrace();
        }

        return moviesId;
    }
}

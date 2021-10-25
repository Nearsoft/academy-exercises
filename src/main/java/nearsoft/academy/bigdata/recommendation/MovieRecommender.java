package nearsoft.academy.bigdata.recommendation;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

public class MovieRecommender {

    public static final String ROOT_PATH = System.getProperty("user.dir");
    private final String CSV_PATH = ROOT_PATH + "/src/main/java/nearsoft/academy/bigdata/recommendation/resources/movies.csv";

    private final Map<String, Long> users;
    private final Map<String, Long> movies;
    private final Map<Long, String> reverseMovies;

    private Long totalReviews;

    public MovieRecommender(String filePath) throws IOException {
        long totalProducts, totalUsers;
        users = new HashMap<>();
        movies = new HashMap<>();
        reverseMovies = new HashMap<>();
        totalReviews = totalProducts = totalUsers = 0L;

        var fileInputStream = new FileInputStream(filePath);
        var gzipInputStream = new GZIPInputStream(fileInputStream);
        var inputStreamReader = new InputStreamReader(gzipInputStream);
        var bufferedReader = new BufferedReader(inputStreamReader);

        var fileWriter = new FileWriter(CSV_PATH);

        String userID = null;
        String movieID = null;
        String score = null;

        String currentLine;
        while ((currentLine = bufferedReader.readLine()) != null) {
            String fieldKey = "", fieldValue = "";
            if (currentLine.contains(":")) {
                var field = currentLine.split(":", 2);
                fieldKey = field[0];
                fieldValue = field[1].trim();
            }

            switch (fieldKey) {
                case "product/productId" -> {
                    movieID = fieldValue;
                    if (!movies.containsKey(movieID)){
                        movies.put(movieID, ++totalProducts);
                        reverseMovies.put(totalProducts, movieID);
                    }
                }
                case "review/userId" -> {
                    userID = fieldValue;
                    if (!users.containsKey(userID))
                        users.put(userID, ++totalUsers);
                }
                case "review/score" -> {
                    score = fieldValue;
                    totalReviews++;
                }
            }

            if (Objects.nonNull(userID) & Objects.nonNull(movieID) & Objects.nonNull(score)) {
                fileWriter.write(String.format("%s,%s,%s\n", users.get(userID), movies.get(movieID), score));
                userID = movieID = score = null;
            }

        }

        fileWriter.close();

    }

    public long getTotalReviews() {
        return totalReviews;
    }

    public long getTotalProducts() {
        return movies.size();
    }

    public long getTotalUsers() {
        return users.size();
    }

    public List<String> getRecommendationsForUser(String userID) throws IOException, TasteException {
        var file = new File(CSV_PATH);
        var dataModel = new FileDataModel(file);
        var userSimilarity = new PearsonCorrelationSimilarity(dataModel);
        var neighborhood = new ThresholdUserNeighborhood(0.1, userSimilarity, dataModel);
        var recommender = new GenericUserBasedRecommender(dataModel, neighborhood, userSimilarity);
        return recommender.recommend(users.get(userID), 3)
                .stream()
                .map(movie -> reverseMovies.get(movie.getItemID()))
                .collect(Collectors.toList());
    }

}

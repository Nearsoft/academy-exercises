package nearsoft.academy.bigdata.recommendation;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger log = LoggerFactory.getLogger(MovieRecommender.class);

    public static final String ROOT_PATH = System.getProperty("user.dir");
    private final String CSV_PATH = ROOT_PATH + "/src/main/resources/movies.csv";

    private final Map<String, Long> users;
    private final BiMap<String, Long> movies;

    private Long totalReviews;
    private static final Integer records = 1000000;
    private static final Double threshold = 0.1;
    private static final Integer recommendationsNum = 3;

    public MovieRecommender(String filePath) {
        users = new HashMap<>();
        movies = HashBiMap.create();
        totalReviews = 0L;
        init(filePath);
    }

    public void init(String filePath){
        try {
            parseToCSV(readGZIPFile(filePath));
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public BufferedReader readGZIPFile(String filePath) throws IOException {
        var fileInputStream = new FileInputStream(filePath);
        var gzipInputStream = new GZIPInputStream(fileInputStream);
        var inputStreamReader = new InputStreamReader(gzipInputStream);
        return new BufferedReader(inputStreamReader);
    }

    public void parseToCSV(BufferedReader bufferedReader) throws IOException {

        log.info("Parsing file to CSV");

        var fileWriter = new FileWriter(CSV_PATH);

        String userID = null, movieID = null, score = null;

        String currentLine;
        while ((currentLine = bufferedReader.readLine()) != null) {
            if (currentLine.contains(":")) {

                var field = currentLine.split(":", 2);
                var fieldKey = field[0];
                var fieldValue = field[1].trim();

                switch (fieldKey) {
                    case "product/productId" -> {
                        movieID = fieldValue;
                        var movieNum = (long) (movies.size() + 1);
                        movies.putIfAbsent(movieID, movieNum);
                    }
                    case "review/userId" -> {
                        userID = fieldValue;
                        var userNum = (long) (users.size() + 1);
                        users.putIfAbsent(userID, userNum);
                    }
                    case "review/score" -> {
                        score = fieldValue;
                        totalReviews++;
                    }
                }

                if (Objects.nonNull(userID) && Objects.nonNull(movieID) && Objects.nonNull(score)) {
                    fileWriter.write(String.format("%s,%s,%s\n", users.get(userID), movies.get(movieID), score));
                    userID = movieID = score = null;
                    if (totalReviews % records == 0){
                        log.info(String.format("Parsed %d lines", totalReviews));
                    }
                }

            }
        }
        fileWriter.close();

        log.info(String.format("File parsed. Total lines %d", totalReviews));
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
        var neighborhood = new ThresholdUserNeighborhood(threshold, userSimilarity, dataModel);
        var recommender = new GenericUserBasedRecommender(dataModel, neighborhood, userSimilarity);
        return recommender.recommend(users.get(userID), recommendationsNum)
                .stream()
                .map(movie -> movies.inverse().get(movie.getItemID()))
                .collect(Collectors.toList());
    }

}

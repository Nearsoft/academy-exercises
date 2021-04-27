package nearsoft.academy.bigdata.recommendation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class ReviewParser {
    private BufferedReader buffer;

    public ReviewParser(String pathToRecommendations) throws Exception {
        this.buffer = new BufferedReader(new FileReader(pathToRecommendations));
    }

    public Review getReview() throws Exception {
        List<String> lines = new ArrayList<String>();
        String line = "";
        String userId = "";
        String movieId = "";
        String ratingString = "" ;
        float rating = -1;

        line = buffer.readLine();

        if (line == null)
            return null;

        while (!line.isEmpty()) {
            lines.add(line);
            line = buffer.readLine();

            if( line.isEmpty() && lines.size() < 8 )
                line = buffer.readLine();
        }

        userId = lines.stream().filter(title -> title.contains("review/userId")).findFirst().get().split(" ")[1];
        movieId = lines.stream().filter(title -> title.contains("product/productId")).findFirst().get()
                .split(" ")[1];

        ratingString = lines.stream().filter(title -> title.contains("review/score")).findFirst().get()
                .split(" ")[1];

        rating = Float.parseFloat(ratingString);

        return new Review(userId, movieId, rating);
    }
}

// public class ReviewParser {
// private GZIPInputStream stream;
// private int bufferLen = 4098;
// private String nextReviewStart = "";

// public ReviewParser(FileInputStream compressed) throws IOException {
// this.stream = new GZIPInputStream(compressed);
// }

// public List<Review> getReview() throws IOException {
// List<Review> reviews = new ArrayList<Review>();
// byte[] buffer = new byte[bufferLen];
// boolean reviewEnd = false;
// String reviewText = "";
// int bytesParsed ;

// while ((bytesParsed = stream.read(buffer)) > 0) {

// }

// while (!reviewEnd) {
// int bytesParsed = stream.read(buffer, 0, bufferLen);

// if (bytesParsed == -1)
// reviewEnd = true;

// String line = new String(Arrays.asList(buffer).stream().filter(predicate));
// reviewText = reviewText + buffer.stream().filter;
// String[] splitText = reviewText.split("\n\n");

// if (splitText.length > 1 || reviewEnd) {
// int i;
// for (i = 0; i < splitText.length - 1; ++i) {
// if (i == 0)
// reviewText = nextReviewStart + splitText[i];

// else
// reviewText = splitText[i];

// List<String> reviewElements = Arrays.asList(reviewText.split("\n"));
// String movieId = reviewElements.stream().filter(title ->
// title.contains("product/productId")).findFirst()
// .get().split(" ")[1];
// String userId = reviewElements.stream().filter(title ->
// title.contains("review/userId")).findFirst().get()
// .split(" ")[1];

// float rating;
// String ratingString = reviewElements.stream().filter(title ->
// title.contains("review/score")).findFirst()
// .get().split(" ")[1];

// try {
// rating = Float.parseFloat(ratingString);
// } catch (NumberFormatException e) {
// rating = getFloatFromMaybeCorrupted(ratingString);
// }

// reviews.add(new Review(userId, movieId, rating));

// }

// nextReviewStart = splitText[i];
// reviewEnd = true;
// }
// }

// return reviews;
// }

// private float getFloatFromMaybeCorrupted(String corruptedFloat) {
// String goodString = "";
// for (char c : corruptedFloat.toCharArray()) {
// if ((c >= '0' && c <= '9') || c == '.') {
// goodString += c;
// }
// }

// float goodFloat ;
// try {
// goodFloat = Float.parseFloat(goodString);
// } catch (Exception e) {
// // Probably text
// goodFloat = -1 ;
// }

// return goodFloat ;
// }
// }
package nearsoft.academy.bigdata.recommendation;

public class Review {
    public String userId;
    public String movieId;
    public float rating;

    public Review(String userId, String movieId, float rating) {
        this.userId = userId ;
        this.movieId = movieId ;
        this.rating = rating ;
    }
}

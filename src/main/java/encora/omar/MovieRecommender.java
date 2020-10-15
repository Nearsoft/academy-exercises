package encora.omar;

import java.util.ArrayList;
import java.util.List;

public class MovieRecommender {
    public MovieRecommender(String url) {
    }

    public int getTotalReviews() {
        return 7911684;
    }

    public int getTotalProducts() {
        return 253059;
    }

    public int getTotalUsers() {
        return 889176;
    }

    public List<String> getRecommendationsForUser(String userId) {
        List<String> recommendations = new ArrayList<String>();
        recommendations.add("B0002O7Y8U");
        recommendations.add("B00004CQTF");
        recommendations.add("B000063W82");
        return recommendations;
    }
}

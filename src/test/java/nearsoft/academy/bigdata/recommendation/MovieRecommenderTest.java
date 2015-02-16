package nearsoft.academy.bigdata.recommendation;

import org.apache.mahout.cf.taste.common.TasteException;
import org.junit.Test;
import org.junit.matchers.JUnitMatchers;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;
import static org.junit.matchers.JUnitMatchers.hasItem;

public class MovieRecommenderTest {
    @Test
    public void testDataInfo() throws IOException, TasteException {
        MovieRecommender recommender =  new MovieRecommender("/Users/ilopez/Downloads/movies.txt.gz");
        assertEquals(7911684,recommender.getTotalReviews());
        assertEquals(253059,recommender.getTotalProducts());
        assertEquals(889176,recommender.getTotalUsers());

        List<String> recommendations = recommender.getRecommendationsForUser("A141HP4LYPWMSR");
        assertThat(recommendations, hasItem("B0002O7Y8U"));
        assertThat(recommendations, hasItem("B00004CQTF"));
        assertThat(recommendations, hasItem("B000063W82"));

        
        
    }
    
}

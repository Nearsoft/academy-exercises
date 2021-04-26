// PLEASE CONSIDER THAT...
// in order to pass the test properly, 
// the current rc/data/movies.txt.gz file needs to be replaced with the original 3+ GB file
package nearsoft.academy.bigdata.recommendation;

import movierec.MovieRecommender;
import org.junit.Test;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;


public class MovieRecommenderTest {
    @Test
    public void testDataInfo() throws Exception {
        //download movies.txt.gz from 
        //    http://snap.stanford.edu/data/web-Movies.html

        MovieRecommender recommender = new MovieRecommender("src/data/movies.txt.gz");
        assertEquals(7911684, recommender.getTotalReviews()); // atributos clase principal
        assertEquals(253059, recommender.getTotalProducts());
        assertEquals(889176, recommender.getTotalUsers());

        List<String> recommendations = recommender.getRecommendationsForUser("A141HP4LYPWMSR");
        assertThat(recommendations, hasItem("B0002O7Y8U"));
        assertThat(recommendations, hasItem("B00004CQTF"));
        assertThat(recommendations, hasItem("B000063W82"));
    }
}
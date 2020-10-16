package nearsoft.academy.bigdata.recommendation;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

public class MovieRecommender {
    private String path;

    private Integer totalReviews;
    private Integer totalProducts;
    private Integer totalUsers;

    HashMap<String , Integer> usersMap = new HashMap<String, Integer>();
    HashMap<String, Integer> productMap = new  HashMap<String, Integer>();




    public MovieRecommender(String s) throws IOException, TasteException {
        this.path = s;
        this.totalReviews = 0;
        this.totalProducts = 0;
        this.totalUsers = 0;
        converter(s);
        dataTreatment("src/data/movies.txt");
    }

    public long getTotalReviews(){
        System.out.println("Total Reviews = " + totalReviews);
        return totalReviews;
    }
    public long getTotalProducts(){
        System.out.println("Total Products = " + totalProducts);
        return totalProducts;
    }
    public long getTotalUsers(){
        System.out.println("Total users = " + totalUsers);
        return totalUsers;
    }


    public void converter(String s){
        try (
                FileInputStream fis = new FileInputStream(s);
                GZIPInputStream gzis = new GZIPInputStream(fis);
                FileOutputStream fos = new FileOutputStream("src/data/movies.txt")) {

            byte[] buffer = new byte[1024];
            int length;

            while ((length = gzis.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void dataTreatment(String s) throws IOException {

        BufferedReader br = new BufferedReader(new FileReader("src/data/movies.txt"));
        BufferedWriter bw = new BufferedWriter(new FileWriter("src/data/movies.csv"));

        String line;

        Integer elementsInArray = 0;
        Integer userId = 1;
        Integer productId = 1;
        Integer totalRev = 0;

        String productTag = "product/productId";
        String userTag = "review/userId";
        String scoreTag = "review/score";

        String[] sampledData = new String[3];

        while((line = br.readLine()) != null) {

            String[] values = line.split("\\:", -1);

            if( values[0].equals(productTag)){
                String formating = values[1].replaceAll("\\s","");
                if(!productMap.containsKey(formating)){
                    productMap.put(formating,productId);
                    sampledData[1] = productId.toString();
                    productId++;
                }else{
                    String cmon = String.valueOf(productMap.get(formating));
                    sampledData[1] = cmon;
                }

                elementsInArray ++;
            }

            if(values[0].equals(userTag)){
                String key = values[1].replaceAll("\\s","");
                if(!usersMap.containsKey(key)){
                    usersMap.put(key,userId);
                    sampledData[0] = userId.toString();
                    userId++;
                }else{
                    sampledData[0] = String.valueOf(usersMap.get(key));
                }

                elementsInArray ++;
            }
            if(values[0].equals(scoreTag)){
                sampledData[2] = values[1].replaceAll("\\s","");
                elementsInArray ++;
            }
            if(elementsInArray == 3){

                bw.write(sampledData[0] + "," + sampledData[1] + "," + sampledData[2] + "\n");
                totalRev++;
                elementsInArray = 0;
            }
        }

        Integer totUsers = usersMap.size();
        Integer totProd =   productMap.size();
        totalReviews = totalRev;
        totalUsers = totUsers;
        totalProducts = totProd;

        br.close();
        bw.close();
    }

    private String getProductID(Integer val){
        for(Map.Entry entry: productMap.entrySet()){
            if(val.equals(entry.getValue())){
                return   entry.getKey().toString();
            }
        }

        return null;
    }

    public List<String> getRecommendationsForUser(String s) throws TasteException, IOException {

        List<String> results = new ArrayList<String>();
        int userId = usersMap.get("A141HP4LYPWMSR");


        DataModel model = new FileDataModel(new File("src/data/movies.csv"));
        Integer iterations = 0;
        UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
        UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);
        UserBasedRecommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);
        List<RecommendedItem> recommendations = recommender.recommend(userId, 3);
        for (RecommendedItem recommendation : recommendations){
            results.add(getProductID(((int) recommendation.getItemID())));
        }
        System.out.println("iterantions = " + iterations);
        return  results;
    }

}

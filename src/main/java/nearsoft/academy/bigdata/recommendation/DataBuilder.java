package nearsoft.academy.bigdata.recommendation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;

public class DataBuilder {
    // Attributes
    private int totalReviews;
    private int totalProducts;
    private int totalUsers;

    private HashMap<String, Integer> users;
    private HashMap<String, Integer> products;

    // Constructor
    public DataBuilder(String srcFile) throws IOException {;
        totalProducts = 0;
        totalUsers = 0;
        totalReviews = 0;

        // the hashmaps have a 1:1 relationships between keys and values
        users = new HashMap<String, Integer>();
        products = new HashMap<String, Integer>();

        // We begin to build all the data we need
        String productTemp = "";
        String userTemp = "";

        // Initialize all the things needed to read the gz file
        InputStream inputStream = new GZIPInputStream(new FileInputStream(srcFile));
        Reader reader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(reader);

        // Initialize all the things needed to write the CSV
        File moviesCSV = new File("movies.csv");
        FileWriter fileWriter = new FileWriter(moviesCSV);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

        // read the gz file to build the csv file for the data model used in the recommender
        String current;
        while ((current = bufferedReader.readLine()) != null) {
            if (current.startsWith("review/score")) {
                totalReviews++;
                String reviewScore = current.split(" ")[1];
                bufferedWriter.write(userTemp + "," + productTemp + "," + reviewScore + "\n");
            } else {
                if (current.startsWith("product/productId")) {
                    String productId = current.split(" ")[1];
                    if (products.containsKey(productId)) {
                        productTemp = Integer.toString(products.get(productId));
                    } else {
                        // totalProducts as a second ID 
                        products.put(productId, totalProducts);
                        productTemp = Integer.toString(totalProducts);
                        totalProducts++;
                    }
                } else if (current.startsWith("review/userId")) {
                    String userId = current.split(" ")[1];
                    if (users.containsKey(userId)) {
                        userTemp = Integer.toString(users.get(userId));
                    } else {
                        // totalUsers as a second ID 
                        users.put(userId, totalUsers);
                        userTemp = Integer.toString(totalUsers);
                        totalUsers++;
                    }
                }
            }
        }
        bufferedReader.close();
        bufferedWriter.close();
    }

    // Getters
    public int getTotalProducts(){
        return totalProducts;
    }
    public int getTotalUsers(){
        return totalUsers;
    }
    public int getTotalReviews(){
        return totalReviews;
    }
    public HashMap<String, Integer> getProducts(){
        return products;
    }
    public HashMap<String, Integer> getUsers(){
        return users;
    }
}

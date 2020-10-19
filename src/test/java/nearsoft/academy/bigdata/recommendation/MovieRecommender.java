//package nearsoft.academy.bigdata.recommendation;


import com.opencsv.CSVWriter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class MovieRecommender {

    String currentProduct = null;
    String currentUser = null;
    String currentValue = null;
    int totalMovies = 0;
    HashMap<String, Integer> users;
    HashMap<String, Integer> products;
    CSVWriter writer;
    //Create a new csv file to push data with specified path

    public static void main(String[] args) throws IOException {
        MovieRecommender recommenderTest = new MovieRecommender("data/moviestest1.txt");
    }

    MovieRecommender(String pathFile) throws IOException {
        users = new HashMap<String, Integer>();
        products = new HashMap<String, Integer>();
        readFile(pathFile);
//        File file = new File("data/test1.csv"); // create FileWriter object with file as parameter
//        FileOutputStream fos = new FileOutputStream(file);
//        OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
//        writer = new CSVWriter(osw);
//        FileWriter outputFile = new FileWriter(file); // create CSVWriter object filewriter object as parameter
//        writer = new CSVWriter(outputFile);
    }

    private void readFile(String pathFile) throws IOException {
        String productID = "product/productId: ";
        String userID = "review/userId: ";
        String valueID = "review/score: ";
        String fileName = "data/moviestest1.txt";
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(fileName), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {  //Checking if the line contains a specific string to match
                if (line.contains(productID)) {
                    totalMovies++;
                    currentProduct = line.replace(productID, ""); //B003AI2VGA
                    if (!products.containsKey(currentProduct)) {
                        products.put(currentProduct, products.size());
                    }
                } else if (line.contains(userID)) {
                    currentUser = line.replace(userID, "");
                    if (!users.containsKey(currentUser)) {
                        users.put(currentUser, users.size());
                        System.out.println(users.size());
                    }
                } else if (line.contains(valueID)) {
                    currentValue = line.replace(valueID, ""); // 5.0
                  System.out.println(currentProduct + currentUser + currentValue);
                    ArrayList<String> cars = new ArrayList<String>();
                    cars.add("Volvo");
                    cars.add("BMW");
                    cars.add("Ford");
                    cars.add("Mazda");
                    writeUnicodeJava8(pathFile, cars);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeUnicodeJava8(String fileName, List<String> lines) {
        Path path = Paths.get(fileName);
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            for (String line : lines) {
                writer.append(line);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    private void writeData() throws FileNotFoundException {
//        File file = new File("data/test1.csv"); // create FileWriter object with file as parameter
//        FileOutputStream fos = new FileOutputStream(file);
//        OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
//        writer = new CSVWriter(osw);
//        writer.writeNext(new String[]{"1"});
//        currentValue = null;
//        currentProduct = null;
//        currentUser = null;
//        }

    public int getTotalReviews() {
        return totalMovies;
    }

    public int getTotalProducts() {
        return products.size();
    }

    public int getTotalUsers() {
        return users.size();
    }
//
//    public List<String> getRecommendationsForUser(String a141HP4LYPWMSR) throws IOException, TasteException {
//        DataModel model = new FileDataModel(new File("data/test1.csv"));
//
//        UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
//
//        UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);
//
//        UserBasedRecommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);
//
//        List<RecommendedItem> recommendations = recommender.recommend(2, 3);
//        for (RecommendedItem recommendation : recommendations) {
//            System.out.println(recommendation);
//        }
//    }
}



// 1. Leer el archivo txt
// 2. Encontrar la informacion que necesito
// 3. Poner esta informacion [0] {userID, productID, value }
// 4. Escribir archivo csv con la inform
// 5.



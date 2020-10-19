package nearsoft.academy.bigdata.recommendation;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
//Librerías de mahoot necesarias para el recommender

import java.io.*;
import java.util.*;
import java.util.zip.*;
//Librerías para el manejo de archivos zip en java

public class MovieRecommender {

    HashMap<String, Integer> uniqueUser = new HashMap<String, Integer>();
    HashMap<String, Integer> uniqueProduct = new HashMap<String, Integer>();
    int totalReviews = 0;
    //Aquí declaramos las variables globales que usaremos y devolveremos en métodos

    public MovieRecommender(String filename) throws IOException{
        //Constructor de la clase

        InputStream inputStream = new GZIPInputStream(new FileInputStream(filename));
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
        String strCurrentLine;
        //Construimos un objeto de BufferedReader para el archivo gzip dado

        String prodPattern = "product/productId: ";
        String userPattern = "review/userId: ";
        String scorePattern = "review/score: ";
        //Variables con el patrón que buscaremos en las líneas

        String product = "";
        int productCont = 0;
        String user = "";
        int userCont = 0;
        String score = "";
        //Variables auxiliares donde almacenamos los valores de cada línea

        File moviesData = new File("data/moviesdb.csv");
        FileWriter writer = new FileWriter(moviesData);
        BufferedWriter lineWritter = new BufferedWriter(writer);
        //Construimos objeto para la creación y escritura de archivo csv

            while((strCurrentLine = br.readLine()) != null){
            //Cíclo para recorrer línea a línea del archivo

                if(strCurrentLine.contains(prodPattern)) {
                    product = strCurrentLine.substring(prodPattern.length());
                    if(!this.uniqueProduct.containsKey(product)){
                        this.uniqueProduct.put(product, productCont);
                        productCont++;
                    }

                }
                if(strCurrentLine.contains(userPattern)) {
                    user = strCurrentLine.substring(userPattern.length());
                    if(!this.uniqueUser.containsKey(user)){
                        this.uniqueUser.put(user, userCont);
                        userCont++;
                    }
                }
                if(strCurrentLine.contains(scorePattern)) {
                    this.totalReviews++;
                    score = strCurrentLine.substring(scorePattern.length());
                    lineWritter.write(this.uniqueUser.get(user) + "," + this.uniqueProduct.get(product) + "," + score + "\n");
                }
                //Condicionales para encontrar patrones en las líneas,
                //si se encuentra cierto patrón se procesa la línea dependiendo
                //el patrón que se encuentr
        }
            lineWritter.close();
            writer.close();
            br.close();
            inputStream.close();


    }

    public String getKey(int val)
    {
        for(String key : this.uniqueProduct.keySet()){
            if(this.uniqueProduct.get(key) == val){
                return key;
            }
        }
        return null;
    }
    //Método para devolver el key del hashmap de productos dado un valor

    public List<String> getRecommendationsForUser(String user) throws IOException, TasteException {

        int numUser = this.uniqueUser.get(user);
        List<String> recs = new ArrayList<String>();

        DataModel model = new FileDataModel(new File("data/moviesdb.csv"));
        UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
        UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);
        UserBasedRecommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);
        List<RecommendedItem> recommendations = recommender.recommend(numUser, 3);
        for (RecommendedItem recommendation : recommendations){
            recs.add(getKey((int)recommendation.getItemID()));
        }
        return recs;
    }
    //Implementación del recommender de 5 min dada la base de datos que constuimos

    public int getTotalReviews(){
        return this.totalReviews;
    }
    //Método para devolver el total de reviews

    public int getTotalProducts(){
        return this.uniqueProduct.size();
    }
    //Método para devolver el total de productos únicos

    public int getTotalUsers(){
        return this.uniqueUser.size();
    }
    //Método para devolver el total de usuarios únicos

}

package nearsoft.academy.bigdata.recommendation;

import java.io.*;
import java.util.*;

public class CleanData {
    public static void main(String[] args) throws Exception {
        String thisLine = null;
        FileWriter fw = new FileWriter("./clean_data.csv");
        BufferedWriter bw = new BufferedWriter(fw);
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File("/Users/nearsoft/Downloads/movies.txt")));
            String[] subarray = new String[3];
            long i = 0;
            long newId = 0;
            while ((thisLine = br.readLine()) != null) {
                if (thisLine.startsWith("product/productId:")) {

                    subarray[0] = thisLine.split(" ")[1];

                }
                else if (thisLine.startsWith("review/userId")) {
                    subarray[1] = thisLine.split(" ")[1];

                }
                else if (thisLine.startsWith("review/score")) {
                    subarray[2] = thisLine.split(" ")[1];

                    bw.write(subarray[1] +","+ subarray [0] +","+ subarray [2] + "\n");
                }

            }
            bw.flush();
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }



    }
}

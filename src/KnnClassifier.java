
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;


public class KnnClassifier {

	/* Create a Flower class for accessing and manipulating the 
	 * csv to be read in */
	
    class Flower {
        String rawString; // Raw CSV input
        float pw; // Petal width
        float pl; // Petal length
        float sw; // Sepal width
        float sl; // Sepal length
        String identity; // Identity (species) of flower
        ArrayList<TrainingDistance> trainingDistances; /* an ArrayList
         to be filled with distances to the training set */

        
        /* Write setter for petal width */
        public void setPetalWidth(float pw) {
        	this.pw = pw;
        }
        
        /* Write getter for petal width */
        public float getPetalWidth() {
            return pw;
        }
        
        /* Write setter for petal length */
        public void setPetalLength(float pl) {
        	this.pl = pl;
        }
        
        /* Write getter for petal length */
        public float getPetalLength() {
            return pl;
        }
        
        /* Write setter for sepal width */
        public void setSepallWidth(float sw) {
        	this.sw = sw;
        }
        
        /* Write getter for sepal width */
        public float getSepalWidth() {
            return sw;
        }
        
        /* Write setter for sepal length */
        public void setSepalLength(float pl) {
        	this.sl = sl;
        }
        
        /* Write getter for sepal length */
        public float getSepalLength() {
            return sl;
        }
        
        
        /* Write setter for identity */
        public void setIdentity(String identity) {
            this.identity = identity;
        }

        /* Write getter for identity */
        public String getIdentity() {
            return identity;
        }

        /* Debugging help */
        public String toString() {
            return  "" + pw + " " + pl + " " + sw + " " + sl + " " + identity + " " + trainingDistances;
        }

        /* Write setter for Training Distances */
        public void setTrainingDistances(ArrayList<TrainingDistance> trainingDistances) {
            this.trainingDistances = trainingDistances;
        }

        /* Write getter for Training Distances */
        public ArrayList<TrainingDistance> getTrainingDistances() {
            return trainingDistances;
        }
    };
    
    
	/* Create a class for recording training distances and identities */
    class TrainingDistance {

        float distance; // calculated distance value
        String identity; // identity (species) of flower 

        /* Impose constructor class structure onto TrainingDistance Class */
        public TrainingDistance(String id, float dist) {
            identity = id;
            distance = dist;
        }

        /* Write setter for distance */
        public void setDistance(float distance) {
            this.distance = distance;
        }

        /* Write getter for distance */
        public float getDistance() {
            return distance;
        }
        
        /* Write setter for identity */
        public void setIdentity(String identity){
        	this.identity = identity;
        }
        
        /* Write getter for identity */
        public String getIdentity(){
        	return identity;
        }

        public String toString() {
            return  "" + distance + " " + identity;
        }
    };


    private void execute(String fileLoc) throws FileNotFoundException {
        long startTime = System.currentTimeMillis();
        System.out.println("hi 1 ");

        ArrayList<Flower> data = readCSV(fileLoc);
        ArrayList<TrainingDistance> td = null;

        System.out.println(data.size());
        System.out.println("hi 2 " + data.toString());

        int size = data.size();
        for (int i = 0; i < size; i++) { // iterate over test data
            for (int j = 0; j < size; j++) { // calculate distances from training data to test data
                if (j == 0) {
                    td = new ArrayList<TrainingDistance>(); //initialize TrainingDistance object
                }
                if (i == j) {
                    data.get(i).setTrainingDistances(td); // set TrainingDistances array in data object of class Flower
                } else {
                    float distance = eucDist(data.get(i), data.get(j)); // calculate euc distance between row i and row j of data
                    td.add(new TrainingDistance(data.get(j).getIdentity(), distance)); // add identity of jth flower and distance from above to training distances object of class TrainingDistance
                }
            }
            /* Write method to sort td by distance and keep only the smallest K records. Then,
             * of those smallest K records, pick the most frequently occuring identity. */
            /* help with sorting */
            /* http://stackoverflow.com/questions/1814095/sorting-an-arraylist-of-contacts-based-on-name */
        }

        System.out.println("TrainingData array " + td.size());
        System.out.println(td);
        System.out.println("hi 3 " + data.toString());


        System.out.println("hi 10 " + (System.currentTimeMillis() - startTime));
    }


    /* This method reads the file supplied from the command line and stores it
     * as an Array list of type Flower */
    public  ArrayList<Flower> readCSV(String filePath) throws FileNotFoundException {

        Scanner input = new Scanner(new File(filePath)); // read the file

        ArrayList<Flower> returnList = new ArrayList<Flower>(); // initialize an ArrayList of type Flower

        while (input.hasNextLine()) { 
            Flower flower = new Flower(); // create a new flower for each line
            flower.rawString = input.nextLine(); // read the whole line from the csv
            StringTokenizer st = new StringTokenizer(flower.rawString, ","); // split the data on a comma
            while (st.hasMoreElements()) {
                flower.pw = Float.parseFloat(st.nextToken()); // get the petal width
                flower.pl = Float.parseFloat(st.nextToken()); // get the petal length
                flower.sw = Float.parseFloat(st.nextToken()); // get the sepal width
                flower.sl = Float.parseFloat(st.nextToken()); // get the sepal length
                flower.identity = st.nextToken(); // get the identity (species) 
                System.out.println(" " + flower.toString());
            }

            returnList.add(flower); // append the flower to the ArrayList returnList
        }

        return returnList;
    }

    /* This method finds the distance between two rows of data. 
     * To avoid reliance on the math library, the square root of
     * the Euclidean distance has been forgone. */
    @SuppressWarnings("unused")
    private float eucDist(Flower a, Flower b) {
        return ((a.getPetalLength()-b.getPetalLength()) * (a.getPetalLength()-b.getPetalLength()) +
                ((a.getPetalWidth()-b.getPetalWidth())) * ((a.getPetalWidth()-b.getPetalWidth())) +
                ((a.getSepalLength()-b.getSepalLength())) * ((a.getSepalLength()-b.getSepalLength())) +
                ((a.getSepalWidth()-b.getSepalWidth())) * ((a.getSepalWidth()-b.getSepalWidth())));
    }

    public static void main(String[] args) throws FileNotFoundException {
        KnnClassifier knnClassifier = new KnnClassifier();
        knnClassifier.execute(args[0]);
    }
}
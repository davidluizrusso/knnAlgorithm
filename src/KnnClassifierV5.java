// import java packages
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.StringTokenizer;

public class KnnClassifierV5 {

    /* This program is intended to be run from the command line - this is the "main" routine */
    public static void main(String[] args) throws Exception {

        /* Initialize KnnClassifier object */
        KnnClassifierV5 knnClassifier = new KnnClassifierV5();
        File file = new File(args[0]); // read file from command line
        int k = Integer.parseInt(args[1]); // read k from command line
        
        /* Build and pre-process data; make predictions */
        ArrayList<Flower> data = knnClassifier.preProcessData(new FileInputStream(file), k);
        ArrayList<Prediction> pd = knnClassifier.makePredictions(data);
        
        /* get accuracy and confusion matrix output */
        float acc = knnClassifier.accuracy(pd);
        HashMap<String, Integer> confMat = knnClassifier.confusionMatrix(pd);
        
        /* Print to the console */
        System.out.println("K = " + k + "\n");
        System.out.println(pd.toString());
        System.out.println("\n Accuracy = " + acc);
        System.out.println("\n Confusion matrix \n" );
        for (String name: confMat.keySet()){
            String key =name.toString();
            String value = confMat.get(name).toString();  
            System.out.println(key + " " + value + "\n");  
            } 
        System.out.println("\n------------- SUCCESS -------------");
        
        /* Print results to file */
        PrintStream out = new PrintStream(new FileOutputStream("output.txt"), true);
        System.setOut(out);
        System.out.println("K = " + k + "\n");
        System.out.println(pd.toString());
        System.out.println("\n Accuracy = " + acc);
        System.out.println("\n Confusion matrix \n" );
        for (String name: confMat.keySet()){
            String key =name.toString();
            String value = confMat.get(name).toString();  
            System.out.println(key + " " + value + "\n");  
            } 
        System.out.println("\n------------- SUCCESS -------------");
        

        

        
        System.exit(0);
    } // end main method
    
    /* This method reads the input data in the form of a list of "petal width, petal length,
     * sepal width, sepal length" records and stores the input data in an Flower Array list */
    public ArrayList<Flower> readCSV(InputStream in) {

        // read the data
    	@SuppressWarnings("resource")
		Scanner input = new Scanner(in);
        ArrayList<Flower> returnList = new ArrayList<Flower>();

        // iterate through data and populate flower object
        while (input.hasNextLine()) {
            Flower flower = new Flower();
            flower.rawString = input.nextLine();
            StringTokenizer st = new StringTokenizer(flower.rawString, ",");
            while (st.hasMoreElements()) {
                flower.pw = Float.parseFloat(st.nextToken());
                flower.pl = Float.parseFloat(st.nextToken());
                flower.sw = Float.parseFloat(st.nextToken());
                flower.sl = Float.parseFloat(st.nextToken());
                flower.identity = st.nextToken();
            }
            returnList.add(flower); // add record to flower object
        }
        return returnList;
    } // end readCSV method
    
    /* This method finds the distance between two rows of data */
    /* To avoid using the math library, the square root was not used */
    private float eucDist(Flower a, Flower b) {
        return ((a.pl - b.pl) * (a.pl - b.pl) +
                (a.pw - b.pw) * (a.pw - b.pw) +
                (a.sl - b.sl) * (a.sl - b.sl) +
                (a.sw - b.sw) * (a.sw - b.sw));
    } // end eucDist method
    
    /* This method reads the input by invoking the readCSV method and computes the Training Distance
     * between the Flowers in the Flower ArrayList */
    private ArrayList<Flower> preProcessData(InputStream in, int k) {

    	// read data with readCSV and create a TrainingDistance object
        ArrayList<Flower> data = readCSV(in);
        ArrayList<TrainingDistance> td = null;

        /* Compare each row to the other 149 rows and calculate the distance 
         * between the two records */
        int size = data.size();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (j == 0) {
                    td = new ArrayList<TrainingDistance>(); // initialize empty ArrayList of TrainingDistance
                }
                if (i == j) {
                    data.get(i).setTrainingDistances(td); // set the training distance matrix
                } else {
                    float distance = eucDist(data.get(i), data.get(j)); // calculate the distance
                    td.add(new TrainingDistance(data.get(j).getIdentity(), distance)); // add distance to td object
                }
            }              
        }
        
        // Sort the td object by distance and filter out all but the k nearest neighbors
        for (int i = 0; i < size; i++) {
        	
            td = data.get(i).getTrainingDistances();
            Collections.sort(td);            
            int tdSize = td.size();
            
            for(int j = tdSize-1; j >= k; j--){
            	td.remove((td.get(j))); //removal of all but k nearest neighbors
            }     
        }                
        return data;
    } // end preProcessData method
    
    /* This method determines who the majority identity is of the k nearest neighbors */
    private Prediction majorityNeighbor(String trueIdentity, ArrayList<TrainingDistance> tda){
    	
    	// initialize a HashMap
    	HashMap<String, Integer> hm = new HashMap<String, Integer>();
    	
    	// populate the HashMap of identities and counts
    	for(int i = 0; i < tda.size(); i++){
    		TrainingDistance td = tda.get(i);
    		String id = td.getOtherIdentity();
    		if(!hm.containsKey(id)){
    			hm.put(id, new Integer(1));
    		} else {
    			int currentCount = hm.get(id).intValue();
    			hm.put(id, new Integer(currentCount + 1));
    		}
    	}
    	
    	// find take majority vote using the iterator class
    	Set candidates = hm.keySet();
    	Iterator<String> itr = candidates.iterator();
    	
    	// determine winning flower
    	String winnerName = "?"; //initialize winnerName
    	int winnerCount = 0; // initialie winnerCount
    	boolean tie = false; // initialie tie boolean
    	while(itr.hasNext()){
    		String flowerName = itr.next();
    		int votes = hm.get(flowerName).intValue();
    		
    		if(votes > winnerCount){ //update winnerCount and flowerName if new winner is found
    			winnerName = flowerName;
    			winnerCount = votes;
    			tie = false; //keep tie as false
    		} else if(votes == winnerCount) {
    			tie = true;	 //set tie to true
    		}
    	}
    	
    	// break ties using a random number geneator
    	if(tie){
    		Random generator = new Random();
     		 double number = generator.nextDouble(); // generate random number between 0 and 1
     		 
     		 if(number <= 0.33){
     			 winnerName = "Iris-setosa";
     		 } else if (number > 0.33 & number < 0.67){
     			 winnerName = "Iris-versicolor";
     		 } else {
     			 winnerName = "Iris-virginica";
     		 }
    	}
    	// return an object of class Prediction (actual identity, predicted identity, match indicator)
    	return new Prediction(trueIdentity, winnerName, (0 == trueIdentity.compareTo(winnerName)));   	
    } // end majorityNeighbor method
    
    // method to set the predictions; returns an arrayList of predictions 
    private ArrayList<Prediction> makePredictions(ArrayList<Flower> data) {
        ArrayList<Prediction> predictions = new ArrayList<Prediction>();

        int size = data.size();
        for (int i = 0; i < size; i++) {
            Flower fw = data.get(i);
            predictions.add(majorityNeighbor(fw.getIdentity(), fw.getTrainingDistances()));
        }
        return predictions;
    } // end makePredictions method
    
    
    // method to determine knn accuracy
    private float accuracy(ArrayList<Prediction> pred){
		
    	int totalSize = pred.size();  
    	int match = 0;
    	
    	// iterate over match field of pred
    	for(int i = 0; i < pred.size(); i++){
    		if(pred.get(i).isMatch()){
    			match = match + 1;
    		}
    	}
    	
    	return (float)match/totalSize;   	
    } // end accuracy method
    
    
    // method to return results in a confusion matrix like form
    private HashMap<String, Integer> confusionMatrix(ArrayList<Prediction> pred){
		
    		// initialize an empty HashMap
    		HashMap<String, Integer> hm = new HashMap<String, Integer>();
    	
    		// populate the HashMap
    		// the key of the HashMap is the unique vlaues of the concatenations of 
    		// "true identity" and "predicted identity"
    		for(int i = 0; i < pred.size(); i++){
    			
    			Prediction pd = pred.get(i);
    			String id = pd.truePredConcat(pd.getTrueIdentity(), pd.getPredictedIdentity());
    			if(!hm.containsKey(id)){
    				hm.put(id, new Integer(1));
    			} else {
    				int currentCount = hm.get(id).intValue();
    				hm.put(id, new Integer(currentCount + 1));
    			}
    		}
    	
    	return hm;
    	
    }
    




    /* This class stores the TrainingDistance between on instance of a Flower and another Flower */
    class TrainingDistance implements Comparable<TrainingDistance>  {

        float distance; // distance between the given (baseline) flower and the other (comparison) flower
        String otherIdentity; // identity of the other flower

        // constructor class 
        public TrainingDistance(String id, float dist) {
            otherIdentity = id;
            distance = dist;
        }

        // getters and setters for TrainingDistance object 
        public float getDistance() {
            return distance;
        }

        public void setDistance(float distance) {
            this.distance = distance;
        }
        
        public String getOtherIdentity() {
            return otherIdentity;
        }

        public void setOtherIdentity(String otherIdentity) {
            this.otherIdentity = otherIdentity;
        }

        // to string method for TrainingDistance
        public String toString() {
            return "" + distance + " " + otherIdentity;
        }
        

        // comparison method for TrainingDistance
        // This will help sort the TrainingDistance object by distance later on
		public int compareTo(TrainingDistance td) {
			if (this.distance > td.distance) return 1;
            else if (this.distance < td.distance) return -1;
            else return 0;
		}
    } // end TrainingDistance class

    /* This class is used to capture Flower parameters provided in the input file and also used to capture
     * an array of Training Distances to the other Flowers provided in the file */
    class Flower {
        String rawString; // raw data from the .txt file
        float pw; // Petal width
        float pl; // Petal length
        float sw; // Sepal Width
        float sl; // Sepal Length
        String identity; // identity of flower
        ArrayList<TrainingDistance> trainingDistances; //an ArrayList of distances from the reference flower to the other flowers

        // getters and setters for Flower data
        public String getIdentity() {
            return identity;
        }

        public void setIdentity(String identity) {
            this.identity = identity;
        }
        
        public ArrayList<TrainingDistance> getTrainingDistances() {
            return trainingDistances;
        }

        public void setTrainingDistances(ArrayList<TrainingDistance> trainingDistances) {
            this.trainingDistances = trainingDistances;
        }

        // toString methof for flower data
        public String toString() {
            return "" + pw + " " + pl + " " + sw + " " + sl + " " + identity + " " + trainingDistances;
        }


    } // end Flower class
    
    // create a class for holding predictions from the knn algorithm
    class Prediction {
    	
    	String trueIdentity;
    	String predictedIdentity;
    	boolean match;
    	
    	// getters and setters for prediction data
    	public boolean isMatch() {
			return match;
		}

		public void setMatch(boolean match) {
			this.match = match;
		}
		
		/* getters and setters for true identity */
    	public String getTrueIdentity(){
    		return trueIdentity;
    	}
    	
    	public void setTrueIdentity(String ti){
    		this.trueIdentity = ti;
    	}
    	
    	/* getters and setters for predicted identity */
    	public String getPredictedIdentity(){
    		return predictedIdentity;
    	}
    	
    	public void setPredictedIdentity(String pri){
    		this.predictedIdentity = pri;
    	}

    	// constructor class
		public Prediction(String ti, String pri, boolean m) {
			trueIdentity = ti;
			predictedIdentity = pri;
			match = m;
		}
    	
		
		// toString method
        public String toString() {
            return "\n Match: " + match + " - True Identity: " + trueIdentity + " - Predicted Identity: " + predictedIdentity;
        }

        // concatenate the true identity and the predicted identity--used in the confusion matrix 
    	public String truePredConcat(String ti, String pri){
    		String concat = "True Identity: " + ti + " - Predicted Identity: " + pri + " - ";
    		return concat;
    	}
    } // end Prediction Class
    

    
} // end KnnClassifierV5 class

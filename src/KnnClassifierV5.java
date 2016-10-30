import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.StringTokenizer;

public class KnnClassifierV5 {

    /* This program is intended to be run from the command line - this is the "main" routine */
    public static void main(String[] args) throws Exception {

        // preProcessorUnitTest();

        long startTime = System.currentTimeMillis();

        KnnClassifierV5 knnClassifier = new KnnClassifierV5();
        File file = new File(args[0]);
        int k = Integer.parseInt(args[1]);
        ArrayList<Flower> data = knnClassifier.preProcessData(new FileInputStream(file), k);
        ArrayList<Prediction> pd = knnClassifier.makePredictions(data);
        float acc = knnClassifier.accuracy(pd);
        
        // System.out.println("\nRun time = " + (System.currentTimeMillis() - startTime) + " milliseconds");
        // System.out.println("Prediction data {" + pd.toString());
        System.out.println("Accuracy = " + acc);
        System.out.println("\n------------- SUCCESS -------------");
        System.exit(0);
    } // end main method
    
    /* This method reads the input data in the form of a list of "5.1,3.5,1.4,0.2,Iris-setosa" records
     * and stores the input data in an Flower Array list */
    public ArrayList<Flower> readCSV(InputStream in) {

        Scanner input = new Scanner(in);
        ArrayList<Flower> returnList = new ArrayList<Flower>();

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
            returnList.add(flower);
        }
        return returnList;
    } // end readCSV method
    
    /* This method finds the distance between two rows of data */
    private float eucDist(Flower a, Flower b) {
        return ((a.pl - b.pl) * (a.pl - b.pl) +
                (a.pw - b.pw) * (a.pw - b.pw) +
                (a.sl - b.sl) * (a.sl - b.sl) +
                (a.sw - b.sw) * (a.sw - b.sw));
    } // end eucDist method
    
    /* This method reads the input by invoking the readCSV method and computes the Training Distance
     * between the Flowers in the Flower ArrayList */
    private ArrayList<Flower> preProcessData(InputStream in, int k) {

        ArrayList<Flower> data = readCSV(in);
        ArrayList<TrainingDistance> td = null;

        int size = data.size();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (j == 0) {
                    td = new ArrayList<TrainingDistance>();
                }
                if (i == j) {
                    data.get(i).setTrainingDistances(td);
                } else {
                    float distance = eucDist(data.get(i), data.get(j));
                    td.add(new TrainingDistance(data.get(j).getIdentity(), distance));
                }
            }              
        }
        

        for (int i = 0; i < size; i++) {
        	
            td = data.get(i).getTrainingDistances();
            Collections.sort(td);            
            int tdSize = td.size();
            
            for(int j = tdSize-1; j >= k; j--){
            	td.remove((td.get(j)));
            }     
        }                
        return data;
    } // end preProcessData method
    
    private Prediction majorityNeighbor(String trueIdentity, ArrayList<TrainingDistance> tda){
    	
    	HashMap<String, Integer> hm = new HashMap<String, Integer>();
    	
    	// populate the HashMap
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
    	
    	// find take majority vote
    	Set candidates = hm.keySet();
    	Iterator<String> itr = candidates.iterator();
    	
    	// determine winning flower
    	String winnerName = "?";
    	int winnerCount = 0;
    	boolean tie = false;
    	while(itr.hasNext()){
    		String flowerName = itr.next();
    		int votes = hm.get(flowerName).intValue();
    		
    		if(votes > winnerCount){
    			winnerName = flowerName;
    			winnerCount = votes;
    			tie = false;
    		} else if(votes == winnerCount) {
    			tie = true;	
    		}
    	}
    	
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
    	
    	return new Prediction(trueIdentity, winnerName, (0 == trueIdentity.compareTo(winnerName)));   	
    } // end majorityNeighbor method
    
    private ArrayList<Prediction> makePredictions(ArrayList<Flower> data) {
        ArrayList<Prediction> predictions = new ArrayList<Prediction>();

        int size = data.size();
        for (int i = 0; i < size; i++) {
            Flower fw = data.get(i);
            predictions.add(majorityNeighbor(fw.getIdentity(), fw.getTrainingDistances()));
        }

        return predictions;
    } // end makePredictions method
    
    private float accuracy(ArrayList<Prediction> pred){
		
    	int totalSize = pred.size();  
    	int match = 0;
    	
    	for(int i = 0; i < pred.size(); i++){
    		if(pred.get(i).isMatch()){
    			match = match + 1;
    		}
    	}
    	
    	return (float)match/totalSize;
    	
    }
    




    /* This class stored the a TrainingDistance between on instance of a Flower and another Flower (otherIdentiry) */
    class TrainingDistance implements Comparable<TrainingDistance>  {

        float distance; // ...
        String otherIdentity;

        public TrainingDistance(String id, float dist) {
            otherIdentity = id;
            distance = dist;
        }

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

        public String toString() {
            return "" + distance + " " + otherIdentity;
        }
        


		public int compareTo(TrainingDistance td) {
			if (this.distance > td.distance) return 1;
            else if (this.distance < td.distance) return -1;
            else return 0;
		}
    } // end TrainingDistance class

    /* This class is used to capture Flower parameters provided in the input file and also used to capture
     * an array of Training Distances to the other Flowers provided in the file */
    class Flower {
        String rawString;
        float pw; // Petal width
        float pl; // ...
        float sw; // ...
        float sl; // ...
        String identity;
        ArrayList<TrainingDistance> trainingDistances;

        public String getIdentity() {
            return identity;
        }

        public void setIdentity(String identity) {
            this.identity = identity;
        }

        public String toString() {
            return "" + pw + " " + pl + " " + sw + " " + sl + " " + identity + " " + trainingDistances;
        }

        public ArrayList<TrainingDistance> getTrainingDistances() {
            return trainingDistances;
        }

        public void setTrainingDistances(ArrayList<TrainingDistance> trainingDistances) {
            this.trainingDistances = trainingDistances;
        }
    } // end Flower class
    
    class Prediction {
    	
    	String trueIdentity;
    	String predictedIdentity;
    	boolean match;
    	
    	public boolean isMatch() {
			return match;
		}

		public void setMatch(boolean match) {
			this.match = match;
		}

		public Prediction(String ti, String pri, boolean m) {
			trueIdentity = ti;
			predictedIdentity = pri;
			match = m;
		}
    	
        public String toString() {
            return "\n" + match + " " + trueIdentity + " " + predictedIdentity;
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
    } // end Prediction Class
    

    
} // end KnnClassifierV5 class

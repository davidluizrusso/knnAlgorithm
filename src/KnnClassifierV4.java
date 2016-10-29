import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;


public class KnnClassifierV4 {

    /* UNIT TEST class for the process of reading input data and storing in a ArrayList of Flowers */
    public static ArrayList<Flower> preProcessorUnitTest() throws Exception {
        KnnClassifierV4 knnClassifier = new KnnClassifierV4();

        String testString = "5.1,3.5,1.4,0.2,Iris-setosa\n4.9,3.0,1.4,0.2,Iris-setosa\n";
        InputStream is = new ByteArrayInputStream(testString.getBytes());
        ArrayList<Flower> data = knnClassifier.preProcessData(is, 3);
        String expectedResult = "[5.1 3.5 1.4 0.2 Iris-setosa [0.28999993 Iris-setosa], 4.9 3.0 1.4 0.2 Iris-setosa [0.28999993 Iris-setosa]]";
        if (expectedResult.compareTo(data.toString()) != 0) {
            System.out.println("\n**************** ERROR ****************");
            System.out.println("================ " + expectedResult.compareTo(data.toString()));
            System.out.println("Expected results  {" + expectedResult + "}");
            System.out.println("Generated results {" + data.toString() + "}");
            System.exit(1);
        }

        return data;
    }

    /* This program is intended to be run from the command line - this is the "main" routine */
    public static void main(String[] args) throws Exception {

        // preProcessorUnitTest();

        long startTime = System.currentTimeMillis();

        KnnClassifierV4 knnClassifier = new KnnClassifierV4();
        File file = new File(args[0]);
        int k = Integer.parseInt(args[1]);
        ArrayList<Flower> data = knnClassifier.preProcessData(new FileInputStream(file), k);
        ArrayList<ConfusionMatrix> cm = knnClassifier.makePredictions(data);
        
        System.out.println("\nRun time = " + (System.currentTimeMillis() - startTime) + " milliseconds");
        System.out.println("Pre-processed data {" + data.toString());
        System.out.println("Confusion Matrix data {" + cm.toString());
        System.out.println("\n------------- SUCCESS -------------");
        System.exit(0);
    }


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
            System.out.println("Size before reduction: " + tdSize);
            for(int j = tdSize-1; j >= k; j--){
            td.remove((td.get(j)));
            }
            System.out.println("Size after reduction: " + td.size());
                
        }
                

        return data;
    }
    
   
    private ArrayList<ConfusionMatrix> makePredictions(ArrayList<Flower> fl){
		
    	ArrayList<ConfusionMatrix> cm = new ArrayList<ConfusionMatrix>();
    	
    	
    	
    	for(int i = 0; i < fl.size(); i++){
            cm.add(new ConfusionMatrix(fl.get(i).getIdentity(), null));
    	}
    	
    	return cm;
    		
    }


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
    }

    /* This method finds the distance between two rows of data */
    private float eucDist(Flower a, Flower b) {
        return ((a.pl - b.pl) * (a.pl - b.pl) +
                (a.pw - b.pw) * (a.pw - b.pw) +
                (a.sl - b.sl) * (a.sl - b.sl) +
                (a.sw - b.sw) * (a.sw - b.sw));
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

        public String toString() {
            return "" + distance + " " + otherIdentity;
        }
        


		public int compareTo(TrainingDistance td) {
			if (this.distance > td.distance) return 1;
            else if (this.distance < td.distance) return -1;
            else return 0;
		}
    }

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
        
        public String majorityNeighbor(TrainingDistance td) {
    		
        	  
      	   
      	   return null;
      	   
         }
        
        
    }
    
    class ConfusionMatrix {
    	
    	String trueIdentity;
    	String predictedIdentity;
    	
    	public ConfusionMatrix(String ti, String pri) {
			trueIdentity = ti;
			predictedIdentity = pri;
		}
    	
        public String toString() {
            return "" + trueIdentity + " " + predictedIdentity;
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
    	
    }
}

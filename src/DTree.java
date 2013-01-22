

import java.io.*;
import java.util.*;

public class DTree {
	/* All training data */
	static ArrayList<Tuple> trainData;
	
	/* All test data */
	static ArrayList<Tuple> testData;
	
	/* Comination of all training and test data */
	static ArrayList<Tuple> allData;
	
	/* All leaf node */
	static ArrayList<DecisionTreeNode> leaf;
	
	/* Root of the decision tree */
	static DecisionTreeNode root;
	
	/* Other global variables */
	static boolean[] fecFlag;
	static int[] gtCountRes;
	static int[] ltCountRes;
	static ArrayList<Integer>[] index;
	static String[] result;
	static int[] locationCount, locationCountTest;
	static double curMax;
	static int selectedFeature;
	static int curValue;
	
	/**
	 * Calculate the entropy for a given p
	 * @param x
	 * @return
	 */
	public static double calcEntropy(double x) {
		if (x == 0.0) {
			return 0;
		}
		return (-x * (Math.log10(x) / Math.log10(2)));
	}

	/**
	 * Calculate the information gain for a given feature, which is stored by global variable
	 * @return
	 */
	public static double calcGain() {
		int gtN = sum(gtCountRes);
		int ltN = sum(ltCountRes);
		int n = gtN + ltN;
		// System.out.println("N=" + n + " gtN=" + gtN + " ltN=" + ltN);

		double I = 0.0;
		double gtI = 0.0;
		double ltI = 0.0;

		for (int i = 0; i < 6; i++) {
			I += calcEntropy(((double) gtCountRes[i] + (double) ltCountRes[i])
					/ (double) n);
			gtI += calcEntropy((double) gtCountRes[i] / (double) gtN);
			ltI += calcEntropy((double) ltCountRes[i] / (double) ltN);
		}
		// System.out.println("I=" + I);
		// System.out.println("ltI=" + ltI);
		// System.out.println("gtI=" + gtI);
		return I - (((double) ltN / (double) n) * ltI + ((double) gtN / (double) n) * gtI);
	}

	/**
	 * Sum the elements in an array
	 * @param list
	 * @return
	 */
	public static int sum(int[] list) {
		int sum = 0;
		for (int i : list)
			sum = sum + i;
		return sum;
	}
	
	/**
	 * At a given node, find the feature with largest information gain, 
	 * and use this feature to divide test data.
	 * @param curData
	 */
	public static void findThreshold(ArrayList<Tuple> curData) {
		curMax = 0.0;
		selectedFeature = -1;
		curValue = 0;
		// Iterate the index
		for (int i = 0; i < 10; i++) {
//			if (fecFlag[i]) {
//				continue;
//			}
			
			// Sort the training data by a given feature
			TupleComparator tc = new TupleComparator();
			tc.setCurrent(i);
			Collections.sort(curData, tc);

			// for (Tuple t:data) {
			// System.out.println(t.getLocationID() + " " + t.getStrength(i));
			// }
			// System.out.println("*************");

			double maxGain = 0;
			int maxIndex = -1;
			int curT;
			
			// Iterate all values of a feature to find the one with largest information gain
			for (int index = 0; index < curData.size(); index++) {
				Tuple t = curData.get(index);
				if (t.getStrength(i) == Integer.MIN_VALUE) {
					continue;
				} else {
					curT = t.getStrength(i);
					// System.out.println(curT);
					Arrays.fill(gtCountRes, 0);
					Arrays.fill(ltCountRes, 0);
					for (Tuple tt : curData) {
						if (tt.getStrength(i) != Integer.MIN_VALUE) {
							if (tt.getStrength(i) <= curT) {
								ltCountRes[tt.getLocationID()]++;
							} else {
								gtCountRes[tt.getLocationID()]++;
							}
						}
					}
					// for (int integ:ltCountRes) {
					// System.out.print(integ + " ");
					// }
					// System.out.println();
					// for (int integ:gtCountRes) {
					// System.out.print(integ + " ");
					// }
					// System.out.println();
					double gain = calcGain();
					// System.out.println("gain=" + gain);
					// System.out.println("maxGain=" + maxGain);
					if (gain > maxGain) {
						maxGain = gain;
						maxIndex = index;
					}
					// System.out.println("After update");
					// System.out.println("gain=" + gain);
					// System.out.println("maxGain=" + maxGain);
					// System.out.println("maxIndex=" + maxIndex);
					// System.out.println("value=" +
					// data.get(maxIndex).getStrength(i));
					// System.out.println("*******");
				}
			}
			// System.out.println("maxGain=" + maxGain);
			// System.out.println("maxIndex=" + maxIndex);
			// System.out.println("value=" +
			// curData.get(maxIndex).getStrength(i));
			// System.out.println("*******");
			if (maxGain > curMax) {
				curMax = maxGain;
				selectedFeature = i;
				curValue = curData.get(maxIndex).getStrength(i);
			}
		}

	}
	
	/**
	 *  Insert a node to the decision tree, with max depth.
	 * @param node: node to be inserted
	 * @param depth: max depth
	 * @return
	 */
	public static DecisionTreeNode insertNode(DecisionTreeNode node, int depth) {
		ArrayList<Tuple> list = node.getData();
		ArrayList<Tuple> ltList = new ArrayList<Tuple>();
		ArrayList<Tuple> gtList = new ArrayList<Tuple>();

		findThreshold(list);
		node.setFeature(selectedFeature);
		node.setThreshold(curValue);

		// selectedFeature == -1 means this is the leaf node
		// Also can cut the tree to a certain depth||| node.getLevel() <= depth && 
		if (node.getLevel() <= depth && selectedFeature > -1) {
			int temp = selectedFeature;
			fecFlag[temp] = true;
			System.out.println("For Level " + node.getLevel());
			System.out.println("Feature=" + selectedFeature + " , Value="
					+ curValue);

			TupleComparator tc = new TupleComparator();
			tc.setCurrent(selectedFeature);
			Collections.sort(list, tc);
			for (Tuple t : list) {
				if (t.getStrength(selectedFeature) <= curValue) {
					ltList.add(t);
				} else {
					gtList.add(t);
				}
			}

			// If this is not a leaf node, recursively insert two child nodes
			DecisionTreeNode child1 = new DecisionTreeNode();
			child1.setData(ltList);
			child1.setLevel(node.getLevel() + 1);
			insertNode(child1, depth);
			node.setLchild(child1);

			DecisionTreeNode child2 = new DecisionTreeNode();
			child2.setData(gtList);
			child2.setLevel(node.getLevel() + 1);
			insertNode(child2, depth);
			node.setRchild(child2);
			
			fecFlag[temp] = false;
		} else {
			// if leaf node, add to the leaf node set
			leaf.add(node);
			node.setFeature(-1);
//			System.out.println("*****");
//			for (int i = 0; i < list.size(); i++) {
//				System.out.println(list.get(i).location);
//			}
//			System.out.println("*****");
		}
		
		// Select the majority location in this group as the final result
		int[] sum = new int[6];
		Arrays.fill(sum, 0);
		for (Tuple t : list) {
			// System.out.println(t.getLocationID());
			sum[t.getLocationID()]++;
		}
		int max = -1;
		int maxId = -1;
		for (int i = 0; i < 6; i++) {
			// System.out.println(sum[i]);
			if (sum[i] > max
					|| ((sum[i] == max) && (i == list.get(0).getLocationID()))) {
				max = sum[i];
				maxId = i;
			}
		}
		// System.out.println(maxId);
		for (Tuple t : list) {
			if (t.getLocationID() == maxId) {
				node.setResult(t.location);
				break;
			}
		}
//		if (node.getLevel() > 2) {
//			System.out.println("Result=" + node.getResId());
//		}
		return node;
	}

	/**
	 *  Initiate and create the root node of a decision tree
	 * @param _trainData
	 * @param maxDepth
	 */
	public static void createTree(ArrayList<Tuple> _trainData, int maxDepth) {
		root = null;
		leaf = new ArrayList<DecisionTreeNode>();
		Arrays.fill(fecFlag, false);
		root = new DecisionTreeNode();
		root.setLevel(0);
		root.setData(_trainData);
		insertNode(root, maxDepth);
	}

	/**
	 * Calculate the training error rate for the decision tree
	 * @param train
	 * @return
	 */
	public static double calcTrainingAcc(ArrayList<Tuple> train) {
		Arrays.fill(locationCount, 0);
		for (Tuple t:train) {
			locationCount[t.getLocationID()]++;
		}
		int n = train.size();
		int[] error = new int[6];
		Arrays.fill(error, 0);
		System.out.println("|Train set|=" + n);
		int totalErr = 0;
		// Calculate the error rate for each location
		//for (int i = 0; i < 6; i++) {
			//int count = 0;
			for (DecisionTreeNode node : leaf) {
				//if (node.getResId() == i) {
					ArrayList<Tuple> list = node.getData();
					for (Tuple t : list) {
						if (t.getLocationID() != node.getResId()) {
							//count++;
							error[t.getLocationID()]++;
							totalErr++;
						}
					}
				//}
			}
		//}
		
//		for (int i = 0; i < 6; i++) {
//			System.out.println("Classification error rate for location " + i + " is " + ((double) error[i] / (double) locationCount[i]));
//		}
	
		//Calculate the total error rate
		System.out.println("Total classification error rate on training set: " + (double) totalErr / (double) n);
		
		return (double) totalErr / (double) n;
	}
	
	/**
	 * Calculate the error rate on a given test set.
	 * @param _test: the test set
	 * @return
	 */
	public static double calcPredictAcc(ArrayList<Tuple> _test) {
		Arrays.fill(locationCountTest, 0);
		int n = _test.size();
		int[] error = new int[6];
		Arrays.fill(error, 0);
		System.out.println("|Test set|=" + n);
		// int count = 0;
		int totalErr = 0;
		for (Tuple t : _test) {
			locationCountTest[t.getLocationID()]++;
			// System.out.println("Testcase " + count);
			DecisionTreeNode node = root;
			int f = -1;
			while (node != null && (f = node.getFeature()) > -1) {
				node = (t.getStrength(f) <= node.getThreshold()) ? node.lchild : node.rchild;
			}
			if (node != null && node.getResId() != t.getLocationID()) {
				totalErr++;
				error[t.getLocationID()]++;
			}
		}
		
//		for (int i = 0; i < 6; i++) {
//			System.out.println("Classification error rate for location " + i + " is " + ((double) error[i] / (double) locationCountTest[i]));
//		}
		System.out.println("Classification error rate on test set " + (double) totalErr / (double)n);
		return (double) totalErr / (double) n;
	}
	
	/**
	 * User 10-fold validation to check the result error rate
	 * @param depth
	 */
	public static void crossValidation(int depth){
		@SuppressWarnings("unchecked")
		ArrayList<Tuple>[] folds = new ArrayList[10]; 
		for (int i = 0; i < 10; i++) {
			folds[i] = new ArrayList<Tuple>();
		}
		
		// Combine test and training data
		allData = new ArrayList<Tuple>();
		allData.addAll(trainData);
		allData.addAll(testData);
		
		// Divide all the data randomly into 10 sets
		Collections.shuffle(allData);
		for (int i = 0; i < allData.size(); i++) {
			folds[i % 10].add(allData.get(i));
		}
		
		ArrayList<Tuple> train = new ArrayList<Tuple>();
		ArrayList<Tuple> test = new ArrayList<Tuple>();
		
		// Each time, use 1 of 10 sets as a test set
		for (int i = 0; i < 10; i++) {
			System.out.println("Use the " + (i + 1) + "th set of 10 sets as testing set:");
			train = new ArrayList<Tuple>();
			test = new ArrayList<Tuple>();
			//System.out.println("Set length=" + folds[i].size());
			
			test = folds[i];
			for (int j = 0; j < 10; j++) {
				if (j != i) {
					train.addAll(folds[j]);
				}
			}
			//System.out.println(train.size() + " " + test.size());
			createTree(train, depth);
			calcTrainingAcc(train);
			calcPredictAcc(test);
			System.out.println("********END TEST********");
		}
	}
	
	/**
	 *  Given a training/validation ratio, find the best tree depth that leads to least errors.
	 * @param ratio
	 * @return
	 */
	public static int findBestDepth(double ratio){
		ArrayList<Tuple> train = new ArrayList<Tuple>();
		ArrayList<Tuple> validation = new ArrayList<Tuple>();
		ArrayList<Tuple> test = new ArrayList<Tuple>();
		train.addAll(trainData);
		test.addAll(testData);
		
		// Randomly select validation set from the training set, obeying the ratio
		int tSize = train.size();
		int vSize = (int)((double)tSize * ratio);
		
		for (int i = 0; i < vSize; i++) {
			int k = (int)(Math.random() * tSize);
			validation.add(train.get(k));
			train.remove(k);
			tSize--;
		}
		
		System.out.println(train.size() + " " + validation.size());
		
		// Find the optimal depth, create the optimal tree
		int bestDepth = -1;
		double bestErr = 1.0;
		
		for (int i = 11; i >=1; i--) {
			createTree(train, i);
			double cur =  calcPredictAcc(validation);
			if (cur < bestErr) {
				bestErr = cur;
				bestDepth = i;
			}
		}
		// Create the optimal tree
		createTree(train, bestDepth);
		System.out.println("Best Depth=" + bestDepth + ", with err rate=" + bestErr);
		System.out.println("Now 1st time test on test set");
		calcPredictAcc(test);
		
		System.out.println("***************************\nTest best depth on random validation set + test for 9 times:");
		
		for (int kk = 0; kk < 9; kk++) {
			// Randomly generate validation set again
			train = new ArrayList<Tuple>();
			validation = new ArrayList<Tuple>();
			train.addAll(trainData);
			tSize = train.size();
			for (int i = 0; i < vSize; i++) {
				int k = (int)(Math.random() * tSize);
				validation.add(train.get(k));
				train.remove(k);
				tSize--;
			}
			System.out.println(train.size() + " " + validation.size());
			// Create the optimal tree
			createTree(train, bestDepth);
			// Test on validation set
			System.out.println("The " + (kk + 1) + " time to test on validation set:");
			calcPredictAcc(validation);
			// Test on test set
			System.out.println("The " + (kk + 1) + " time to test on test set:");
			calcPredictAcc(test);
		}
		return bestDepth;
	}
	
	/**
	 * Iterate all the ratio and find one with the best error rate
	 */
	public static void findBestRatio(){
		double[] ratios = {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9};
		for (double i:ratios) {
			System.out.println("Ratio=" + i);
			findBestDepth(i);
			System.out.println("End for ratio " + i);
		}
	}
	
	/**
	 * Read test data and training data
	 * @param filename
	 * @param test
	 * @throws IOException
	 */
	public static void readFile(String filename, boolean test) throws IOException {
		int count = 0;
		String line;
		BufferedReader input = new BufferedReader(new FileReader(filename));

		while ((line = input.readLine()) != null) {
			Tuple t = new Tuple();
			// System.out.println("s1 " + line);
			// System.out.println("COUNT==" + count);
			StringTokenizer token = new StringTokenizer(line, " ");
			// System.out.println(line);
			String[] readin = new String[token.countTokens()];
			// System.out.println("num : " + token.countTokens());
			for (int i = 0; i < readin.length - 1; i += 2) {
				String s = token.nextToken().trim();
				int div = s.indexOf(':');
				int id = Integer.parseInt(s.substring(0, div));
				int strength = Integer.parseInt(token.nextToken().trim());
				// hm.put(id, strength);
				index[id].add(strength);
				t.setStrength(id, strength);
			}

			// extract the location
			for (int i = line.length() - 1; i >= 0; i--) {
				if (line.charAt(i) == ' ') {
					result[count] = line.substring(i + 1);
					t.setLocation(result[count]);
					break;
				}
			}
			if (!test) {
				trainData.add(t);
			} else {
				testData.add(t);
			}

			count++;
		}
		input.close();
	}

	/**
	 * Main method
	 * @param args
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws IOException {
		trainData = new ArrayList<Tuple>();
		testData = new ArrayList<Tuple>();
		result = new String[240];
		index = new ArrayList[10];
		locationCount = new int[6];
		locationCountTest= new int[6];
		// threshold = new int[10];
		gtCountRes = new int[6];
		ltCountRes = new int[6];
		fecFlag = new boolean[10];

		for (int i = 0; i < 10; i++) {
			index[i] = new ArrayList<Integer>();
		}
		readFile("Training set.train", false);
		readFile("Testing set.test", true);
		
		/* For single test with depth*/
		createTree(trainData, 2);
		calcTrainingAcc(trainData);
		//calcPredictAcc(testData);
		
		/* For cross validation with depth */
		//crossValidation(2);
		
		/* For finding best train/validation ratio */
	//	findBestRatio();
	}
	
	/**
	 * Class to compare tuples according to different features
	 */
	static class TupleComparator implements Comparator<Tuple> {
		int current;

		public int getCurrent() {
			return current;
		}

		public void setCurrent(int current) {
			this.current = current;
		}

		@Override
		public int compare(Tuple o1, Tuple o2) {
			// TODO Auto-generated method stub
			switch (current) {
			case 0:
				return o1.getStrength(0) - o2.getStrength(0);
			case 1:
				return o1.getStrength(1) - o2.getStrength(1);
			case 2:
				return o1.getStrength(2) - o2.getStrength(2);
			case 3:
				return o1.getStrength(3) - o2.getStrength(3);
			case 4:
				return o1.getStrength(4) - o2.getStrength(4);
			case 5:
				return o1.getStrength(5) - o2.getStrength(5);
			case 6:
				return o1.getStrength(6) - o2.getStrength(6);
			case 7:
				return o1.getStrength(7) - o2.getStrength(7);
			case 8:
				return o1.getStrength(8) - o2.getStrength(8);
			case 9:
				return o1.getStrength(9) - o2.getStrength(9);
			default:
				return 0;
			}
		}
	}
	
	/**
	 * Map location string to id number
	 * @param location
	 * @return
	 */
	public static int getLocationId(String location) {
		int locationId = -1;
		if (location.equals("LOUNGE")) {
			locationId = 0;
		} else if (location.equals("HALLWAY")) {
			locationId = 1;
		} else if (location.equals("ATRIUM_1")) {
			locationId = 2;
		} else if (location.equals("ATRIUM_2")) {
			locationId = 3;
		} else if (location.equals("MATTINS")) {
			locationId = 4;
		} else if (location.equals("PHILLIPS")) {
			locationId = 5;
		}
		return locationId;
	}
	
	/**
	 * Class represents a line in the dataset.
	 */
	static class Tuple {
		/* Classification result */
		String location;
		int locationId;
		
		/* All features */
		int[] strength;

		public Tuple() {
			strength = new int[10];
			for (int i = 0; i < 10; i++) {
				strength[i] = Integer.MIN_VALUE;
			}
		}

		public void setLocation(String str) {
			location = str;
			locationId = getLocationId(location);
		}

		public int getLocationID() {
			return locationId;
		}

		public void setStrength(int index, int value) {
			strength[index] = value;
		}

		public int getStrength(int index) {
			return strength[index];
		}
	}
	
	/**
	 * Class represents a node in a decision tree
	 */
	static class DecisionTreeNode {
		/* Depth of the node */
		int level;
		
		/* Feature used by this node to select data, -1 for leaf node */
		int feature;
		
		/* Binary threshold used to select data */
		int threshold;
		
		/* All data contains in this node */
		ArrayList<Tuple> data;
		
		/* Children of this node */
		DecisionTreeNode lchild;
		DecisionTreeNode rchild;
		
		/* Corresponding classification result of this node */
		String result;
		
		/* Id of the location string */
		int resId;


		public int getLevel() {
			return level;
		}

		public void setLevel(int level) {
			this.level = level;
		}

		public int getFeature() {
			return feature;
		}

		public void setFeature(int feature) {
			this.feature = feature;
		}

		public int getThreshold() {
			return threshold;
		}

		public void setThreshold(int threshold) {
			this.threshold = threshold;
		}

		public DecisionTreeNode getLchild() {
			return lchild;
		}

		public void setLchild(DecisionTreeNode lchild) {
			this.lchild = lchild;
		}

		public DecisionTreeNode getRchild() {
			return rchild;
		}

		public void setRchild(DecisionTreeNode rchild) {
			this.rchild = rchild;
		}

		public void setData(ArrayList<Tuple> _data) {
			data = _data;
		}

		public void addData(Tuple t) {
			data.add(t);
		}

		public ArrayList<Tuple> getData() {
			return data;
		}

		public String getResult() {
			return result;
		}

		public void setResult(String result) {
			this.result = result;
			this.resId = getLocationId(result);
		}

		public int getResId() {
			return resId;
		}

		public void setResId(int resId) {
			this.resId = resId;
		}
		public DecisionTreeNode() {
			data = new ArrayList<Tuple>();
		}
	}
}

package AI;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

import Neural.NeuralNetwork;

public class Behaviour implements Serializable {
	private static final long serialVersionUID = 6795916425147912587L;
	final double discountFactor = 0.9;
	NeuralNetwork n;
	double[] rewardWeights;
	int[] inputKey;
	int[] size;
	String ID;

	public Behaviour(int[] size, double[] rewardWeights, int[] inputKey, String ID) {
		this.ID = ID;
		this.rewardWeights = rewardWeights.clone();
		this.inputKey = inputKey.clone();
		this.size = size;

		this.size[0] = inputKey.length;

		n = new NeuralNetwork(size);
	}

	public Behaviour(int[] size, double[] rewardWeights, int[] inputKey, String ID, double rateDecay) {
		this.ID = ID;
		this.rewardWeights = rewardWeights.clone();
		this.inputKey = inputKey.clone();
		this.size = size;

		this.size[0] = inputKey.length;

		n = new NeuralNetwork(size, rateDecay);
	}

	private double[] decodeInput(double[] rawInput) {
		double[] input = new double[inputKey.length];
		for (int i = 0; i < inputKey.length; i++) {
			input[i] = rawInput[inputKey[i]];
		}
		return input;
	}

	public double[] feedForward(double[] rawInput) {
		double[] input = decodeInput(rawInput);
//		if (this instanceof Navigate)
//			System.out.println(Arrays.toString(input));
		return n.forwardProp(input);

	}

	public void updateNetwork(State current, State next, boolean mainBehaviour) {
		double reward = 0; //step reward

		for (int i = 0; i < current.rewards.length; i++) {
			reward += rewardWeights[i] * current.rewards[i];
			// System.out.println(rewardWeights[i] * current.rewards[i]);
		}
		// System.out.println("Reward: "+reward+"\n");
		double[] output;
		double[] expectedOutput;
		double[] nextExpected;
		double[] input = this.decodeInput(current.input);
//		 System.out.println(this.ID+", "+reward+", "+Arrays.toString(input));
		double max;

		if (next != null) {
			output = n.forwardProp(decodeInput(next.input));
			nextExpected = output.clone();
			max = nextExpected[0];
			for (int j = 1; j < nextExpected.length; j++) {
				// String str = String.format("%1.2f", nextExpected[j]) + " ";
				// System.out.print(str);
				if (nextExpected[j] > max) {
					max = nextExpected[j];
				}
			}
			// System.out.println();
		} else {
			max = 0;
		}

		output = n.forwardProp(input);
		if (mainBehaviour || output.length > current.action) {
			expectedOutput = output.clone();
			if (mainBehaviour) {
				expectedOutput[current.behaviour] = reward + discountFactor * max;
			} else {

				expectedOutput[current.action] = reward + discountFactor * max;

			}
			n.backProp(input, expectedOutput);
			// if (!mainBehaviour)
			// System.out.println(this.getClass()+" "+reward + " " +
			// n.squaredError+" "+Arrays.toString(this.inputKey));
		}

	}

	public void save(String fileName, int t, int e) throws IOException {
		File dir = new File("AIs");
		File subDir = new File(dir, t + fileName + e);
		subDir.mkdirs();

		File f = new File(subDir, this.ID);
		System.out.println("Saved AI to file: " + f.getPath());
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f));
		oos.writeObject(this.n);
		oos.flush();
		oos.close();

	}

	private Object readFromFile(String file) throws FileNotFoundException, IOException, ClassNotFoundException {
		File dir = new File("AIs");
		File subFolder = new File(dir, file);
		subFolder.mkdirs();
		File f = new File(subFolder, this.ID);

		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f.getAbsoluteFile()));
		Object o = ois.readObject();
		ois.close();
		return o;
	}

	public void load(String fileName) {
		try {
			this.n = (NeuralNetwork) readFromFile(fileName);
		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			System.err.println("Snapshot not available");
			e.printStackTrace();
			System.exit(2);
		}
		System.out.println("Loaded "+this.ID +" from disk");

	}

	public void updateLearnRate() {
		n.updateLearningRate();
	}

}

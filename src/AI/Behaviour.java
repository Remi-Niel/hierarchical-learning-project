package AI;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import Neural.NeuralNetwork;

public class Behaviour implements Serializable {
	private static final long serialVersionUID = 6795916425147912587L;
	final double gamma = 0.9999;
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
		if (this.size[0] == -1) {
			this.size[0] = inputKey.length;
		}
		n = new NeuralNetwork(size);
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
		return n.forwardProp(input);

	}

	public void updateNetwork(State current, State next, boolean mainBehaviour) {
		double reward = 0;

		for (int i = 0; i < current.rewards.length; i++) {
			reward += rewardWeights[i] * current.rewards[i];
		}
		// System.out.println("Reward: "+reward+"\n");
		double[] output;
		double[] expectedOutput;
		double[] nextExpected;
		double[] input = current.input;
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
				expectedOutput[current.behaviour] = reward + gamma * max;
			} else {

				expectedOutput[current.action] = reward + gamma * max;

			}
			n.backProp(input, expectedOutput);
		}

	}

}

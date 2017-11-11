package AI;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import Neural.NeuralNetwork;

public class behaviour {
	final double gamma = 0.98;
	NeuralNetwork n;
	int[] rewardWeights;
	int[] inputKey;
	Queue<double[]> inputs;
	Queue<Integer> action;
	int[] size;

	public behaviour(int[] size, int[] rewardWeights, int[] inputKey) {
		n = new NeuralNetwork(size);
		this.rewardWeights = rewardWeights.clone();
		inputs = new LinkedList<double[]>();
		this.inputKey = inputKey.clone();
		this.size = size;
		action = new LinkedList<Integer>();
	}

	private double[] decodeInput(double[] rawInput) {
		double[] input = new double[inputKey.length];
		for (int i = 0; i < inputKey.length; i++) {
			input[i]=rawInput[inputKey[i]];
		}
		return input;
	}

	public void updateNetwork(int state, int[] rewards) {
		double reward = 0;

		for (int i = 0; i < rewards.length; i++) {
			reward += rewardWeights[i] * rewards[i];
		}

		double[][] activation;
		double[] expectedOutput;
		double[] nextExpected;
		double[] input = inputs.poll();
		double max;

		if (inputs.size() > 0) {
			activation = n.forwardProp(decodeInput(inputs.peek()));
			nextExpected = activation[size.length - 1].clone();
			max = Double.NEGATIVE_INFINITY;
			for (int j = 0; j < nextExpected.length; j++) {
				// String str = String.format("%1.2f", nextExpected[j]) + " ";
				// System.out.print(nextExpected[j]);
				if (nextExpected[j] > max) {
					max = nextExpected[j];
				}
			}

		} else {
			max = 0;
		}

		activation = n.forwardProp(input);
		expectedOutput = activation[size.length - 1].clone();
		expectedOutput[action.poll()] = reward + gamma * max;

		n.backProp(activation, expectedOutput);

	}

}

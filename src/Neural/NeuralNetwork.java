package Neural;

import java.io.Serializable;
import java.text.*;
import java.util.*;

public class NeuralNetwork implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2273583349766512604L;

	static {
		Locale.setDefault(Locale.ENGLISH);
	}
	public double squaredError=0;
	
	final boolean isTrained = false;
	final DecimalFormat df;
	final Random rand = new Random(System.currentTimeMillis());
	ArrayList<ArrayList<Neuron>> network = new ArrayList<ArrayList<Neuron>>(3);
	final Neuron bias = new Neuron();
	final int[] layers;
	final double randomWeightMultiplier = 2;

	final double epsilon = 0.00000000001;

	final double learningRate = 0.000001f;
	final double momentum = 0.01f;

	// Inputs for xor problem
	final double inputs[][] = { { 1, 1 }, { 1, 0 }, { 0, 1 }, { 0, 0 } };

	// Corresponding outputs, xor training data
	final double expectedOutputs[][] = { { 0 }, { 1 }, { 1 }, { 0 } };
	double resultOutputs[][] = { { -1 }, { -1 }, { -1 }, { -1 } }; // dummy init
	double output[];

	// for weight update all
	final HashMap<String, Double> weightUpdate = new HashMap<String, Double>();

	public static void main(String[] args) {
		NeuralNetwork nn = new NeuralNetwork(new int[] { 2, 30, 20, 1 });
		int maxRuns = 500000;
		double minErrorCondition = 0.00001;
		nn.run(maxRuns, minErrorCondition);
	}

	public NeuralNetwork(int[] size) {
		this.layers = size;
		df = new DecimalFormat("#.0#");

		/**
		 * Create all neurons and connections Connections are created in the
		 * neuron class
		 */
		for (int i = 0; i < layers.length; i++) {
			network.add(new ArrayList<Neuron>());
			for (int j = 0; j < layers[i]; j++) {
				Neuron neuron = new Neuron();
				if (i != 0) {
					neuron.addInConnectionsS(network.get(i - 1));
					neuron.addBiasConnection(bias);
				}
				network.get(i).add(neuron);
			}
		}
		for (ArrayList<Neuron> layer : network.subList(1, network.size())) {
			for (Neuron neuron : layer) {
				ArrayList<Connection> connections = neuron.getAllInConnections();
				for (Connection conn : connections) {
					double netWeight = getRandom();
					conn.setWeight(netWeight);
				}
			}
		}

		// reset id counters
		Neuron.counter = 0;
		Connection.counter = 0;

		if (isTrained) {
			trainedWeights();
			updateAllWeights();
		}
	}

	// random
	double getRandom() {
		return randomWeightMultiplier * (rand.nextDouble() * 2 - 1); // [-1;1]
	}

	/**
	 * 
	 * @param inputs
	 *            There is equally many neurons in the input layer as there are
	 *            in input variables
	 */
	public void setInput(double inputs[]) {
		for (int i = 0; i < network.get(0).size(); i++) {
			network.get(0).get(i).setOutput(inputs[i]);
		}
	}

	public double[] getOutput() {
		double[] outputs = new double[network.get(network.size() - 1).size()];
		for (int i = 0; i < network.get(network.size() - 1).size(); i++)
			outputs[i] = network.get(network.size() - 1).get(i).getOutput();
		return outputs;
	}

	/**
	 * Calculate the output of the neural network based on the input The forward
	 * operation
	 */
	public void activate() {
		for (int i = 1; i < network.size(); i++) {
			for (Neuron n : network.get(i)) {
				n.calculateOutput(i!=network.size()-1);
			}
			//System.out.println("\n");
		}

		//System.out.println("\n");
	}

	public double[] forwardProp(double[] input) {
		this.setInput(input);
		this.activate();
		return this.getOutput();
	}

	public void backProp(double[] input, double[] expectedOutput) {
		this.setInput(input);
		this.applyBackpropagation(expectedOutput);
	}

	/**
	 * all output propagate back
	 * 
	 * @param expectedOutput
	 *            first calculate the partial derivative of the error with
	 *            respect to each of the weight leading into the output neurons
	 *            bias is also updated here
	 */
	public void applyBackpropagation(double expectedOutput[]) {

		double[][] error = new double[layers.length][];
		for (int i = 0; i < layers.length; i++) {
			error[i] = new double[layers[i]];
		}
		for (int i = 0; i < expectedOutput.length; i++) {
			error[layers.length - 1][i] = expectedOutput[i];
		}

		int i = 0;
		squaredError=0;
		//update weights for output layer
		for (Neuron n : network.get(network.size() - 1)) {
			ArrayList<Connection> connections = n.getAllInConnections();
			for (Connection con : connections) {
				double ak = n.getOutput();
				double ai = con.leftNeuron.getOutput();
				double desiredOutput = expectedOutput[i];
				squaredError+=(desiredOutput-ak)*(desiredOutput-ak);
				double partialDerivative = (desiredOutput - ak);
				double deltaWeight = -learningRate * ai * partialDerivative;
				double newWeight = con.getWeight() + deltaWeight;
				con.setDeltaWeight(deltaWeight);
				con.setWeight(newWeight + momentum * con.getPrevDeltaWeight());
			}
			i++;
		}

		// update weights for the hidden layer
		for (i = layers.length - 2; i > 0; i--) {
			int k = 0;
			for (Neuron n : network.get(i)) {
				ArrayList<Connection> connections = n.getAllInConnections();
				for (Connection con : connections) {
					double aj = n.getOutput();
					double ai = con.leftNeuron.getOutput();
					double sumKoutputs = 0;
					int j = 0;
					for (Neuron out_neu : network.get(i + 1)) {
						double wjk = out_neu.getConnection(n.id).getWeight();
						double ak = out_neu.getOutput();
						if (i == layers.length - 2) {
							double desiredOutput = (double) expectedOutput[j];
							sumKoutputs = sumKoutputs + (-(desiredOutput - ak) * ak * (1 - ak) * wjk);
						} else {
							sumKoutputs = sumKoutputs + ((out_neu.getError()) * ak * (1 - ak) * wjk);
						}
						j++;
					}
					n.setError(sumKoutputs);

					double partialDerivative = aj * (1 - aj) * ai * sumKoutputs;
					double deltaWeight = -learningRate * partialDerivative;
					double newWeight = con.getWeight() + deltaWeight;
					con.setDeltaWeight(deltaWeight);
					con.setWeight(newWeight + momentum * con.getPrevDeltaWeight());
					k++;
				}
			}
		}
	}

	void run(int maxSteps, double minError) {
		int i;
		// Train neural network until minError reached or maxSteps exceeded
		double error = 1;
		for (i = 0; i < maxSteps && error > minError; i++) {
			error = 0;
			for (int p = 0; p < inputs.length; p++) {
				setInput(inputs[p]);

				activate();

				output = getOutput();
				resultOutputs[p] = output;

				for (int j = 0; j < expectedOutputs[p].length; j++) {
					double err = Math.pow(output[j] - expectedOutputs[p][j], 2);
					error += err;
				}

				applyBackpropagation(expectedOutputs[p]);
			}
		}

		printResult();

		System.out.println("Sum of squared errors = " + error);
		System.out.println("##### EPOCH " + i + "\n");
		if (i == maxSteps) {
			System.out.println("!Error training try again");
		} else {
			// printAllWeights();
			// printWeightUpdate();
		}
	}

	void printResult() {
		System.out.println("NN example with xor training");
		for (int p = 0; p < inputs.length; p++) {
			System.out.print("INPUTS: ");
			for (int x = 0; x < layers[0]; x++) {
				System.out.print(inputs[p][x] + " ");
			}

			System.out.print("EXPECTED: ");
			for (int x = 0; x < layers[layers.length - 1]; x++) {
				System.out.print(expectedOutputs[p][x] + " ");
			}

			System.out.print("ACTUAL: ");
			for (int x = 0; x < layers[layers.length - 1]; x++) {
				System.out.print(resultOutputs[p][x] + " ");
			}
			System.out.println();
		}
		System.out.println();
	}

	String weightKey(int neuronId, int conId) {
		return "N" + neuronId + "_C" + conId;
	}

	/**
	 * Take from hash table and put into all weights
	 */
	public void updateAllWeights() {
		// update weights for the output layer
		for (Neuron n : network.get(network.size() - 1)) {
			ArrayList<Connection> connections = n.getAllInConnections();
			for (Connection con : connections) {
				String key = weightKey(n.id, con.id);
				double newWeight = weightUpdate.get(key);
				con.setWeight(newWeight);
			}
		}
		// update weights for the hidden layer
		for (int i = layers.length - 2; i > 0; i--) {
			for (Neuron n : network.get(i)) {
				ArrayList<Connection> connections = n.getAllInConnections();
				for (Connection con : connections) {
					String key = weightKey(n.id, con.id);
					double newWeight = weightUpdate.get(key);
					con.setWeight(newWeight);
				}
			}
		}

	}

	// trained data
	void trainedWeights() {
		weightUpdate.clear();

		weightUpdate.put(weightKey(3, 0), 1.03);
		weightUpdate.put(weightKey(3, 1), 1.13);
		weightUpdate.put(weightKey(3, 2), -.97);
		weightUpdate.put(weightKey(4, 3), 7.24);
		weightUpdate.put(weightKey(4, 4), -3.71);
		weightUpdate.put(weightKey(4, 5), -.51);
		weightUpdate.put(weightKey(5, 6), -3.28);
		weightUpdate.put(weightKey(5, 7), 7.29);
		weightUpdate.put(weightKey(5, 8), -.05);
		weightUpdate.put(weightKey(6, 9), 5.86);
		weightUpdate.put(weightKey(6, 10), 6.03);
		weightUpdate.put(weightKey(6, 11), .71);
		weightUpdate.put(weightKey(7, 12), 2.19);
		weightUpdate.put(weightKey(7, 13), -8.82);
		weightUpdate.put(weightKey(7, 14), -8.84);
		weightUpdate.put(weightKey(7, 15), 11.81);
		weightUpdate.put(weightKey(7, 16), .44);
	}

	public void printWeightUpdate() {
		System.out.println("printWeightUpdate, put this i trainedWeights() and set isTrained to true");
		// weights for the hidden layer
		for (ArrayList<Neuron> layer : network.subList(1, network.size())) {
			for (Neuron n : layer) {
				ArrayList<Connection> connections = n.getAllInConnections();
				for (Connection con : connections) {
					String w = df.format(con.getWeight());
					System.out.println("weightUpdate.put(weightKey(" + n.id + ", " + con.id + "), " + w + ");");
				}
			}
		}
		System.out.println();
	}

	public void printAllWeights() {
		System.out.println("printAllWeights");
		// weights for the hidden layer
		for (ArrayList<Neuron> layer : network.subList(1, network.size())) {
			for (Neuron n : layer) {
				ArrayList<Connection> connections = n.getAllInConnections();
				for (Connection con : connections) {
					double w = con.getWeight();
					System.out.println("n=" + n.id + " c=" + con.id + " w=" + w);
				}
			}
		}
		System.out.println();
	}
}
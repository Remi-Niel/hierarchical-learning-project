package maxQQ;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import Main.Model;
import Neural.NeuralNetwork;
import maxQQ.tasks.Navigate;
import maxQQ.tasks.OpenDoor;
import maxQQ.tasks.Root;

public abstract class SubTask implements AbstractAction, Serializable {

	protected double discountfactor = 0.9;
	protected double epsilon = 0.0;
	double minTemp = .1;
	double decay = .98;
	double maxEpsilon = .99;
	double epsilonIncrement = 0.001;
	private AbstractAction[] subTasks;
	protected NeuralNetwork net;
	protected transient Model model;
	protected int[] inputKey;
	protected int startTime;
	protected int rewarded;
	protected int lastActionTime;
	protected int chosenAction;
	protected double rewardSum;
	protected double currentReward;
	protected double currentPseudoReward;
	protected double temp = 100;
	ArrayList<double[]> inputHistory;
	int windowSize = 1000;
	transient MovingAverage avg;
	MovingAverage avgError;
	Random rand;

	public class MovingAverage {
		private double[] window;
		private int n, insert;
		private double sum;

		public MovingAverage(int size) {
			window = new double[size];
			insert = 0;
			sum = 0;
		}

		public double next(double val) {
			if (n < window.length)
				n++;
			sum -= window[insert];
			sum += val;
			window[insert] = val;
			insert = (insert + 1) % window.length;
			return sum / n;
		}

		public double getAvg() {
			return sum / n;
		}
	}

	public SubTask(AbstractAction[] as, int[] size, int[] inputKey, Model m, int time) {
		subTasks = as.clone();
		this.inputKey = inputKey;
		rand = new Random();
		net = new NeuralNetwork(size.clone());
		model = m;
		startTime = time;
		inputHistory = new ArrayList<double[]>();
		rewardSum = currentReward = rewarded = 0;
		avg = new MovingAverage(windowSize);
		avgError = new MovingAverage(windowSize);
	}

	public void setModel(Model m, int time) {
		this.model = m;
		startTime = time;
		rewardSum = currentReward = rewarded = 0;
		avg = new MovingAverage(windowSize);
	}

	@Override
	public int getAction() {
		// TODO Auto-generated method stub
		return -1;
	}

	protected double[] decodeInput(double[] rawInput) {
		double[] input = new double[inputKey.length];
		for (int i = 0; i < inputKey.length; i++) {
			input[i] = rawInput[inputKey[i]];
		}
		return input;
	}

	@Override
	public AbstractAction getSubtask(double[] rawInput, int time, boolean b) {
		// if(this instanceof Navigate)System.out.print("Nav: ");
		// TODO Auto-generated method stub
		lastActionTime = time;
		currentReward = currentPseudoReward = 0;

		double[] input = decodeInput(rawInput);
		double[] out = net.forwardProp(input);
		double normalized[] = new double[out.length];
		int h = -1;

		double max = out[0];
		for (int i = 1; i < out.length; i++) {
			if (out[i] >= max) {
				max = out[i];
				h = i;
			}
		}

		if (b) {
			h = rand.nextInt(out.length);

			double sum = 0;

			for (int i = 0; i < out.length; i++) {
				normalized[i] = Math.exp((out[i] - max) / temp);
				sum += normalized[i];
			}
			double r = rand.nextDouble();
			double chanceSum = 0;

			for (int i = 0; i < out.length; i++) {
				normalized[i] /= sum;
				chanceSum += normalized[i];
				if (r <= chanceSum) {
					h = i;
					break;
				}
			}
//		if (!(this instanceof Navigate))
//			System.out.println(this.getClass() + ": " +this.subTasks[h].getClass()+", "+ Arrays.toString(out));
		} else {
			h = 0;
			max = out[0];
			for (int i = 1; i < out.length; i++) {
				if (out[i] >= max) {
					max = out[i];
					h = i;
				}
			}
//			System.out.println(this.getClass());
//			System.out.println(Arrays.toString(input));
//			System.out.println(Arrays.toString(out));
		}

		chosenAction = h;
//		if ((this instanceof Navigate)) {
//			// System.out.println();
//			System.out.println(this.getClass() + ", " + subTasks[chosenAction].getClass()+", "+Arrays.toString(out));
//		}
		if (h == -1) {
			// System.out.println(Arrays.toString(rawInput));
			// System.out.println(Arrays.toString(normalized));
			// System.out.println(Arrays.toString(out));
			// net.printAllWeights();
			System.err.println("Softmax did not return value for some reason, last option is assumed " + h);
			chosenAction = out.length - 1;
			System.exit(1);
		}
		// System.out.println(chosenAction);

		return subTasks[chosenAction];
	}

	public void reward(ArrayList<double[]> inHist, double[] current, double reward, boolean terminate, int time,
			boolean learn) {

		// System.out.println("Rewarding: "+this.getClass());
		inputHistory.addAll(inHist);
		rewardSum += Math.pow(discountfactor, time - startTime) * (reward + currentReward);
		double local = (reward + currentReward + currentPseudoReward);
//		if ((this instanceof OpenDoor)) {
//			System.out
//					.println(this.getClass() + ", " + subTasks[chosenAction].getClass() + ", " + reward + ", " + local);
//		}
		double[] out;
		double max = 0;
		if (!terminate) {
			double[] decodedInput = this.decodeInput(current);
			double[] nextExpected = net.forwardProp(decodedInput);

			max = nextExpected[0];
			for (int i = 1; i < nextExpected.length; i++) {
				if (nextExpected[i] > max) {
					max = nextExpected[i];
				}
			}
		}else{
//			System.out.println(this.getClass()+": terminate");
			
		}
		boolean first = true;
		int count = 0;
		for (double[] in : inputHistory.subList(lastActionTime - startTime, lastActionTime - startTime + 1)) {
			out = net.forwardProp(decodeInput(in));
			out[chosenAction] = local;
			if (!terminate) {
				out[chosenAction] += Math.pow(discountfactor, time - lastActionTime - count) * max;
			}
			if (learn) {
				net.backProp(in, out.clone());
				avgError.next(net.squaredError);
				// if (!(this instanceof Navigate))
				// System.out.println(this.getClass() + ", " + time + ", " +
				// local + ", " + max + ", "+ net.squaredError + ", " +
				// rewardSum);
			}
			local = local / discountfactor;
			count++;
		}

		if ((this instanceof Root) && terminate) {
			System.out.println("Final reward for this trial: " + this.rewardSum);
			System.out.println("Average final reward last " + windowSize + " trials: " + this.avg.next(this.rewardSum));
		}

		currentReward = currentPseudoReward = 0;
	}

	@Override
	public boolean primitive() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public abstract boolean finished(double[] input, Model model, int time);

	public double getRewardSum() {
		return rewardSum;
	}

	public ArrayList<double[]> getInHist() {
		return (ArrayList<double[]>) inputHistory.clone();
	}

	public void setStartTime(int t) {
		startTime = t;
	}

	public void finish() {
		currentReward = 0;
		rewardSum = 0;
		inputHistory.clear();
	}

	public void addHistory(double[] input) {
		inputHistory.add(input.clone());
		// System.out.println("Added history "+inputHistory.size());

	}

	public void updateDiscount() {
//		System.out.println(this.getClass() + " avg abs error for last " + this.windowSize + " learn steps: "
//				+ Math.sqrt(this.avgError.getAvg()));

		epsilon += epsilonIncrement;
		epsilon = Math.min(epsilon, maxEpsilon);
		temp = decay * temp;
		temp = Math.max(temp, minTemp);
		net.updateLearningRate();
		// System.out.println(this.getClass()+ " epsilon: "+epsilon);
	}

}

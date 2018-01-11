package maxQQ;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import Main.Model;
import Neural.NeuralNetwork;
import maxQQ.tasks.Navigate;
import maxQQ.tasks.Root;

public abstract class SubTask implements AbstractAction {

	protected double discountfactor = 0.9999;
	protected double epsilon = 0.1;
	double maxEpsilon = .99;
	double epsilonIncrement=0.00001;
	private AbstractAction[] subTasks;
	NeuralNetwork net;
	protected Model model;
	protected int[] inputKey;
	protected int startTime;
	protected int rewarded;
	protected int lastActionTime;
	protected int chosenAction;
	protected double rewardSum;
	protected double currentReward;
	protected double currentPseudoReward;
	protected double temp=1000;
	ArrayList<double[]> inputHistory;
	int windowSize=1000;
	MovingAverage avg;
	Random rand;
	
	public class MovingAverage {
	    private double [] window;
	    private int n, insert;
	    private long sum;

	    public MovingAverage(int size) {
	        window = new double[size];
	        insert = 0;
	        sum = 0;
	    }

	    public double next(double val) {
	        if (n < window.length)  n++;
	        sum -= window[insert];
	        sum += val;
	        window[insert] = val;
	        insert = (insert + 1) % window.length;
	        return (double)sum / n;
	    }
	}
	
	
	public SubTask(AbstractAction[] as, int[] size, int[] inputKey, Model m, int time) {
		subTasks = as.clone();
		this.inputKey = inputKey;
		rand=new Random();
		net = new NeuralNetwork(size.clone());
		model = m;
		startTime = time;
		inputHistory = new ArrayList<double[]>();
		rewardSum = currentReward=rewarded = 0;
		avg=new MovingAverage(windowSize);
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
		//if(this instanceof Navigate)System.out.print("Nav: ");
		// TODO Auto-generated method stub
		lastActionTime = time;
		currentReward = currentPseudoReward = 0;

		double[] input = decodeInput(rawInput);
		double[] out = net.forwardProp(input);

		double sum=0;
		double normalized[]=new double[out.length];
		
		for(int i=0;i<out.length;i++){
			normalized[i]=Math.exp(out[i]/temp);
			sum+=normalized[i];
		}
		
		double r=rand.nextDouble();
		double chanceSum=0;
		int h=-1;
		
		for(int i=0;i<out.length;i++){
			normalized[i] /= sum;
			chanceSum+=normalized[i];
			if(r<=chanceSum){
				h=i;
				break;
			}
		}		
		
		//System.out.print(out[0]+" ");
//		double max = out[0];
//		int h = 0;
//		for (int i = 1; i < out.length; i++) {
//			//System.out.print(out[i]+" ");
//			if (max < out[i]) {
//				max = out[i];
//				h = i;
//			}
//		}
//		//System.out.println("");
//		if((rand.nextDouble()>epsilon)){
//			h=rand.nextInt(out.length);
//		}
		chosenAction = h;
		if(h==-1){
			System.out.println(Arrays.toString(rawInput));
			System.out.println(Arrays.toString(normalized));
			System.out.println(Arrays.toString(out));
			System.err.println("Softmax did not return value for some reason, last option is assumed");
			chosenAction=out.length-1;
			System.exit(1);
		}
		//System.out.println(chosenAction);
		
		return subTasks[chosenAction];
	}

	public void reward(ArrayList<double[]> inHist, double[] current, double reward, boolean terminate,int time) {
		inputHistory.addAll(inHist);
		rewardSum += Math.pow(discountfactor, lastActionTime - startTime) * reward + currentReward;
		double local = reward + currentReward + currentPseudoReward;
		double[] out;
		double max=0;
		if (!terminate) {
			double[] nextExpected = this.decodeInput(current);
			max = nextExpected[0];
			for (int i = 1; i < nextExpected.length; i++) {
				if (nextExpected[i] > max) {
					max = nextExpected[i];
				}
			}
		}
		//System.out.println(this.getClass()+" "+(time-startTime)+" "+inputHistory.size());
		for (double[] in : inputHistory.subList(lastActionTime-startTime, inputHistory.size())) {
			out = net.forwardProp(decodeInput(in));
			out[chosenAction] = local;
			//System.out.println(this.getClass()+" "+local);
			if (!terminate) {
				out[chosenAction] += discountfactor * max;
			}
			net.backProp(in, out.clone());
			local = local / discountfactor;
		}
		
		if((this instanceof Root) && terminate)System.out.println("Average final reward last "+windowSize+" trials: "+this.avg.next(this.rewardSum));

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
		//System.out.println("Added history "+inputHistory.size());
		
	}
	
	public void updateDiscount(){
		epsilon += epsilonIncrement;
		epsilon = Math.min(epsilon, maxEpsilon);
		temp = .999*temp;
		temp=Math.max(temp, 1);
		
//		System.out.println(this.getClass()+ " epsilon: "+epsilon);
	}

}

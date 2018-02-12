package AI;

public class Navigate extends Behaviour {

	public Navigate(int[] size, double[] rewardWeights, int[] inputKey, String ID) {
		super(size, rewardWeights, inputKey, ID);
	}

	public double[] feedForward(double[] rawInput, int[] inputKey) {
		this.inputKey = inputKey;
		return super.feedForward(rawInput);
	}

	public void updateNetwork(State current, State next, boolean mainBehaviour, int[] inputKey, double[] rewardWeights) {
		this.inputKey = inputKey;
		this.rewardWeights=rewardWeights;
		super.updateNetwork(current, next, mainBehaviour);
	}

}

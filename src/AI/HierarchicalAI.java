package AI;

import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

import Main.Model;
import Neural.NeuralNetwork;

public class HierarchicalAI implements AI, Serializable {
	private static final long serialVersionUID = 5012192102485271035L;
	Model model;
	boolean shoot;
	double heading;
	Behaviour mainBehaviour;
	int inputs = 20;
	Behaviour[] subBehaviours = new Behaviour[2];
	int[] sizeMain = { inputs, 100, 50, subBehaviours.length };
	int[] sizeSub = { -1, 100, 50, 16 };
	double gamma = .95;
	double epsilon = .10;
	double[] input;
	double[][] activation;

	Queue<State> history;

	public HierarchicalAI(Model m) {
		model=m;
		history = new LinkedList<State>();
	}

	private void constructBehaviours(){
		double[] rewardWeights=new double[]{20,0,0,2,2,2,-50,-2};
		int[] inputKey=new int[inputs];
		for(int i=0;i<inputs;i++){
			inputKey[i]=i;
		}
		mainBehaviour=new Behaviour(sizeMain,rewardWeights,inputKey,"Main");
		
		/*Gets reward for reaching a key, it also gets punished for being damaged.
		 * We hope by giving it the enemies in its vicinity it will learn to dodge those
		 * while still reaching its goal.
		 */
		
		rewardWeights=new double[] {0,0,1,0,0,0,-50,-2};
		subBehaviours[0]=new Behaviour(sizeSub,rewardWeights,inputKey,"Keyfinder");
		
		/*Gets reward for reaching a door, it also gets punished for being damaged.
		 * We hope by giving it the enemies in its vicinity it will learn to dodge those
		 * while still reaching its goal. Its get the door reached reward regardless of if it has a key or not
		 * The main behaviour should learn not to approach a door while not carrying a key.
		 */
		rewardWeights=new double[] {0,1,0,0,0,0,-50,-2};
		subBehaviours[1]=new Behaviour(sizeSub,rewardWeights,inputKey,"Door");
	}
	
	public void update(){
		if(history.size()>1 && !history.peek().flyingBullet && !((State)((LinkedList)history).get(2)).flyingBullet){
			State current=history.poll();
			State next=(State)((LinkedList)history).get(2);
			mainBehaviour.updateNetwork(current, next);
			for(int i=0;i<subBehaviours.length;i++){
				subBehaviours[i].updateNetwork(current, next);
			}
			update();
		};
	}
	
	public void forceUpdateAll(){
		while(history.size()>0){
			State current=history.poll();
			State next=history.peek();
			mainBehaviour.updateNetwork(current, next);
			for(int i=0;i<subBehaviours.length;i++){
				subBehaviours[i].updateNetwork(current, next);
			}
		}
	}

	@Override
	public double getHeading() {
		// TODO Auto-generated method stub
		return heading;
	}

	@Override
	public boolean shoot() {
		// TODO Auto-generated method stub
		return shoot;
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub

	}

}

package AI;

import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

import Assets.Enemy;
import Assets.Player;
import Main.Map;
import Main.Model;
import Neural.NeuralNetwork;
import PathFinder.ResultTuple;
import PathFinder.ShortestPathFinder;
import mapTiles.*;

public class HierarchicalAI implements AI, Serializable {
	private static final long serialVersionUID = 5012192102485271035L;
	Model model;
	boolean shoot;
	double heading;
	Behaviour mainBehaviour;
	int inputs = 16;
	int inputsCombat = 49;
	Behaviour[] subBehaviours = new Behaviour[4];
	int[] sizeMain = { inputs, 10, 8, subBehaviours.length };
	int[] sizeSubDef = { -1, 30, 15, 8 };
	int[] sizeSubCombat = { -1, 35, 20, 16 };

	protected double temp=1000;
	
	double gamma = .95;
	double epsilon = .80;
	double maxEpsilon = .95;
	double epsilonIncrement = 0.005;
	double[] input;
	double[][] activation;
	Random r;
	ShortestPathFinder path;
	boolean learn;

	Queue<State> history;

	public HierarchicalAI(Model m) {
		learn = true;
		model = m;
		history = new LinkedList<State>();
		m.setHistory(history);
		path = new ShortestPathFinder(m.getLevelMap());
		r = new Random();
		constructBehaviours();
	}

	public static int[] concat(int[] inputKey, int[] localMap) {
		int[] result = Arrays.copyOf(inputKey, inputKey.length + localMap.length);
		System.arraycopy(localMap, 0, result, inputKey.length, localMap.length);
		return result;
	}

	private void constructBehaviours() {
		
		int[] localMap=new int[25];
		for(int i=23;i<48;i++){
			localMap[i-23]=i;
		}
		
		double[] rewardWeights = new double[] { 30, 0, 0, 5, 2, 2, -50, -2,0,0,0 };
		int[] inputKey = new int[inputs];
		for (int i = 0; i < 15; i++) {
			inputKey[i] = i;
		}
		inputKey[15]=48;
		mainBehaviour = new Behaviour(sizeMain, rewardWeights, inputKey, "Main");

		/*
		 * Gets reward for reaching a key, it also gets punished for being
		 * damaged. We hope when giving it the enemies in its vicinity it will
		 * learn to dodge those while still reaching its goal.
		 */

		rewardWeights = new double[] { 0, 0, 1, 0, 0, 0, -50, -2,1,0,0 };
		inputKey = new int[] { 0, 1, 2, 3, 4 };
		inputKey=concat(inputKey,localMap);
		subBehaviours[0] = new Behaviour(sizeSubDef, rewardWeights, inputKey, "Keyfinder");

		/*
		 * Gets reward for reaching a door, it also gets punished for being
		 * damaged. We hope when giving it the enemies in its vicinity it will
		 * learn to dodge those while still reaching its goal. Its get the door
		 * reached reward regardless of if it has a key or not The main
		 * behaviour should learn not to approach a door while not carrying a
		 * key.
		 */
		rewardWeights = new double[] { 0, 1, 0, 0, 0, 0, -50, -2,0,1,0 };

		inputKey = new int[] { 5, 6, 7, 8, 9 };
		inputKey=concat(inputKey,localMap);
		subBehaviours[1] = new Behaviour(sizeSubDef, rewardWeights, inputKey, "Door");

		/*
		 * Gets reward for reaching a exit, it also gets punished for being
		 * damaged. We hope when giving it the enemies in its vicinity it will
		 * learn to dodge those while still reaching its goal.
		 */
		rewardWeights = new double[] { 30, 0, 0, 0, 0, 0, -50, -2,0,0,1 };

		inputKey = new int[] { 10, 11, 12, 13, 14 };
		inputKey=concat(inputKey,localMap);
		subBehaviours[2] = new Behaviour(sizeSubDef, rewardWeights, inputKey, "Exit");

		rewardWeights = new double[] { 0, 0, 0, 0, 0, 5, -50, -2,0,0,0 };

		inputKey = new int[] { 15,16,17,18,19,20,21,22 };
		inputKey=concat(inputKey,localMap);
		subBehaviours[3] = new Behaviour(sizeSubCombat, rewardWeights, inputKey, "Combat");
	}

	public void determineAction(int time, boolean b) {

		this.update();

		double[] input = determineInput();

//		if (!learn) {
//			for (int i = 0; i < input.length; i++) {
//				System.out.print(input[i] + "  ");
//			}
//			System.out.println();
//		}
		double[] out = mainBehaviour.feedForward(input);

		double sum=0;
		double normalized[]=new double[out.length];
		for(int i=0;i<out.length;i++){
			normalized[i]=Math.exp(out[i]/temp);
			sum+=normalized[i];
		}
		
		double rand=r.nextDouble();
		double chanceSum=0;
		int behaviour=-1;
		
		for(int i=0;i<out.length;i++){
			normalized[i] /= sum;
			chanceSum+=normalized[i];
			if(rand<=chanceSum){
				behaviour=i;
				break;
			}
		}		
		
		
		
//		double max = output[0];
//		int behaviour = 0;
//		for (int i = 1; i < output.length; i++) {
//			if (output[i] > max) {
//				max = output[i];
//				behaviour = i;
//			}
//		}
//		if (learn && r.nextDouble() > .5) {
//			behaviour = r.nextInt(subBehaviours.length);
//		}
//		// if (!learn)
//		System.out.println("behaviour: " + subBehaviours[behaviour].ID);

		out = subBehaviours[behaviour].feedForward(input);
		

		sum=0;
		normalized=new double[out.length];
		for(int i=0;i<out.length;i++){
			normalized[i]=Math.exp(out[i]/temp);
			sum+=normalized[i];
		}
		
		chanceSum=0;
		int action=-1;
		
		for(int i=0;i<out.length;i++){
			normalized[i] /= sum;
			chanceSum+=normalized[i];
			if(rand<=chanceSum){
				action=i;
				break;
			}
		}	
		
		
//		int action = 0;
//		max = output[0];
//		for (int i = 1; i < output.length; i++) {
//			if (output[i] > max) {
//				max = output[i];
//				action = i;
//			}
//		}
//		// System.out.println("Action: " + action + " " + max);
//		if (learn && r.nextDouble() > .7) {
//			action = r.nextInt(8);
//		}
		//
		// if (!learn)
		// System.out.println("Action: " + action + " " + max);

		history.add(new State(time, input, action, behaviour));

		heading = (action % 8) * Math.PI / 4;
		shoot = action >= 8;

	}

	/**
	 * Constructs and returns the global input for the neural networks.
	 */
	public double[] determineInput() {
		/*
		 * - Closeness is 1/distance to player, if none reachable infinite
		 * distance is assumed hence closeness of 0. - Closeness of nearest key
		 * - 4 variables for Closeness of key to adjacent tiles - Closeness of
		 * nearest door - 4 variables for Closeness of door to adjacent tiles -
		 * Closeness of nearest exit - 4 variables for Closeness of exit to
		 * adjacent tiles
		 */

		double[] input = new double[inputsCombat];

		Door nearestDoor = null;
		Key nearestKey = null;
		Exit nearestExit = null;
		double distanceKey = Double.MAX_VALUE;
		double distanceDoor = Double.MAX_VALUE;
		double distanceExit = Double.MAX_VALUE;
		Map m = model.getLevelMap();
		Tile t;
		Player p = model.getPlayer();
		for (int x = 0; x < m.getSize(); x++) {
			for (int y = 0; y < m.getSize(); y++) {
				t = m.getTile(x, y);

				if (t.reachable()) {
					if (t instanceof Key) {
						ResultTuple res = path.findPath(p.getX(), p.getY(), t.getX(), t.getY(), .95);
						if (res.distance < distanceKey) {
							distanceKey = res.distance;
							nearestKey = (Key) t;
						}
					} else if (t instanceof Door && t.getSolid()) {
						ResultTuple res = path.findPath(p.getX(), p.getY(), t.getX(), t.getY(), .95);
						if (res.distance < distanceDoor) {
							distanceDoor = res.distance;
							nearestDoor = (Door) t;
						}
					} else if (t instanceof Exit) {
						ResultTuple res = path.findPath(p.getX(), p.getY(), t.getX(), t.getY(), .95);
						if (res.distance < distanceExit) {
							distanceExit = res.distance;
							nearestExit = (Exit) t;
						}
					}
				}
			}
		}
		if (nearestKey == null) {
			for (int i = 0; i < 5; i++) {
				input[i] = -1;
			}
		} else {
			input[0] = 1.0 / (1 + distanceKey);
			input[1] = 1.0
					/ (1 + path.findPath(p.getX() + 1, p.getY(), nearestKey.getX(), nearestKey.getY(), .95).distance);
			input[2] = 1.0
					/ (1 + path.findPath(p.getX() - 1, p.getY(), nearestKey.getX(), nearestKey.getY(), .95).distance);
			input[3] = 1.0
					/ (1 + path.findPath(p.getX(), p.getY() + 1, nearestKey.getX(), nearestKey.getY(), .95).distance);
			input[4] = 1.0
					/ (1 + path.findPath(p.getX(), p.getY() - 1, nearestKey.getX(), nearestKey.getY(), .95).distance);
		}

		if (nearestDoor == null) {
			for (int i = 5; i < 10; i++) {
				input[i] = -1;
			}
		} else {
			input[5] = 1.0 / (1 + distanceDoor);
			input[6] = 1.0
					/ (1 + path.findPath(p.getX() + 1, p.getY(), nearestDoor.getX(), nearestDoor.getY(), .95).distance);
			input[7] = 1.0
					/ (1 + path.findPath(p.getX() - 1, p.getY(), nearestDoor.getX(), nearestDoor.getY(), .95).distance);
			input[8] = 1.0
					/ (1 + path.findPath(p.getX(), p.getY() + 1, nearestDoor.getX(), nearestDoor.getY(), .95).distance);
			input[9] = 1.0
					/ (1 + path.findPath(p.getX(), p.getY() - 1, nearestDoor.getX(), nearestDoor.getY(), .95).distance);
		}

		if (nearestExit == null) {
			for (int i = 10; i < 15; i++) {
				input[i] = -1;
			}
		} else {
			input[10] = 1.0 / (1 + distanceExit);
			input[11] = 1.0
					/ (1 + path.findPath(p.getX() + 1, p.getY(), nearestExit.getX(), nearestExit.getY(), .95).distance);
			input[12] = 1.0
					/ (1 + path.findPath(p.getX() - 1, p.getY(), nearestExit.getX(), nearestExit.getY(), .95).distance);
			input[13] = 1.0
					/ (1 + path.findPath(p.getX(), p.getY() + 1, nearestExit.getX(), nearestExit.getY(), .95).distance);
			input[14] = 1.0
					/ (1 + path.findPath(p.getX(), p.getY() - 1, nearestExit.getX(), nearestExit.getY(), .95).distance);
		}

		for (int i = 0; i < 8; i++) {
			input[15 + i] = Hits(i * Math.PI / 4);
		}
		int count = 0;

		for (int x = -2; x < 3; x++) {
			for (int y = -2; y < 3; y++) {
				int fX = (int) p.getX() - x;
				int fY = (int) p.getY() - y;
				if (fX > 0 && fY > 0 && fX < model.getLevelMap().getSize() && fY < model.getLevelMap().getSize()
						&& model.getEnemyMap()[(int) p.getX() - x][(int) p.getY() - y]) {
					input[23 + count] = 1;
				} else {
					input[23 + count] = 0;
				}
				count++;

			}
		}

		input[48] = model.getPlayer().getHealth();

//		for (double i : input) {
//			System.out.print(i + ", ");
//		}
//		System.out.println();
		return input;

	}

	private int Hits(double heading) {
		int count = 0;

		Player p = model.getPlayer();
		double minDist = Double.MAX_VALUE;

		for (Tile ts[] : model.getLevelMap().getTileMap()) {
			for (Tile t : ts) {

				double x = t.getX();
				double y = t.getY();
				double x1 = model.getPlayer().getX();
				double y1 = model.getPlayer().getY();
				double x2 = x1 + (1000 * Math.cos(heading));
				double y2 = y1 - (1000 * Math.sin(heading));

				double ch = (y1 - y2) * x + (x2 - x1) * y + (x1 * y2 - x2 * y1);
				double del = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
				double distance = ch / del;

				double absHeading = -Math.atan2(t.getY() - model.getPlayer().getY(),
						t.getX() - model.getPlayer().getX());
				if (absHeading < 0)
					absHeading += 2 * Math.PI;

				double relativeHeading = Math.abs(absHeading - heading);

				if (relativeHeading < Math.PI / 2 && t.getSolid() && distance < 1.5) {
					if (model.distance(t.getX(), p.getX(), t.getY(), p.getY()) < minDist) {
						minDist = model.distance(t.getX(), p.getX(), t.getY(), p.getY());
					}
				}
			}
		}

		for (Enemy e : model.getEnemyList()) {
			double x = e.getX();
			double y = e.getY();
			double x1 = model.getPlayer().getX();
			double y1 = model.getPlayer().getY();
			double x2 = x1 + (1000 * Math.cos(heading));
			double y2 = y1 - (1000 * Math.sin(heading));

			double ch = (y1 - y2) * x + (x2 - x1) * y + (x1 * y2 - x2 * y1);
			double del = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
			double distance = ch / del;

			double absHeading = -Math.atan2(e.getY() - model.getPlayer().getY(), e.getX() - model.getPlayer().getX());
			if (absHeading < 0)
				absHeading += 2 * Math.PI;

			double relativeHeading = Math.abs(absHeading - heading);

			if (relativeHeading < Math.PI / 2 && distance < 1.5
					&& model.distance(e.getX(), p.getX(), e.getY(), p.getY()) < minDist)
				count++;

		}

		return count;
	}

	/**
	 * Makes sure all behaviours learn from previous states for which the final
	 * reward and following the state are known.
	 */
	public void update() {
		
		double[] in=determineInput();
		
		if (!learn)
			return;
		if (history.size() > 1 && !history.peek().flyingBullet) {
			//System.out.println("learned");
			State current = history.poll();
			State next = history.peek();
			next.rewards[8]=in[0]*5;
			next.rewards[9]=in[5]*5;
			next.rewards[10]=in[10]*5;
			
			
			
			
			mainBehaviour.updateNetwork(current, next, true);
			for (int i = 0; i < subBehaviours.length; i++) {
				if(subBehaviours[i].size[subBehaviours[i].size.length-1]>current.action)
					subBehaviours[i].updateNetwork(current, next, false);
			}
			update();
		}
		;
	}

	/**
	 * Should only be used when game is over, the behaviours learn from all yet
	 * to be processed states whether the state still has a connected bullet
	 * flying or not.
	 */
	public void forceUpdateAll() {
		if (!learn)
			return;
		while (history.size() > 0) {
			State current = history.poll();
			State next = history.peek();
			mainBehaviour.updateNetwork(current, next, true);
			for (int i = 0; i < subBehaviours.length; i++) {
				subBehaviours[i].updateNetwork(current, next, false);
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
	public void reset(boolean learn, int time) {
		this.forceUpdateAll();
		this.learn = learn;
		System.out.println(epsilon);
		if (learn) {
			epsilon += epsilonIncrement;
			epsilon = Math.min(epsilon, maxEpsilon);
			temp = .999*temp;
			temp=Math.max(temp, 1);
		}
	}

}

package AI;

import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

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
	int inputs = 15;
	Behaviour[] subBehaviours = new Behaviour[2];
	int[] sizeMain = { inputs, 100, 50, subBehaviours.length };
	int[] sizeSub = { -1, 100, 50, 8 };
	double gamma = .95;
	double epsilon = .10;
	double[] input;
	double[][] activation;
	ShortestPathFinder path;

	Queue<State> history;

	public HierarchicalAI(Model m) {
		model = m;
		history = new LinkedList<State>();
		m.setHistory(history);
		path = new ShortestPathFinder(m.getLevelMap());
	}

	private void constructBehaviours() {
		double[] rewardWeights = new double[] { 20, 0, 0, 2, 2, 2, -50, -2 };
		int[] inputKey = new int[inputs];
		for (int i = 0; i < inputs; i++) {
			inputKey[i] = i;
		}
		mainBehaviour = new Behaviour(sizeMain, rewardWeights, inputKey, "Main");

		/*
		 * Gets reward for reaching a key, it also gets punished for being
		 * damaged. We hope when giving it the enemies in its vicinity it will
		 * learn to dodge those while still reaching its goal.
		 */

		rewardWeights = new double[] { 0, 0, 1, 0, 0, 0, -50, -2 };
		inputKey = new int[] { 0, 1, 2, 3, 4 };
		subBehaviours[0] = new Behaviour(sizeSub, rewardWeights, inputKey, "Keyfinder");

		/*
		 * Gets reward for reaching a door, it also gets punished for being
		 * damaged. We hope when giving it the enemies in its vicinity it will
		 * learn to dodge those while still reaching its goal. Its get the door
		 * reached reward regardless of if it has a key or not The main
		 * behaviour should learn not to approach a door while not carrying a
		 * key.
		 */
		rewardWeights = new double[] { 0, 1, 0, 0, 0, 0, -50, -2 };

		inputKey = new int[] { 5, 6, 7, 8, 9 };
		subBehaviours[1] = new Behaviour(sizeSub, rewardWeights, inputKey, "Door");

		/*
		 * Gets reward for reaching a exit, it also gets punished for being
		 * damaged. We hope when giving it the enemies in its vicinity it will
		 * learn to dodge those while still reaching its goal.
		 */
		rewardWeights = new double[] { 0, 1, 0, 0, 0, 0, -50, -2 };

		inputKey = new int[] { 10, 11, 12, 13, 14 };
		subBehaviours[1] = new Behaviour(sizeSub, rewardWeights, inputKey, "Exit");
	}

	public void determineAction(int time) {
		double[] input = determineInput();
		double[][] activation = mainBehaviour.feedForward(input);
		double[] output = activation[sizeMain.length];
		double max = output[0];
		int behaviour = 0;
		for (int i = 1; i < output.length; i++) {
			if (output[i] > max){
				max = output[i];
				behaviour=i;
			}
		}

		activation = subBehaviours[behaviour].feedForward(input);
		output = activation[sizeSub.length];
		int action = 0;
		max=output[0];
		for (int i = 1; i < output.length; i++) {
			if (output[i] > max){
				max = output[i];
				action=i;
			}
		}
		
		history.add(new State(time,action>=8,input,action,behaviour));
		
		heading=(action%8)*Math.PI/4;
		shoot=action>=8;
		
	}

	public double[] determineInput() {
		/*
		 * - Closeness is 1/distance to player, if none reachable infinite
		 * distance is assumed hence closeness of 0. - Closeness of nearest key
		 * - 4 variables for Closeness of key to adjacent tiles - Closeness of
		 * nearest door - 4 variables for Closeness of door to adjacent tiles -
		 * Closeness of nearest exit - 4 variables for Closeness of exit to
		 * adjacent tiles
		 */

		double[] input = new double[inputs];

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
					} else if (t instanceof Door) {
						ResultTuple res = path.findPath(p.getX(), p.getY(), t.getX(), t.getY(), .95);
						if (res.distance < distanceDoor) {
							distanceDoor = res.distance;
							nearestDoor = (Door) t;
						}
					} else if (t instanceof Door) {
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
				input[i] = 0;
			}
		} else {
			input[0] = 1.0 / distanceKey;
			input[1] = 1.0 / path.findPath(p.getX() + 1, p.getY(), nearestKey.getX(), nearestKey.getY(), .95).distance;
			input[2] = 1.0 / path.findPath(p.getX() - 1, p.getY(), nearestKey.getX(), nearestKey.getY(), .95).distance;
			input[3] = 1.0 / path.findPath(p.getX(), p.getY() + 1, nearestKey.getX(), nearestKey.getY(), .95).distance;
			input[4] = 1.0 / path.findPath(p.getX(), p.getY() - 1, nearestKey.getX(), nearestKey.getY(), .95).distance;
		}

		if (nearestDoor == null) {
			for (int i = 5; i < 10; i++) {
				input[i] = 0;
			}
		} else {
			input[5] = 1.0 / distanceDoor;
			input[6] = 1.0
					/ path.findPath(p.getX() + 1, p.getY(), nearestDoor.getX(), nearestDoor.getY(), .95).distance;
			input[7] = 1.0
					/ path.findPath(p.getX() - 1, p.getY(), nearestDoor.getX(), nearestDoor.getY(), .95).distance;
			input[8] = 1.0
					/ path.findPath(p.getX(), p.getY() + 1, nearestDoor.getX(), nearestDoor.getY(), .95).distance;
			input[9] = 1.0
					/ path.findPath(p.getX(), p.getY() - 1, nearestDoor.getX(), nearestDoor.getY(), .95).distance;
		}

		if (nearestExit == null) {
			for (int i = 10; i < 15; i++) {
				input[i] = 0;
			}
		} else {
			input[10] = 1.0 / distanceExit;
			input[11] = 1.0
					/ path.findPath(p.getX() + 1, p.getY(), nearestExit.getX(), nearestExit.getY(), .95).distance;
			input[12] = 1.0
					/ path.findPath(p.getX() - 1, p.getY(), nearestExit.getX(), nearestExit.getY(), .95).distance;
			input[13] = 1.0
					/ path.findPath(p.getX(), p.getY() + 1, nearestExit.getX(), nearestExit.getY(), .95).distance;
			input[14] = 1.0
					/ path.findPath(p.getX(), p.getY() - 1, nearestExit.getX(), nearestExit.getY(), .95).distance;
		}

		return input;

	}

	public void update() {
		if (history.size() > 1 && !history.peek().flyingBullet
				&& !((State) ((LinkedList) history).get(2)).flyingBullet) {
			State current = history.poll();
			State next = (State) ((LinkedList) history).get(2);
			mainBehaviour.updateNetwork(current, next, true);
			for (int i = 0; i < subBehaviours.length; i++) {
				subBehaviours[i].updateNetwork(current, next, false);
			}
			update();
		}
		;
	}

	public void forceUpdateAll() {
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
	public void reset() {
		// TODO Auto-generated method stub

	}

}

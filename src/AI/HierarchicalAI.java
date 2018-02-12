package AI;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
	int inputs = 51;
	int inputsCombat = 49;
	Behaviour[] subBehaviours = new Behaviour[4];
	int[] sizeMain = { 6, 50, subBehaviours.length };
	int[] sizeSubDef = { -1, 300, 16 };
	int[] sizeSubCombat = { -1, 150, 16 };

	int[] navKey;
	int[] navDoor;
	int[] navExit;
	double[] keyReward;
	double[] doorReward;
	double[] exitReward;

	protected double temp = .5;
	protected double tempMain = .1;
	double decay = .97;
	double decayMain = .97;
	double mintemp = 0.00000001;

	double discountFactor = .9;
	double epsilon = .80;
	double maxEpsilon = .95;
	double epsilonIncrement = 0.005;
	double[] input;
	double[][] activation;
	Random r;
	ShortestPathFinder path;
	boolean learn;
	double distanceKey = -1;
	double distanceDoor = -1;
	double distanceExit = -1;

	double[] previous = new double[3];

	Queue<State> history;

	public HierarchicalAI(Model m) {
		learn = true;
		model = m;
		history = new LinkedList<State>();
		m.setHistory(history);
		path = new ShortestPathFinder(m.getLevelMap(), m);
		r = new Random();
		constructBehaviours();
	}

	public static int[] concat(int[] inputKey, int[] localMap) {
		int[] result = Arrays.copyOf(inputKey, inputKey.length + localMap.length);
		System.arraycopy(localMap, 0, result, inputKey.length, localMap.length);
		return result;
	}

	private void constructBehaviours() {

		double[] rewardWeights = new double[] { 100, 0, 5, 5, 2, 1, -100, -2, 0, 0, 0, 0, -5};
		int[] inputKey = new int[] { 0, 5, 10, 15, 49, 50 };
		mainBehaviour = new Behaviour(sizeMain, rewardWeights, inputKey, "Main");

		/*
		 * Gets reward for reaching a key, it also gets punished for being
		 * damaged. We hope when giving it the enemies in its vicinity it will
		 * learn to dodge those while still reaching its goal.
		 */

		keyReward = new double[] { 0, 0, 5, 0, 0, 0, -50, -5, -1, 0, 0, -5, 0 };
		navKey = new int[] { 1, 2, 3, 4, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43,
				44, 45, 46, 47, 48, 50 };
		subBehaviours[0] = new Navigate(sizeSubDef, keyReward, navKey, "Keyfinder");
		subBehaviours[1] = subBehaviours[0];
		subBehaviours[2] = subBehaviours[0];

		/*
		 * Gets reward for reaching a door, it also gets punished for being
		 * damaged. We hope when giving it the enemies in its vicinity it will
		 * learn to dodge those while still reaching its goal. Its get the door
		 * reached reward regardless of if it has a key or not The main
		 * behaviour should learn not to approach a door while not carrying a
		 * key.
		 */
		doorReward = new double[] { 0, 5, 0, 0, 0, 0, -50, -5, 0, -1, 0, -5, 0 };

		navDoor = new int[] { 6, 7, 8, 9, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42,
				43, 44, 45, 46, 47, 48, 50 };

		/*
		 * Gets reward for reaching a exit, it also gets punished for being
		 * damaged. We hope when giving it the enemies in its vicinity it will
		 * learn to dodge those while still reaching its goal.
		 */
		exitReward = new double[] { 5, 0, 0, 0, 0, 0, -50, -5, 0, 0, -1, -5, 0 };

		navExit = new int[] { 11, 12, 13, 14, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41,
				42, 43, 44, 45, 46, 47, 48, 50 };

		rewardWeights = new double[] { 0, 0, 0, 0, 0, 5, -50, -5, 0, 0, 0, 0, -1 };
		inputKey = new int[] { 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37,
				38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50 };
		subBehaviours[3] = new Behaviour(sizeSubCombat, rewardWeights, inputKey, "Combat");
	}

	public void determineAction(int time, boolean b) {

		this.update();

		if (!b) {
			this.temp = 0.01;
			this.tempMain = 0.01;
		}

		double[] input = determineInput();

		// if (!learn) {
		// for (int i = 0; i < input.length; i++) {
		// System.out.print(input[i] + " ");
		// }
		// System.out.println();
		// }
		double[] out = mainBehaviour.feedForward(input);

		double max = Double.NEGATIVE_INFINITY;
		int behaviour = 0;
		for (int i = 0; i < out.length; i++) {
			if (out[i] > max) {
				max = out[i];
				behaviour = i;
			}
		}
		double sum = 0;
		double normalized[] = new double[out.length];
		if (b) {
			for (int i = 0; i < out.length; i++) {
				normalized[i] = Math.exp((out[i] - max) / tempMain);
				sum += normalized[i];
			}

			double rand = r.nextDouble();
			double chanceSum = 0;

			for (int i = 0; i < out.length; i++) {
				normalized[i] /= sum;
				chanceSum += normalized[i];
				if (rand <= chanceSum) {
					behaviour = i;
					break;
				}
			}
		}

		// double max = output[0];
		// int behaviour = 0;
		// for (int i = 1; i < output.length; i++) {
		// if (output[i] > max) {
		// max = output[i];
		// behaviour = i;
		// }
		// }
		// if (learn && r.nextDouble() > .5) {
		// behaviour = r.nextInt(subBehaviours.length);
		// }
		// // if (!learn)
		// System.out.println("behaviour: " + subBehaviours[behaviour].ID);
		boolean reachable = true;
		if (subBehaviours[behaviour] instanceof Navigate) {
			int[] inputKey = null;
			if (behaviour == 0) {
				inputKey = navKey;
				if (input[0] == -1)
					reachable = false;
			} else if (behaviour == 1) {
				inputKey = navDoor;
				if (input[5] == -1 || input[15] <= 0)
					reachable = false;
			} else {
				inputKey = navExit;
				if (input[10] == -1)
					reachable = false;
			}
			out = ((Navigate) subBehaviours[behaviour]).feedForward(input, inputKey);
		} else {
			out = subBehaviours[behaviour].feedForward(input);
			if (input[49] == 1)
				reachable = false;
		}

		int action = 0;
		max = out[0];
		for (int i = 1; i < out.length; i++) {
			if (out[i] >= max) {
				max = out[i];
				action = i;
			}
		}
		if (b) {
			sum = 0;
			normalized = new double[out.length];
			for (int i = 0; i < out.length; i++) {
				normalized[i] = Math.exp((out[i] - max) / temp);
				sum += normalized[i];
			}

			double rand = r.nextDouble();
			double chanceSum = 0;

			for (int i = 0; i < out.length; i++) {
				normalized[i] /= sum;
				chanceSum += normalized[i];
				if (rand <= chanceSum) {
					action = i;
					break;
				}
			}
		}
		// int action = 0;
		// max = output[0];
		// for (int i = 1; i < output.length; i++) {
		// if (output[i] > max) {
		// max = output[i];
		// action = i;
		// }
		// }
		// // System.out.println("Action: " + action + " " + max);
		// if (learn && r.nextDouble() > .7) {
		// action = r.nextInt(8);
		// }
		//
		// if (!learn)
		// System.out.println("Action: " + action + " " + max);
		State s = new State(time, input, action, behaviour);
		s.distanceDoor = distanceDoor;
		s.distanceExit = distanceExit;
		s.distanceKey = distanceKey;
		if (!reachable) {
			s.targetNotReachable();
			// System.out.print("Not reachable! ");
		}
		history.add(s);

		// System.out.print(behaviour + ": ");
		// if (behaviour == 0) {
		// System.out.println(input[0] + " getting key");
		// } else if (behaviour == 1) {
		// System.out.println("goto door");
		// } else if (behaviour == 2) {
		// System.out.println("goto exit");
		// } else {
		// System.out.println("combat");
		// }
		// System.out.println(Arrays.toString(out));
		// System.out.println((action));

		heading = (action % 8) * Math.PI / 4;
		shoot = action >= 8;

	}

	/**
	 * Constructs and returns the global input for the neural networks.
	 */
	public double[] determineInput() {
		int inputs = 51;
		double[] input = new double[inputs];

		Door nearestDoor = null;
		Key nearestKey = null;
		Exit nearestExit = null;
		distanceKey = -1;
		distanceDoor = -1;
		distanceExit = -1;
		Map m = model.getLevelMap();
		Tile t;
		Player p = model.getPlayer();
		for (int x = 0; x < m.getSize(); x++) {
			for (int y = 0; y < m.getSize(); y++) {
				t = m.getTile(x, y);

				if (t.reachable()) {
					if (t instanceof Key) {
						ResultTuple r = path.findPath(p.getX(), p.getY(), t.getX(), t.getY(), .95);
						if ((r.distance < distanceKey && r.distance != -1) || distanceKey == -1) {
							distanceKey = r.distance;
							nearestKey = (Key) t;
						}
					} else if (t instanceof Door && t.getSolid() && ((Door) t).entry) {
						ResultTuple r = path.findPath(p.getX(), p.getY(), t.getX(), t.getY(), .95);
						if ((r.distance < distanceDoor && r.distance != -1) || distanceDoor == -1) {
							distanceDoor = r.distance;
							nearestDoor = (Door) t;
						}
					} else if (t instanceof Exit) {
						ResultTuple r = path.findPath(p.getX(), p.getY(), t.getX(), t.getY(), .95);
						if ((r.distance < distanceExit && r.distance != -1) || distanceExit == -1) {
							distanceExit = r.distance;
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
			input[0] = distanceKey;
			// System.out.print(model.getLevelMap().getTile((int) p.getX() + 1,
			// (int) p.getY()).getClass() + " ");
			input[1] = path.findPath(p.getX() + 1, p.getY(), nearestKey.getX(), nearestKey.getY(), .95).distance;
			// System.out.print(input[1]+", ");
			// System.out.print(model.getLevelMap().getTile((int) p.getX() - 1,
			// (int) p.getY()).getClass() + " ");
			input[2] = path.findPath(p.getX() - 1, p.getY(), nearestKey.getX(), nearestKey.getY(), .95).distance;
			// System.out.print(input[2]+", ");
			// System.out.print(model.getLevelMap().getTile((int) p.getX(),
			// (int) p.getY() + 1).getClass() + " " +(int) p.getX()+"
			// "+(int)(p.getY() + 1)+", ");
			input[3] = path.findPath(p.getX(), p.getY() + 1, nearestKey.getX(), nearestKey.getY(), .95).distance;
			// System.out.print(input[3]+", ");
			// System.out.print(model.getLevelMap().getTile((int) p.getX(),
			// (int) p.getY() - 1).getClass() + " ");
			input[4] = path.findPath(p.getX(), p.getY() - 1, nearestKey.getX(), nearestKey.getY(), .95).distance;
			// System.out.println(input[4]+", ");
			// System.out.println(input[1] + ", " + input[2] + ", " + input[3] +
			// ", " + input[4]);

			double min = Double.POSITIVE_INFINITY, max = Double.NEGATIVE_INFINITY;
			for (int i = 1; i < 5; i++) {
				if (input[i] == -1)
					continue;
				if (input[i] < min)
					min = input[i];
				if (input[i] > max)
					max = input[i];
			}
			if (min == max) {
				min = 0;
			}
			for (int i = 1; i < 5; i++) {
				if (input[i] == -1) {
					continue;
				}
				input[i] = 1 - ((input[i] - min) / (max - min));
			}
		}

		if (nearestDoor == null) {
			for (int i = 5; i < 10; i++) {
				input[i] = -1;
			}
		} else {
			input[5] = distanceDoor;
			input[6] = path.findPath(p.getX() + 1, p.getY(), nearestDoor.getX(), nearestDoor.getY(), .95).distance;
			input[7] = path.findPath(p.getX() - 1, p.getY(), nearestDoor.getX(), nearestDoor.getY(), .95).distance;
			input[8] = path.findPath(p.getX(), p.getY() + 1, nearestDoor.getX(), nearestDoor.getY(), .95).distance;
			input[9] = path.findPath(p.getX(), p.getY() - 1, nearestDoor.getX(), nearestDoor.getY(), .95).distance;

			double min = Double.POSITIVE_INFINITY, max = Double.NEGATIVE_INFINITY;
			for (int i = 6; i < 10; i++) {
				if (input[i] == -1)
					continue;
				if (input[i] < min)
					min = input[i];
				if (input[i] > max)
					max = input[i];
			}
			if (min == max) {
				min = 0;
			}
			for (int i = 6; i < 10; i++) {
				if (input[i] == -1) {
					continue;
				}
				input[i] = 1 - ((input[i] - min) / (max - min));
			}
		}

		if (nearestExit == null) {
			for (int i = 10; i < 15; i++) {
				input[i] = -1;
			}
		} else {
			input[10] = distanceExit;
			input[11] = path.findPath(p.getX() + 1, p.getY(), nearestExit.getX(), nearestExit.getY(), .95).distance;
			input[12] = path.findPath(p.getX() - 1, p.getY(), nearestExit.getX(), nearestExit.getY(), .95).distance;
			input[13] = path.findPath(p.getX(), p.getY() + 1, nearestExit.getX(), nearestExit.getY(), .95).distance;
			input[14] = path.findPath(p.getX(), p.getY() - 1, nearestExit.getX(), nearestExit.getY(), .95).distance;
			double min = Double.POSITIVE_INFINITY, max = Double.NEGATIVE_INFINITY;
			for (int i = 11; i < 15; i++) {
				if (input[i] == -1)
					continue;
				if (input[i] < min)
					min = input[i];
				if (input[i] > max)
					max = input[i];
			}
			if (min == max) {
				min = 0;
			}
			for (int i = 11; i < 15; i++) {
				if (input[i] == -1) {
					continue;
				}
				input[i] = 1 - ((input[i] - min) / (max - min));
			}
		}
		double min = Double.POSITIVE_INFINITY;
		double max = Double.NEGATIVE_INFINITY;
		for (int i = 0; i <= 10; i = i + 5) {
			if (input[i] != -1) {
				if (input[i] < min)
					min = input[i];
				if (input[i] > max)
					max = input[i];
			}
		}
		for (int i = 0; i <= 10; i = i + 5) {
			if (input[i] != -1) {
				if (max != min) {
					input[i] = 1 - ((input[i] - min) / (max - min));
				} else {
					input[i] = 1;
				}
			}
		}

		int sum = 0;
		input[15] = model.getPlayer().getKeys();
		for (int i = 0; i < 8; i++) {
			input[16 + i] = Hits(i * Math.PI / 4);
			sum += input[16 + i];
		}
		int count = 0;
		for (int x = -2; x < 3; x++) {
			for (int y = -2; y < 3; y++) {
				int fX = (int) p.getX() - x;
				int fY = (int) p.getY() - y;
				if (fX > 0 && fY > 0 && fX < model.getLevelMap().getSize() && fY < model.getLevelMap().getSize()
						&& model.getEnemyMap()[(int) p.getX() - x][(int) p.getY() - y]) {
					input[24 + count] = 1;
					sum++;
				} else {
					input[24 + count] = 0;
				}
				count++;

			}
		}
		input[49] = 1 / (1 + sum);
		input[50] = model.getPlayer().getHealth() / 5;
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

		if (!learn)
			return;
		if (history.size() > 1) {
			// System.out.println("learned");
			State current = history.poll();
			State next = history.peek();
			if (current.distanceKey <= next.distanceKey) {
				// System.out.println(current.action+ "Wrong direction key");
				current.rewards[8] = 1;
			}
			if (current.distanceDoor <= next.distanceDoor) {
				// System.out.println(current.action+ " Wrong direction door");
				current.rewards[9] = 1;
			}
			if (current.distanceExit <= next.distanceExit) {
				// System.out.println(current.action+ " Wrong direction exit");
				current.rewards[10] = 1;
			}

			mainBehaviour.updateNetwork(current, next, true);
			boolean nav = false;
			for (int i = 0; i < subBehaviours.length; i++) {
				if (subBehaviours[i] instanceof Navigate) {
					int[] inputKey = null;
					double[] rewardWeights = null;
					if (i == 0) {
						inputKey = navKey;
						rewardWeights = keyReward;
						if (current.input[0] == -1)
							continue;
					} else if (i == 1) {
						inputKey = navDoor;
						rewardWeights = doorReward;
						if (current.input[5] == -1)
							continue;
					} else {
						inputKey = navExit;
						rewardWeights = exitReward;
						if (current.input[10] == -1)
							continue;
					}
					if (subBehaviours[i].size[subBehaviours[i].size.length - 1] > current.action)
						((Navigate) subBehaviours[i]).updateNetwork(current, next, false, inputKey, rewardWeights);
				} else {
					if (subBehaviours[i].size[subBehaviours[i].size.length - 1] > current.action)
						subBehaviours[i].updateNetwork(current, next, false);
				}
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
		mainBehaviour.updateLearnRate();

		for (Behaviour b : subBehaviours) {
			b.updateLearnRate();
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
		if (learn) {
			epsilon += epsilonIncrement;
			epsilon = Math.min(epsilon, maxEpsilon);
			temp = decay * temp;
			temp = Math.max(temp, mintemp);
			tempMain = decayMain * tempMain;
			tempMain = Math.max(tempMain, mintemp);
		}

		System.out.println("Win, epsilon= " + this.epsilon + ", temp= " + this.temp + ", tempMain:" + tempMain
				+ ", rate:" + this.mainBehaviour.n.learningRate);
	}

	@Override
	public double getScore() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void save(String fileName, int t, int e) {
		// TODO Auto-generated method stub
		boolean flag = true;
		try {
			mainBehaviour.save(fileName, t, e);
			for (Behaviour b : subBehaviours) {
				if (b.ID != "Keyfinder" || (b.ID == "Keyfinder" && flag)) {
					b.save(fileName, t, e);
					flag = flag && b.ID != "Keyfinder";
				}
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	@Override
	public void load(String fileName) {
		mainBehaviour.load(fileName);
		for (Behaviour b : subBehaviours) {
			b.load(fileName);
		}

		subBehaviours[1] = subBehaviours[0];
		subBehaviours[2] = subBehaviours[0];
	}

	public void setModel(Model m, int i) {
		this.model = m;
		this.path = new ShortestPathFinder(m.getLevelMap(), m);
	}
}

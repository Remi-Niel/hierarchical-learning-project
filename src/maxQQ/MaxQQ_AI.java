package maxQQ;

import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;

import AI.AI;
import Assets.Player;
import Main.Map;
import Main.Model;
import PathFinder.ResultTuple;
import PathFinder.ShortestPathFinder;
import mapTiles.Door;
import mapTiles.Exit;
import mapTiles.Key;
import mapTiles.Tile;
import maxQQ.tasks.*;

public class MaxQQ_AI implements AI {

	Stack<AbstractAction> actionStack;
	Random r;
	ShortestPathFinder path;
	Model model;
	int inputs = 16;
	boolean shoot;
	double heading;

	Root root;
	GoToExit goToExit;
	OpenDoor openDoor;
	GetKey getKey;
	GoToDoor goToDoor;
	Navigate nav;

	public MaxQQ_AI(Model m) {
		path = new ShortestPathFinder(m.getLevelMap());
		r = new Random();
		shoot = false;
		heading = 0;
		model = m;
		// Add task and subtasks, make new SubTasks which are subclass of
		// Subtask class
		primitiveAction[] primitives = new primitiveAction[8];

		for (int i = 0; i < primitives.length; i++) {
			primitives[i] = new primitiveAction(i);
		}

		nav = new Navigate(primitives, new int[] { 5, 15, 8 }, new int[] { 0, 1, 2, 3, 4 }, m, 0);
		getKey = new GetKey(new AbstractAction[] { nav }, new int[] { 1, 1, 1 }, new int[] { 0 }, m, 0);
		goToDoor = new GoToDoor(new AbstractAction[] { nav }, new int[] { 1, 1, 1 }, new int[] { 5 }, m, 0);
		goToExit = new GoToExit(new AbstractAction[] { nav }, new int[] { 1, 1, 1 }, new int[] { 10 }, m, 0);
		openDoor = new OpenDoor(new AbstractAction[] { getKey, goToDoor }, new int[] { 3,4, 2 },
				new int[] { 0, 5, 15 }, m, 0);
		root = new Root(new AbstractAction[] { openDoor, goToExit }, new int[] { 3, 6, 2 }, new int[] { 0, 5, 10 }, m,
				0);

		actionStack = new Stack<AbstractAction>();
		actionStack.push(root);
		root.setStartTime(0);
		// System.out.println("Added " + actionStack.peek().getClass() + " to
		// the stack");
	}

	@Override
	public void determineAction(int time, boolean b) {
		AbstractAction top = actionStack.peek();
		SubTask subTop = null;
		double[] input = determineInput();
		while (!top.primitive()) {
			subTop = (SubTask) top;
			if (top instanceof GoToDoor) {
				actionStack.push(((GoToDoor) top).getSubtask(input, time, b));
			} else if (top instanceof GetKey) {
				actionStack.push(((GetKey) top).getSubtask(input, time, b));
			} else if (top instanceof GoToExit) {
				actionStack.push(((GoToExit) top).getSubtask(input, time, b));
			} else {
				actionStack.push(top.getSubtask(input, time, b));
			}
			// System.out.println("Added " + actionStack.peek().getClass() + "
			// to the stack");
			top = actionStack.peek();
			if (top instanceof SubTask) {
				((SubTask) top).setStartTime(time);
			}
		}
		subTop.addHistory(input);
		int action = actionStack.pop().getAction();
		//System.out.println("Action: " + action);
		shoot = action >= 8;
		heading = (action % 8) * Math.PI / 4;

	}

	public void checkTerminate(int time) {
		int i = 0;
		int l = Integer.MAX_VALUE;
		// Determine highest hierarchical task that is finished
		for (AbstractAction subTask : actionStack) {
			// System.out.println(subTask.getClass());
			if (subTask.finished(determineInput(), model, time)) {
				// System.out.println(i+" ");
				l = Math.min(i, l);
			}
			i++;
		}
		((SubTask) actionStack.peek()).reward(new ArrayList<double[]>(), determineInput(), 0, false, time);

		l = actionStack.size() - l - 1;
		// Terminate all subtasks below the highest finished task and the
		// finished task itself
		for (i = 0; i <= l; i++) {
			SubTask s = ((SubTask) actionStack.pop());
			// System.out.println("Popped " + s.getClass() + " from the stack");
			if (i == 0) {
				s.reward(new ArrayList<double[]>(), this.determineInput(), 0, true, time);
			}
			if (!actionStack.isEmpty()) {
				SubTask t = ((SubTask) actionStack.peek());
				t.reward(s.getInHist(), this.determineInput(), s.getRewardSum(), i < l, time);
			}
			s.finish();
		}
	}

	public double[] determineInput() {
		/*
		 * - Closeness is 1/(1 + distance to player), closeness of -1 is
		 * assumed. - Closeness of nearest key - 4 variables for Closeness of
		 * key to adjacent tiles - Closeness of nearest door - 4 variables for
		 * Closeness of door to adjacent tiles - Closeness of nearest exit - 4
		 * variables for Closeness of exit to adjacent tiles
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
						ResultTuple r = path.findPath(p.getX(), p.getY(), t.getX(), t.getY(), .95);
						if (r.distance < distanceKey) {
							distanceKey = r.distance;
							nearestKey = (Key) t;
						}
					} else if (t instanceof Door && t.getSolid()) {
						ResultTuple r = path.findPath(p.getX(), p.getY(), t.getX(), t.getY(), .95);
						if (r.distance < distanceDoor) {
							distanceDoor =r.distance;
							nearestDoor = (Door) t;
						}
					} else if (t instanceof Exit) {
						ResultTuple r = path.findPath(p.getX(), p.getY(), t.getX(), t.getY(), .95);
						if (r.distance < distanceExit) {
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
			input[1] = path.findPath(p.getX() + 1, p.getY(), nearestKey.getX(), nearestKey.getY(), .95).distance;
			input[2] = path.findPath(p.getX() - 1, p.getY(), nearestKey.getX(), nearestKey.getY(), .95).distance;
			input[3] = path.findPath(p.getX(), p.getY() + 1, nearestKey.getX(), nearestKey.getY(), .95).distance;
			input[4] = path.findPath(p.getX(), p.getY() - 1, nearestKey.getX(), nearestKey.getY(), .95).distance;

			double min = input[1], max = input[1];
			for (int i = 2; i < 5; i++) {
				if (input[i] < min)
					min = input[i];
				if (input[i] > max)
					max = input[i];
			}
			for (int i = 1; i < 5; i++) {
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
			double min = input[5], max = input[5];
			for (int i = 6; i < 10; i++) {
				if (input[i] < min)
					min = input[i];
				if (input[i] > max)
					max = input[i];
			}
			for (int i = 5; i < 10; i++) {
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
			double min = input[11], max = input[11];
			for (int i = 12; i < 15; i++) {
				if (input[i] < min)
					min = input[i];
				if (input[i] > max)
					max = input[i];
			}
			for (int i = 11; i < 15; i++) {
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
				}else{
					input[i]=1;
				}
			}
		}

		input[15] = model.getPlayer().getKeys();
		return input;

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
	public void reset(boolean train, int time) {
		boolean flag = true;

		while (actionStack.size() > 0) {
			SubTask s = ((SubTask) actionStack.pop());
			if (flag) {
				s.reward(new ArrayList<double[]>(), this.determineInput(), 0, true, time);
				flag = false;
			}
			if (actionStack.size() > 0) {
				SubTask t = ((SubTask) actionStack.peek());
				t.reward(s.getInHist(), this.determineInput(), s.getRewardSum(), true, time);
			}
			if (s instanceof Root) {
				System.out.println("Total reward: " + s.getRewardSum());
			}
			s.finish();
		}
		root.updateDiscount();
		openDoor.updateDiscount();
		getKey.updateDiscount();
		goToDoor.updateDiscount();
		goToExit.updateDiscount();
		nav.updateDiscount();

		actionStack.push(root);

	}

	public void openedDoor() {
		openDoor.setOpenedDoor(true);
	}

	public void reachedDoor() {
		goToDoor.reachedDoor();
	}

	public void gotKey() {
		getKey.gotKey();
	}

	public void reachedExit() {
		goToExit.reachedExit();
	}

}

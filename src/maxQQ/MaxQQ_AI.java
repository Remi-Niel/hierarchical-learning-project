package maxQQ;

import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;

import AI.AI;
import Assets.Enemy;
import Assets.Player;
import Main.Map;
import Main.Model;
import PathFinder.ResultTuple;
import PathFinder.ShortestPathFinder;
import mapTiles.Door;
import mapTiles.Exit;
import mapTiles.Key;
import mapTiles.Spawner;
import mapTiles.Tile;
import maxQQ.tasks.*;

public class MaxQQ_AI implements AI {

	Stack<AbstractAction> actionStack;
	Random r;
	ShortestPathFinder path;
	Model model;
	int inputs = 51;
	boolean shoot;
	double heading;

	Root root;
	GoToExit goToExit;
	OpenDoor openDoor;
	GetKey getKey;
	GoToDoor goToDoor;
	Navigate nav;
	Combat combat;

	public MaxQQ_AI(Model m) {
		path = new ShortestPathFinder(m.getLevelMap());
		r = new Random();
		shoot = false;
		heading = 0;
		model = m;
		// Add task and subtasks, make new SubTasks which are subclass of
		// Subtask class
		primitiveAction[] primitives = new primitiveAction[16];

		for (int i = 0; i < primitives.length; i++) {
			primitives[i] = new primitiveAction(i);
		}
		
		
		nav = new Navigate(primitives, new int[] { 30, 20, 10, 16 }, new int[] { 1, 2, 3, 4, 24, 25, 26, 27, 28, 29, 30,
				31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 50 }, m, 0);

		int[] inKey = new int[34];

		for (int i = 0; i < 34; i++) {
			inKey[i] = 16 + i;
		}

		combat = new Combat(primitives, new int[] { 35, 25, 15, 16 }, new int[] { 16, 17, 18, 19, 20, 21, 22, 23, 24, 25,
				26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50 }, m,
				0);

		getKey = new GetKey(new AbstractAction[] { nav }, new int[] { 1, 1, 1 }, new int[] { 0 }, m, 0);
		goToDoor = new GoToDoor(new AbstractAction[] { nav }, new int[] { 1, 1, 1 }, new int[] { 5 }, m, 0);
		goToExit = new GoToExit(new AbstractAction[] { nav }, new int[] { 1, 1, 1 }, new int[] { 10 }, m, 0);
		openDoor = new OpenDoor(new AbstractAction[] { getKey, goToDoor }, new int[] { 3, 20, 2 },
				new int[] { 0, 5, 15 }, m, 0);
		root = new Root(new AbstractAction[] { openDoor, goToExit, combat }, new int[] { 5, 20, 3 },
				new int[] { 0, 5, 10, 49, 50 }, m, 0);

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
				AbstractAction a = top.getSubtask(input, time, b);
				actionStack.push(a);
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
		// System.out.println("Action: " + action);
		shoot = action >= 8;
		heading = (action % 8) * Math.PI / 4;
		//System.out.println(shoot+"  "+heading);

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
							distanceDoor = r.distance;
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
			if(min==max){
				min=0;
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
			if(min==max){
				min=0;
			}
			for (int i = 6; i < 10; i++) {
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
			if(min==max){
				min=0;
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
		input[49] = sum;
		input[50] = model.getPlayer().getHealth();
		return input;

	}

	private int Hits(double heading) {
		int count = 0;

		Player p = model.getPlayer();
		double minDist = Double.MAX_VALUE;
		boolean spawner=false;
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
						if(t instanceof Spawner){
							spawner=true;
						}else{
							spawner=false;
						}
					}
				}
			}
		}
		if(spawner)count++;
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
			// if (s instanceof Root) {
			// System.out.println("Total reward: " + s.getRewardSum());
			// }
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

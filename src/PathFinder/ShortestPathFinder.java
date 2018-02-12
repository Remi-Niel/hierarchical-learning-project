package PathFinder;

import java.util.*;
import java.util.Random;

import Main.Map;
import Main.Model;
import mapTiles.Door;

public class ShortestPathFinder {

	class Position {
		int x, y;

		public Position(int x, int y) {
			this.x = x;
			this.y = y;
		}
	}

	int startX;
	int startY;
	int goalX;
	int goalY;

	private Queue<Position> queue;
	private Model model;
	private double[][][][] distanceFromTo;
	private int[][][][] direction;
	boolean[][] map;

	public ShortestPathFinder(Map levelMap, Model model) {

		this.model = model;
		map = new boolean[model.getLevelMap().getSize()][model.getLevelMap().getSize()];
		int s = model.getLevelMap().getSize();
		distanceFromTo = new double[s][s][s][s];
		direction = new int[s][s][s][s];
		for (int i = 0; i < s; i++) {
			for (int j = 0; j < s; j++) {
				for (int k = 0; k < s; k++) {
					for (int l = 0; l < s; l++) {
						distanceFromTo[i][j][k][l] = -1;
						direction[i][j][k][l] = -1;
					}
				}
			}
		}
		int startX = 0;
		int startY = 0;
		int goalX = 0;
		int goalY = 0;

	}

	private void addState(Position s, double distance, int d) {
		// System.out.println(map.length);
		if (!map[s.x][s.y] && (!model.getLevelMap().getTile(s.x, s.y).getSolid())
				|| (model.getLevelMap().getTile(s.x, s.y) instanceof Door)) {
			if (distanceFromTo[startX][startY][s.x][s.y] == -1
					|| (distance < distanceFromTo[startX][startY][s.x][s.y])) {
				distanceFromTo[startX][startY][s.x][s.y] = distance;
				direction[startX][startY][s.x][s.y] = d;
				queue.add(s);
			}
		}
	}

	public ResultTuple findPath(double x1, double y1, double x2, double y2, double diameter) {
		Map m = model.getLevelMap();
		map = new boolean[m.getSize()][m.getSize()];
		int direction = -1;
		queue = new LinkedList<Position>();

		startX = (int) (x1);
		startY = (int) (y1);
		goalX = (int) (x2);
		goalY = (int) (y2);

		// System.out.print(m.getTile(startX, startY).getClass() + " " + startX
		// + " " + startY + ": ");
		if (startX < 0 || startY < 0 || startX >= m.getSize() || startY >= m.getSize() || goalX < 0 || goalY < 0
				|| goalX >= m.getSize() || goalY >= m.getSize()
				|| (m.getTile(startX, startY).getSolid() && !(m.getTile(startX, startY) instanceof Door))
				|| ((m.getTile(startX, startY) instanceof Door) && (model.getPlayer().getKeys() <= 0))
				|| model.collides(x1, y1, true)) {
			return new ResultTuple(-1, -1);
		}
		distanceFromTo[startX][startY][startX][startY] = 0;

		if (distanceFromTo[startX][startY][goalX][goalY] >= 0) {
			ResultTuple t = new ResultTuple(this.direction[startX][startY][goalX][goalY],
					(int) distanceFromTo[startX][startY][goalX][goalY]);
			return t;
		}

		map[startX][startY] = true;

		if (startY > 0) {
			addState(new Position(startX, startY - 1), 1, 0);
		}
		if (startY > 0 && startX < m.getSize() - 1) {
			if (!m.getTile(startX + 1, startY).getSolid() && !m.getTile(startX, startY - 1).getSolid())
				addState(new Position(startX + 1, startY - 1), Math.sqrt(2), 1);
		}
		if (startX < m.getSize() - 1) {
			addState(new Position(startX + 1, startY), 1, 2);
		}
		if (startX < m.getSize() - 1 && startY < m.getSize() - 1) {
			if (!m.getTile(startX + 1, startY).getSolid() && !m.getTile(startX, startY + 1).getSolid())
				addState(new Position(startX + 1, startY + 1), Math.sqrt(2), 3);
		}
		if (startY < m.getSize() - 1) {
			addState(new Position(startX, startY + 1), 1, 4);
		}
		if (startY < m.getSize() - 1 && startX > 0) {
			if (!m.getTile(startX - 1, startY).getSolid() && !m.getTile(startX, startY + 1).getSolid())
				addState(new Position(startX - 1, startY + 1), Math.sqrt(2), 5);
		}
		if (startX > 0) {
			addState(new Position(startX - 1, startY), 1, 6);
		}
		if (startX > 0 && startY > 0) {
			if (!m.getTile(startX - 1, startY).getSolid() && !m.getTile(startX, startY - 1).getSolid())
				addState(new Position(startX - 1, startY - 1), Math.sqrt(2), 7);
		}

		while (!queue.isEmpty()) {
			Position p = queue.poll();
			double distance = distanceFromTo[startX][startY][p.x][p.y];
			int action = this.direction[startX][startY][p.x][p.y];
			if (p.x == goalX && p.y == goalY) {
				continue;
			}

			if (p.x > 0) {
				addState(new Position(p.x, p.y - 1), distance + 1, action);
			}
			if (p.y > 0 && p.x < m.getSize() - 1) {
				if (!m.getTile(p.x + 1, p.y).getSolid() && !m.getTile(p.x, p.y - 1).getSolid())
					addState(new Position(p.x + 1, p.y - 1), distance + Math.sqrt(2), action);
			}
			if (p.x < m.getSize() - 1) {
				addState(new Position(p.x + 1, p.y), distance + 1, action);
			}
			if (p.x < m.getSize() - 1 && p.y < m.getSize() - 1) {
				if (!m.getTile(p.x + 1, p.y).getSolid() && !m.getTile(p.x, p.y + 1).getSolid())
					addState(new Position(p.x + 1, p.y + 1), distance + Math.sqrt(2), action);
			}
			if (p.y < m.getSize() - 1) {
				addState(new Position(p.x, p.y + 1), distance + 1, action);
			}
			if (p.y < m.getSize() - 1 && p.x > 0) {
				if (!m.getTile(p.x - 1, p.y).getSolid() && !m.getTile(p.x, p.y + 1).getSolid())
					addState(new Position(p.x - 1, p.y + 1), distance + Math.sqrt(2), action);
			}
			if (p.x > 0) {
				addState(new Position(p.x - 1, p.y), distance + 1, action);
			}
			if (p.x > 0 && p.y > 0) {
				if (!m.getTile(p.x - 1, p.y).getSolid() && !m.getTile(p.x, p.y - 1).getSolid())
					addState(new Position(p.x - 1, p.y - 1), distance + Math.sqrt(2), action);
			}
		}

		ResultTuple t = new ResultTuple(this.direction[startX][startY][goalX][goalY],
				(int) distanceFromTo[startX][startY][goalX][goalY]);
		return t;

	}

}

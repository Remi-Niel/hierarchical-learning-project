package PathFinder;

import java.util.*;
import java.util.Random;

import Main.Map;
import mapTiles.Door;

public class ShortestPathFinder {

	class Position {
		int x, y;

		public Position(int x, int y) {
			this.x = x;
			this.y = y;
		}
	}

	final int factor = 10000;
	int startX;
	int startY;
	int goalX;
	int goalY;

	private Queue<Position> queue;
	private Map m;
	private double[][][][] distanceFromTo;
	boolean[][] map;

	public ShortestPathFinder(Map levelMap) {
		m = levelMap;
		map = new boolean[m.getSize()][m.getSize()];
		int s = m.getSize();
		distanceFromTo = new double[s][s][s][s];
		for (int i = 0; i < s; i++) {
			for (int j = 0; j < s; j++) {
				for (int k = 0; k < s; k++) {
					for (int l = 0; l < s; l++) {
						distanceFromTo[i][j][k][l] = -1;
					}
				}
			}
		}
		int startX = 0;
		int startY = 0;
		int goalX = 0;
		int goalY = 0;

	}

	private void addState(Position s, double value) {
		//System.out.println(map.length);
		if (!map[s.x][s.y] && (!m.getTile(s.x, s.y).getSolid()) || (m.getTile(s.x, s.y) instanceof Door)) {
			if (distanceFromTo[startX][startY][s.x][s.y]==-1 || (value < distanceFromTo[startX][startY][s.x][s.y])) {
				distanceFromTo[startX][startY][s.x][s.y] = value;
				queue.add(s);
			}
		}
	}

	public ResultTuple findPath(double x1, double y1, double x2, double y2, double diameter) {
		map = new boolean[m.getSize()][m.getSize()];
		int direction = -1;
		queue = new LinkedList<Position>();

		startX = (int) (x1);
		startY = (int) (y1);
		goalX = (int) (x2);
		goalY = (int) (y2);

		distanceFromTo[startX][startY][startX][startY] = 0;
		
		if(x2<0||y2<0||x2>=m.getSize()||y2>=m.getSize()){
			return new ResultTuple(-1,-1);
		}
		
		
		if (distanceFromTo[startX][startY][goalX][goalY] >= 0) {
			ResultTuple t = new ResultTuple((int) distanceFromTo[startX][startY][goalX][goalY] / factor,
					distanceFromTo[startX][startY][goalX][goalY] % factor);
			return t;
		}

		map[startX][startY] = true;

		if (startY > 0) {
			addState(new Position(startX, startY - 1), 1);
		}
		if (startY > 0 && startX < m.getSize() - 1) {
			if (!m.getTile(startX + 1, startY).getSolid() && !m.getTile(startX, startY - 1).getSolid())
				addState(new Position(startX + 1, startY - 1), Math.sqrt(2) + factor);
		}
		if (startX < m.getSize() - 1) {
			addState(new Position(startX + 1, startY), 1 + (2 * factor));
		}
		if (startX < m.getSize() - 1 && startY < m.getSize() - 1) {
			if (!m.getTile(startX + 1, startY).getSolid() && !m.getTile(startX, startY + 1).getSolid())
				addState(new Position(startX + 1, startY + 1), Math.sqrt(2) + factor * 3);
		}
		if (startY < m.getSize() - 1) {
			addState(new Position(startX, startY + 1), 1 + factor * 4);
		}
		if (startY < m.getSize() - 1 && startX > 0) {
			if (!m.getTile(startX - 1, startY).getSolid() && !m.getTile(startX, startY + 1).getSolid())
				addState(new Position(startX - 1, startY + 1), Math.sqrt(2) + 5 * factor);
		}
		if (startX > 0) {
			addState(new Position(startX - 1, startY), 1 + 6 * factor);
		}
		if (startX > 0 && startY > 0) {
			if (!m.getTile(startX - 1, startY).getSolid() && !m.getTile(startX, startY - 1).getSolid())
				addState(new Position(startX - 1, startY - 1), Math.sqrt(2) + 7 * factor);
		}

		while (!queue.isEmpty()) {
			Position p = queue.poll();
			double distance = distanceFromTo[startX][startY][p.x][p.y] % factor;
			int action = (int) distanceFromTo[startX][startY][p.x][p.y] / factor;
			if (p.x == goalX && p.y == goalY) {
				continue;
			}

			if (p.x > 0) {
				addState(new Position(p.x, p.y - 1),distance+ 1 + action * factor);
			}
			if (p.y > 0 && p.x < m.getSize() - 1) {
				if (!m.getTile(p.x + 1, p.y).getSolid() && !m.getTile(p.x, p.y - 1).getSolid())
					addState(new Position(p.x + 1, p.y - 1), distance+Math.sqrt(2) + action * factor);
			}
			if (p.x < m.getSize() - 1) {
				addState(new Position(p.x + 1, p.y),distance+ 1 + (action * factor));
			}
			if (p.x < m.getSize() - 1 && p.y < m.getSize() - 1) {
				if (!m.getTile(p.x + 1, p.y).getSolid() && !m.getTile(p.x, p.y + 1).getSolid())
					addState(new Position(p.x + 1, p.y + 1),distance+ Math.sqrt(2) + factor * action);
			}
			if (p.y < m.getSize() - 1) {
				addState(new Position(p.x, p.y + 1),distance+ 1 + factor * action);
			}
			if (p.y < m.getSize() - 1 && p.x > 0) {
				if (!m.getTile(p.x - 1, p.y).getSolid() && !m.getTile(p.x, p.y + 1).getSolid())
					addState(new Position(p.x - 1, p.y + 1),distance+ Math.sqrt(2) + action * factor);
			}
			if (p.x > 0) {
				addState(new Position(p.x - 1, p.y),distance+ 1 + action * factor);
			}
			if (p.x > 0 && p.y > 0) {
				if (!m.getTile(p.x - 1, p.y).getSolid() && !m.getTile(p.x, p.y - 1).getSolid())
					addState(new Position(p.x - 1, p.y - 1),distance+ Math.sqrt(2) + action * factor);
			}
		}

		ResultTuple t = new ResultTuple((int) distanceFromTo[startX][startY][goalX][goalY] / 100000000,
				distanceFromTo[startX][startY][goalX][goalY] % 100000000);
		return t;

	}

}

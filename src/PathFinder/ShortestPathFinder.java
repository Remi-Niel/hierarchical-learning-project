package PathFinder;

import java.util.PriorityQueue;
import java.util.Random;

import Main.Map;

public class ShortestPathFinder {

	class Position implements Comparable<Position> {
		int x, y;
		double travelled;
		double heuristic;
		double total;
		int direction;
		Random rand = new Random();

		public Position(int x, int y, int direction, double travelled, double heuristic) {
			this.x = x;
			this.y = y;
			this.direction = direction;
			this.travelled = travelled;
			this.heuristic = heuristic;
			total = heuristic + travelled;
		}

		public void set(int direction, double travelled) {
			if (travelled < this.travelled) {
				this.direction = direction;
				this.travelled = travelled;
				total = heuristic + travelled;
				addState(this);
			}
		}

		@Override
		public int compareTo(Position s) {
			if (this.total - total == 0) {
				return 1 - (rand.nextInt(2)) * 2;
			}
			if (this.total > s.total) {
				return 1;
			} else {
				return -1;
			}
		}
	}

	private PriorityQueue<Position> queue;
	private Map m;

	public ShortestPathFinder(Map levelMap) {
		m = levelMap;
	}

	private void addState(Position s) {
		if (!m.getTile(s.x, s.y).getSolid()) {
			queue.offer(s);
		}
	}

	public ResultTuple findPath(double x1, double y1, double x2, double y2, double diameter) {
		int direction = -1;
		queue = new PriorityQueue<Position>();
		Position[][] map = new Position[m.getSize()][m.getSize()];

		int startX = (int) (x1);
		int startY = (int) (y1);
		int goalX = (int) (x2);
		int goalY = (int) (y2);

		// System.out.println(startX +" "+startY +" " + goalX +" "+goalY);

		// if already on correct tile return 'no move'
		if (this.heuristic(startX, startY, goalX, goalY) <= Math.sqrt(2)) {

			return new ResultTuple(-1, 0);

		}

		for (int i = 0; i < m.getSize(); i++) {
			for (int j = 0; j < m.getSize(); j++) {
				map[i][j] = new Position(i, j, 'n', 1000 * m.getSize(), this.heuristic(i, j, goalX, goalY));
			}
		}
		if (startY > 0) {
			map[startX][startY - 1].set(0, 1);
		}
		if (startY > 0 && startX < m.getSize() - 1) {
			if (!m.getTile(startX + 1, startY).getSolid() && !m.getTile(startX, startY - 1).getSolid())
				map[startX + 1][startY - 1].set(1, Math.sqrt(2));
		}
		if (startX < m.getSize() - 1) {
			map[startX + 1][startY].set(2, 1);
		}
		if (startX < m.getSize() - 1 && startY < m.getSize() - 1) {
			if (!m.getTile(startX + 1, startY).getSolid() && !m.getTile(startX, startY + 1).getSolid())
				map[startX + 1][startY + 1].set(3, Math.sqrt(2));
		}
		if (startY < m.getSize() - 1) {
			map[startX][startY + 1].set(4, 1);
		}
		if (startY < m.getSize() - 1 && startX > 0) {
			if (!m.getTile(startX - 1, startY).getSolid() && !m.getTile(startX, startY + 1).getSolid())
				map[startX - 1][startY + 1].set(5, Math.sqrt(2));
		}
		if (startX > 0) {
			map[startX - 1][startY].set(6, 1);
		}
		if (startX > 0 && startY > 0) {
			if (!m.getTile(startX - 1, startY).getSolid() && !m.getTile(startX, startY - 1).getSolid())
				map[startX - 1][startY - 1].set(7, Math.sqrt(2));
		}

		ResultTuple tuple = new ResultTuple('n', 0);
		Position s;
		while (queue.size() > 0) {

			s = queue.poll();
			// System.out.println(s.x +" "+s.y+" "+x2+" "+y2+" "+s.travelled+"
			// "+queue.size()+" "+s.direction);
			if (s.x == goalX && s.y == goalY) {
				// System.out.println(s.direction);
				tuple.direction = s.direction;
				tuple.distance = s.total;
				continue;
			}
			if (s.x < m.getSize()) {
				map[s.x + 1][s.y].set(s.direction, s.travelled + 1);
			}
			if (s.x > 0) {
				map[s.x - 1][s.y].set(s.direction, s.travelled + 1);
			}
			if (s.y < m.getSize()) {
				map[s.x][s.y + 1].set(s.direction, s.travelled + 1);
			}
			if (s.y > 0) {
				map[s.x][s.y - 1].set(s.direction, s.travelled + 1);
			}

			if (s.y > 0 && s.x < m.getSize() - 1) {
				if (!m.getTile(s.x + 1, s.y).getSolid() && !m.getTile(s.x, s.y - 1).getSolid())
					map[s.x + 1][s.y - 1].set(s.direction, s.travelled + Math.sqrt(2));
			}
			if (s.x < m.getSize() - 1 && s.y < m.getSize() - 1) {
				if (!m.getTile(s.x + 1, s.y).getSolid() && !m.getTile(s.x, s.y + 1).getSolid())
					map[s.x + 1][s.y + 1].set(s.direction, s.travelled + Math.sqrt(2));
			}
			if (s.y < m.getSize() - 1 && s.x > 0) {
				if (!m.getTile(s.x - 1, s.y).getSolid() && !m.getTile(s.x, s.y + 1).getSolid())
					map[s.x - 1][s.y + 1].set(s.direction, s.travelled + Math.sqrt(2));
			}
			if (s.x > 0 && s.y > 0) {
				if (!m.getTile(s.x - 1, s.y).getSolid() && !m.getTile(s.x, s.y - 1).getSolid())
					map[s.x - 1][s.y - 1].set(s.direction, s.travelled + Math.sqrt(2));
			}

		}
		// System.out.println(direction);

		// System.out.println(direction);

		return tuple;
	}

	private double heuristic(int x1, int y1, int x2, int y2) {
		return Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
	}

}

package AI;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Arrays;

import Assets.Enemy;
import Assets.Player;
import Main.Map;
import Main.Model;
import PathFinder.ResultTuple;
import PathFinder.ShortestPathFinder;
import mapTiles.Door;
import mapTiles.Exit;
import mapTiles.Key;
import mapTiles.Tile;

public class ManualAI implements AI, KeyListener {

	boolean left, right, up, down, shoot;
	double heading = -1;
	Model model;
	ShortestPathFinder path;

	double distanceKey = -1;
	double distanceDoor = -1;
	double distanceExit = -1;

	public ManualAI(Model m) {
		model = m;
		ShortestPathFinder path = new ShortestPathFinder(m.getLevelMap(), m);
		left = right = up = down = shoot = false;
	}

	public void setHeading() {
		double vertical = -1;
		double horizontal = -1;
		double heading = 0;

		if (up && !down) {
			vertical = Math.PI / 2;
		} else if (!up && down) {
			vertical = Math.PI * 3 / 2;
		}

		if (left && !right) {
			horizontal = Math.PI;
		} else if (!left && right) {
			horizontal = 0;
		}

		int count = 0;

		if (vertical != -1) {
			heading += vertical;
			count++;
		}

		if (horizontal != -1) {
			if (horizontal == 0) {
				if (vertical > Math.PI) {
					heading += 2 * Math.PI;
				}
			} else {
				heading += horizontal;
			}
			count++;
		}

		if (count == 0) {
			this.heading = -1;
		} else {
			this.heading = heading / count;
		}
	}

	@Override
	public double getHeading() {
		return heading;
	}

	@Override
	public boolean shoot() {
		return shoot;
	}

	@Override
	public void reset(boolean t, int time) {
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_UP) {
			up = true;
			this.setHeading();
		} else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
			down = true;
			this.setHeading();
		} else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
			left = true;
			this.setHeading();
		} else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
			right = true;
			this.setHeading();
		} else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
			shoot = true;
		}

	}

	@Override
	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_UP) {
			up = false;
			this.setHeading();
		} else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
			down = false;
			this.setHeading();
		} else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
			left = false;
			this.setHeading();
		} else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
			right = false;
			this.setHeading();
		} else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
			shoot = false;
		}
	}

	public void determineAction(int time, boolean b) {
		System.out.println(Arrays.toString(determineInput()));
	}

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

		System.out.println(p.getY());
		if (nearestExit == null) {
			for (int i = 10; i < 15; i++) {
				input[i] = -1;
			}
		} else {
			input[10] = distanceExit;
			input[11] = path.findPath(p.getX() + 1, p.getY(), nearestExit.getX(), nearestExit.getY(), .95).distance;
			input[12] = path.findPath(p.getX() - 1, p.getY(), nearestExit.getX(), nearestExit.getY(), .95).distance;
			input[13] = path.findPath(p.getX(), p.getY() + 2, nearestExit.getX(), nearestExit.getY(), .95).distance;
			System.out.println("Up: " + input[14]);
			input[14] = path.findPath(p.getX(), p.getY() - 2, nearestExit.getX(), nearestExit.getY(), .95).distance;
			double min = Double.POSITIVE_INFINITY, max = Double.NEGATIVE_INFINITY;
			for (int i = 11; i < 15; i++) {
				if (input[i] == -1)
					continue;
				if (input[i] < min)
					min = input[i];
				if (input[i] > max)
					max = input[i];
			}
			System.out.println(min + "," + max);
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

	@Override
	public double getScore() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void save(String fileName, int t, int e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void load(String s) {
		// TODO Auto-generated method stub

	}

	public void setModel(Model m, int i) {
		this.model = m;
		path = new ShortestPathFinder(m.getLevelMap(), m);
	}

}

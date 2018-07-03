package Main;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import AI.*;
import Assets.*;
import PathFinder.*;
import mapTiles.*;
import maxQQ.MaxQQ_AI;

public class Model {

	Random r = new Random();
	Map levelMap;
	CopyOnWriteArrayList<Enemy> enemyList;
	Player player;
	double mapSize;
	boolean enemyMap[][];
	double bulletDist;
	Bullet b;
	public boolean gameOver = false;
	ShortestPathFinder p;
	Queue<State> history;
	String map;
	AI ai;
	final int timeLimit = 20000;
	public boolean enemyDied;
	public boolean enemyDamaged;
	public boolean playerDamaged;

	public double score = 0;
	public boolean win = false;

	public Model(String fileName) {
		map = fileName;
		enemyList = new CopyOnWriteArrayList<Enemy>();
		try {
			levelMap = new Map(fileName, enemyList);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		// System.out.println(enemyList.size());
		p = new ShortestPathFinder(levelMap, this);
		mapSize = (double) levelMap.getSize();
		enemyMap = new boolean[levelMap.getSize()][levelMap.getSize()];
		player = new Player((levelMap.getSpawnX() + 0.5), (levelMap.getSpawnY() + 0.5));
		enemyDied = false;
		enemyDamaged = false;
		playerDamaged = false;
	}

	public void setHistory(Queue<State> history) {
		this.history = history;
	}

	public double distance(double x1, double x2, double y1, double y2) {

		return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
	}

	public boolean colidesWithEnemy(double x, double y) {

		for (Enemy e : enemyList) {
			if (distance(e.getX(), x, e.getY(), y) < e.diameter) {
				return true;
			}
		}

		return false;
	}

	public boolean legalLocation(double x, double y) {
		return (x > 0 && y > 0 && x < mapSize && y < mapSize && !colidesWithEnemy((x), (y))
				&& !levelMap.getTile((int) (x), (int) (y)).getSolid());

	}

	public void tickSpawners() {
		for (Spawner s : levelMap.getSpawners()) {
			if (s.spawn()) {
				double dX = 0;
				double dY = 0;
				boolean legal = false;
				for (int x = 0; x <= 2; x++) {
					for (int y = 0; y <= Math.min(x + 1, 2); y++) {
						dX = s.getX() + x + 0.5;
						dY = s.getY() - y + 0.5;

						if (legalLocation(dX, dY)) {
							legal = true;
							break;
						}

						dY = s.getY() + y + 0.5;

						if (legalLocation(dX, dY)) {
							legal = true;
							break;
						}
						dX = s.getX() - x + 0.5;
						dY = s.getY() - y + 0.5;

						if (legalLocation(dX, dY)) {
							legal = true;
							break;
						}

						dY = s.getY() + y + 0.5;

						if (legalLocation(dX, dY)) {
							legal = true;
							break;
						}
						dX = s.getX() + 0.5;
						dY = s.getY() - y + 0.5;

						if (legalLocation(dX, dY)) {
							legal = true;
							break;
						}

						dY = s.getY() + y + 0.5;

						if (legalLocation(dX, dY)) {
							legal = true;
							break;
						}

					}
					if (legal)
						break;
				}
				if (legal)
					enemyList.add(new Ghost((dX), (dY), s));
			}
		}
	}

	public void updatePlayer(int time) {
		player.tick();
		movePlayer(time);
		if (time >= timeLimit - 1) {
			this.gameOver = true;
			// score -= 10;
			this.win = false;

			if (ai instanceof HierarchicalAI) {
				((LinkedList<State>) history).getLast().death();
			}
		}
	}

	public void updateEnemyMap() {
		enemyMap = new boolean[levelMap.getSize()][levelMap.getSize()];

		for (Enemy e : enemyList) {
			enemyMap[(int) e.getX()][(int) e.getY()] = true;
		}

		for (Tile[] ts : levelMap.getTileMap()) {
			for (Tile t : ts) {
				if (t instanceof Spawner) {
					enemyMap[t.getX()][t.getY()] = true;
				}
			}
		}

	}

	public void moveEnemies() {
		for (Enemy e : enemyList) {
			if (!levelMap.getTile((int) (e.getX()), (int) (e.getY())).reachable()) {
				e.setHeading(-1);
				continue;
			}

			// System.out.println(e.getX()+" "+player.getX());

			ResultTuple r = p.findPath(e.getX(), e.getY(), player.getX(), player.getY(), e.diameter);
			if (r.distance > 10) {
				e.setHeading(-1);
				continue;
			}

			double heading = e.getHeading();
			if (heading == -1) {
				if (r.distance > 0) {
					// System.out.println(r.direction);
					heading = Math.PI / 2 - r.direction * Math.PI / 4;
					if (heading < 0) {
						heading += 2 * Math.PI;
					}
				} else {
					heading = -Math.atan2(player.getY() - e.getY(), player.getX() - e.getX());
					if (heading < 0) {
						heading += 2 * Math.PI;
					}
					int d = (int) Math.round(heading * 4 / (Math.PI));
					heading = d * Math.PI / 4;
				}
				e.setHeading(heading);
			}

			double distance = e.getSpeed();
			double dx = Math.cos(heading) * distance;
			double dy = -Math.sin(heading) * distance;

			if (!this.collidesEnemy(e.getX() + dx, e.getY() + dy))
				e.move(e.getX() + dx, e.getY() + dy);

			if (distance(e.getX(), player.getX(), e.getY(), player.getY()) < e.diameter) {
				this.playerDamaged = true;
				if (!gameOver && player.damage(e.getHealth())) {
					gameOver = true;
					score -= 10;
				}

				if (e instanceof Ghost) {
					((Ghost) e).getParent().decrementCount();
				}
				enemyList.remove(e);
				enemyDied = true;
				if (ai instanceof HierarchicalAI) {

					score += 10;
					((LinkedList<State>) history).getLast().damagedEnemy();
					((LinkedList<State>) history).getLast().damaged();
					if (gameOver) {
						((LinkedList<State>) history).getLast().death();
					}
				}

			}

		}
		updateEnemyMap();
	}

	public boolean collides(double x, double y, int g) {

		Tile t[] = new Tile[9];

		State current = null;
		if (ai instanceof HierarchicalAI && history.size() > 0)
			current = ((LinkedList<State>) history).getLast();

		t[0] = levelMap.getTile((int) (x + 0.4), (int) (y - 0.4));
		t[1] = levelMap.getTile((int) (x + 0.4), (int) (y + 0.4));
		t[2] = levelMap.getTile((int) (x - 0.4), (int) (y - 0.4));
		t[3] = levelMap.getTile((int) (x - 0.4), (int) (y + 0.4));
		t[4] = levelMap.getTile((int) (x + 0.4), (int) (y));
		t[5] = levelMap.getTile((int) (x - 0.4), (int) (y));
		t[6] = levelMap.getTile((int) (x), (int) (y + 0.4));
		t[7] = levelMap.getTile((int) (x), (int) (y - 0.4));
		t[8] = levelMap.getTile((int) (x), (int) (y));

		for (int i = 0; i < 9; i++) {
			if (t[i].getSolid()) {
				if (t[i] instanceof Door) {
					if (ai instanceof HierarchicalAI)
						current.reachedDoor();
					if (ai instanceof MaxQQ_AI) {
						((MaxQQ_AI) ai).reachedDoor(player.getKeys() > 0);
					}
					if (player.useKey()) {
						score += 10;
						if (ai instanceof HierarchicalAI)
							current.openedDoor();
						if (ai instanceof MaxQQ_AI) {
							((MaxQQ_AI) ai).openedDoor();
						}
						levelMap.open(t[i].getX(), t[i].getY());
						levelMap.floodFillReachable(t[i].getX(), t[i].getY());
					} else {
						return true;
					}
				} else {

					return true;
				}
			}

		}
		boolean key = false;
		t[0] = levelMap.getTile((int) (x + .8), (int) (y - .8));
		t[1] = levelMap.getTile((int) (x + .8), (int) (y + .8));
		t[2] = levelMap.getTile((int) (x - .8), (int) (y - .8));
		t[3] = levelMap.getTile((int) (x - .8), (int) (y + .8));
		t[4] = levelMap.getTile((int) (x), (int) (y));
		t[5] = levelMap.getTile((int) (x + .8), (int) (y));
		t[6] = levelMap.getTile((int) (x - .8), (int) (y));
		t[7] = levelMap.getTile((int) (x), (int) (y + .8));
		t[8] = levelMap.getTile((int) (x), (int) (y - .8));

		for (int i = 0; i < 9; i++) {
			if (t[i] instanceof Key && !key) {
				key = !key;
				score += 5;
				if (ai instanceof HierarchicalAI)
					current.gotKey();
				if (ai instanceof MaxQQ_AI) {
					((MaxQQ_AI) ai).gotKey();
				}
				player.addKey();
				levelMap.destroyTile(t[i].getX(), t[i].getY());
			}
			if (t[i] instanceof Health) {
				if (ai instanceof HierarchicalAI)
					current.health();
				player.damage(-((Health) t[i]).health);
				levelMap.destroyTile(t[i].getX(), t[i].getY());
			}
			if (t[i] instanceof Exit) {
				if (!gameOver)
					score += 100;
				if (ai instanceof HierarchicalAI)
					current.win();
				gameOver = true;
				win = true;
				if (ai instanceof MaxQQ_AI) {
					((MaxQQ_AI) ai).reachedExit();
				}
			}

		}
		return false;
	}

	public boolean collidesEnemy(double x, double y) {

		Tile t[] = new Tile[9];

		t[0] = levelMap.getTile((int) (x + 0.4), (int) (y - 0.4));
		t[1] = levelMap.getTile((int) (x + 0.4), (int) (y + 0.4));
		t[2] = levelMap.getTile((int) (x - 0.4), (int) (y - 0.4));
		t[3] = levelMap.getTile((int) (x - 0.4), (int) (y + 0.4));
		t[4] = levelMap.getTile((int) (x + 0.4), (int) (y));
		t[5] = levelMap.getTile((int) (x - 0.4), (int) (y));
		t[6] = levelMap.getTile((int) (x), (int) (y + 0.4));
		t[7] = levelMap.getTile((int) (x), (int) (y - 0.4));
		t[8] = levelMap.getTile((int) (x), (int) (y));

		for (int i = 0; i < 9; i++) {
			if (t[i].getSolid()) {
				return true;
			}
		}
		return false;
	}

	public boolean collides(double x, double y, boolean b) {

		Tile t[] = new Tile[9];

		t[0] = levelMap.getTile((int) (x + 0.4), (int) (y - 0.4));
		t[1] = levelMap.getTile((int) (x + 0.4), (int) (y + 0.4));
		t[2] = levelMap.getTile((int) (x - 0.4), (int) (y - 0.4));
		t[3] = levelMap.getTile((int) (x - 0.4), (int) (y + 0.4));
		t[4] = levelMap.getTile((int) (x + 0.4), (int) (y));
		t[5] = levelMap.getTile((int) (x - 0.4), (int) (y));
		t[6] = levelMap.getTile((int) (x), (int) (y + 0.4));
		t[7] = levelMap.getTile((int) (x), (int) (y - 0.4));
		t[8] = levelMap.getTile((int) (x), (int) (y));

		for (int i = 0; i < 4; i++) {
			if (t[i].getSolid()) {
				if (t[i] instanceof Door) {
					if (player.getKeys() <= 0) {
						return true;
					}
				} else {
					return true;
				}
			}

		}

		return false;
	}

	public void movePlayer(int t) {
		State current = null;
		if (ai instanceof HierarchicalAI && history.size() > 0)
			current = ((LinkedList<State>) history).getLast();
		// teteSystem.out.println(ai.toString());
		if (ai.getHeading() < 0 || (ai.shoot() && player.loaded()))
			return;

		double x = player.getX();
		double y = player.getY();
		double distance = player.speed;
		double dx = Math.cos(ai.getHeading()) * distance;
		double dy = -Math.sin(ai.getHeading()) * distance;
		x += dx;
		y += dy;

		if (!collides(x, y, t)) {
			player.move(x, y);
		} else {

			score -= 0.1;
			if (ai instanceof MaxQQ_AI) {
				((MaxQQ_AI) ai).walkedIntoWall(t);
			}
			if (ai instanceof HierarchicalAI) {
				current.hitWall();
			}

			x -= dx;
			if (!collides(x, y, t)) {
				player.move(x, y);
			} else {
				x += dx;
				y -= dy;
				if (!collides(x, y, t)) {
					player.move(x, y);
				}
			}
		}

	}

	private void updateHistoryBullet(int t, boolean hit) {

		if (history == null)
			return;
		for (State s : ((LinkedList<State>) history)) {
			if (s.turnTime == t) {
				s.bulletHit(hit);
				continue;
			}
		}

	}

	// (y1-y2)x + (x2-x1)y + (x1y2-x2y1)
	// d(P,L) = --------------------------------
	// sqrt( (x2-x1)pow2 + (y2-y1)pow2 )
	public double bulletDistance(double x, double y) {

		double x1 = b.getX();
		double y1 = b.getY();
		double x2 = x1 + (1000 / mapSize * Math.cos(b.getHeading()));
		double y2 = y1 - (1000 / mapSize * Math.sin(b.getHeading()));

		double ch = (y1 - y2) * x + (x2 - x1) * y + (x1 * y2 - x2 * y1);
		double del = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
		double distance = ch / del;

		return distance;
	}

	public void updateBullets(int time) {

		if (b != null) {

			b.move();
			int bulletTime = b.getSpawnTime();
			boolean destroyed = false;
			boolean hit = false;
			double minDist = Double.MAX_VALUE;
			Spawner closest = null;
			for (Tile[] ts : levelMap.getTileMap()) {
				for (Tile t : ts) {

					double heading = -Math.atan2(t.getY() - player.getY(), t.getX() - player.getX());
					if (heading < 0)
						heading += 2 * Math.PI;

					double relativeHeading = Math.abs(heading - ai.getHeading());

					if (relativeHeading < Math.PI / 2 && t.getSolid()
							&& Math.abs(bulletDistance(t.getX(), t.getY())) < 1.5) {
						if (distance(player.getX(), t.getX(), player.getY(), t.getY()) < minDist) {
							minDist = distance(player.getX(), t.getX(), player.getY(), t.getY());
							if (t instanceof Spawner) {
								closest = (Spawner) t;
							} else {
								closest = null;
							}
						}
						destroyed = true;
					}
				}
			}

			if (closest != null) {

				if (closest.damage()) {

					levelMap.destroyTile(closest.getX(), closest.getY());
					enemyDied = true;
				} else {
					enemyDamaged = true;
				}
				hit = true;
			}
			// System.out.println("Test");
			bulletDist = minDist;
			for (Enemy e : enemyList) {
				if (!levelMap.getTile((int) (e.getX()), (int) (e.getY())).reachable()) {
					continue;
				}

				double heading = -Math.atan2(e.getY() - player.getY(), e.getX() - player.getX());
				if (heading < 0)
					heading += 2 * Math.PI;

				double relativeHeading = Math.abs(heading - ai.getHeading());

				// System.out.println("Enemy:
				// "+Math.abs(bulletDistance(e.getX(), e.getY()))+",
				// "+(heading)+", "+ai.getHeading()+", "+relativeHeading);
				double dist = Math.abs(player.getX() - e.getX()) + Math.abs(player.getY() - e.getY());
				double pathdist = p.findPath(player.getX(), player.getY(), e.getX(), e.getY(), .95).distance;
				if (relativeHeading < Math.PI / 2 && Math.abs(bulletDistance(e.getX(), e.getY())) < 1.5) {
					// System.out.println("Hit!");
					if (distance(e.getX(), player.getX(), e.getY(), player.getY()) < bulletDist && pathdist <= dist) {
						if (ai instanceof HierarchicalAI) {
							((LinkedList<State>) history).getLast().damagedEnemy();
						}
						if (e.hit()) {
							enemyList.remove(e);
							enemyDied = true;
						}
						enemyDamaged = true;
						hit = true;
						destroyed = true;
					}
				}
			}
			if (destroyed) {
				updateHistoryBullet(bulletTime, hit);
			}
			b = null;
		}
		if (enemyDamaged) {
			score += 10;
		}
		if (ai.shoot() && ai.getHeading() != -1 && player.shoot()) {
			// System.out.println("Shoot!!!");
			b = new Bullet(mapSize, player.getX(), player.getY(), ai.getHeading(), time);
		}
	}

	public Bullet getBullet() {
		return b;
	}

	public Map getLevelMap() {
		return levelMap;
	}

	public CopyOnWriteArrayList<Enemy> getEnemyList() {
		return enemyList;
	}

	public Player getPlayer() {
		return player;
	}

	public void setAI(AI ai) {
		this.ai = ai;
	}

	public double getHeading() {
		return ai.getHeading();
	}

	public void reset() {
		score = 0;
		gameOver = false;
		enemyList.clear();
		win = false;
		b = null;
		try {
			levelMap = new Map(map, enemyList);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		player = new Player((levelMap.getSpawnX() + 0.5), (levelMap.getSpawnY() + 0.5));
	}

	public boolean[][] getEnemyMap() {
		return enemyMap;
	}
}

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
	Bullet b;
	public boolean gameOver = false;
	ShortestPathFinder p;
	Queue<State> history;
	String map;
	AI ai;
	final int timeLimit = 100000;

	public Model(String fileName) {
		map = fileName;
		enemyList = new CopyOnWriteArrayList<Enemy>();
		try {
			levelMap = new Map(fileName, enemyList);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		System.out.println(enemyList.size());
		p = new ShortestPathFinder(levelMap);
		mapSize = (double) levelMap.getSize();
		player = new Player((levelMap.getSpawnX() + 0.5), (levelMap.getSpawnY() + 0.5));

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
						dY = s.getY() + y + 0.5;

						if (legalLocation(dX, dY)) {
							legal = true;
							break;
						}

						dY = s.getY() - y + 0.5;

						if (legalLocation(dX, dY)) {
							legal = true;
							break;
						}
						dX = s.getX() - x + 0.5;
						dY = s.getY() + y + 0.5;

						if (legalLocation(dX, dY)) {
							legal = true;
							break;
						}

						dY = s.getY() - y + 0.5;

						if (legalLocation(dX, dY)) {
							legal = true;
							break;
						}
						dX = s.getX() + 0.5;
						dY = s.getY() + y + 0.5;

						if (legalLocation(dX, dY)) {
							legal = true;
							break;
						}

						dY = s.getY() - y + 0.5;

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
		movePlayer();
		if (time >= timeLimit) {
			player.damage(100000);
			this.gameOver = true;
		}
	}

	public void moveEnemies() {
		for (Enemy e : enemyList) {
			if (!levelMap.getTile((int) (e.getX()), (int) (e.getY())).reachable()) {
				e.setHeading(-1);
				continue;
			}
			
//			System.out.println(e.getX()+" "+player.getX());
			
			ResultTuple r = p.findPath(e.getX(), e.getY(), player.getX(), player.getY(), e.diameter);
			if (r.distance > 10) {
				e.setHeading(-1);
				continue;
			}

			double heading = e.getHeading();
			if (heading == -1) {
				if (r.distance > 0) {
					System.out.println(r.direction);
					heading = Math.PI/2 - r.direction * Math.PI / 4;
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

			e.move(e.getX() + dx, e.getY() + dy);

			if (distance(e.getX(), player.getX(), e.getY(), player.getY()) < e.diameter) {
				gameOver = gameOver || player.damage(e.getHealth());
				if (e instanceof Ghost) {
					((Ghost) e).getParent().decrementCount();
				}
				enemyList.remove(e);

			}

		}
	}

	public boolean collides(double x, double y) {

		Tile t[] = new Tile[4];

		t[0] = levelMap.getTile((int) (x + 0.5), (int) (y - 0.5));
		t[1] = levelMap.getTile((int) (x + 0.5), (int) (y + 0.5));
		t[2] = levelMap.getTile((int) (x - 0.5), (int) (y - 0.5));
		t[3] = levelMap.getTile((int) (x - 0.5), (int) (y + 0.5));
		State current = null;
		if (ai instanceof HierarchicalAI)
			current = ((LinkedList<State>) history).getLast();

		for (int i = 0; i < 4; i++) {
			if (t[i].getSolid()) {
				if (t[i] instanceof Door) {
					if (ai instanceof HierarchicalAI)
						current.reachedDoor();
					if (ai instanceof MaxQQ_AI) {
						((MaxQQ_AI) ai).reachedDoor();
					}
					if (player.useKey()) {
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
			if (t[i] instanceof Key) {
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
				if (ai instanceof HierarchicalAI)
					current.win();
				gameOver = true;
				if (ai instanceof MaxQQ_AI) {
					((MaxQQ_AI) ai).reachedExit();
				}
			}

		}

		return false;
	}

	public void movePlayer() {
		if (ai.getHeading() < 0 || ai.shoot())
			return;

		double x = player.getX();
		double y = player.getY();
		double distance = player.speed;
		double dx = Math.cos(ai.getHeading()) * distance;
		double dy = -Math.sin(ai.getHeading()) * distance;
		x += dx;
		y += dy;

		if (!collides(x, y)) {
			player.move(x, y);
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
		double y1 = b.getX();
		double x2 = x1 + (.5 / mapSize * Math.cos(b.getHeading()));
		double y2 = y1 - (.5 / mapSize * Math.sin(b.getHeading()));

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
			for (Enemy e : enemyList) {
				if (Math.abs(bulletDistance(e.getX(), e.getY())) < 2) {
					System.out.println("Hit!");
					if (e.hit()) {
						enemyList.remove(e);
						hit = true;
					}
					destroyed = true;
					continue;
				}
			}
			for (Tile[] ts : levelMap.getTileMap()) {
				for (Tile t : ts) {
					if (t.getSolid() && bulletDistance(t.getX(),t.getY())<1) {
						//System.out.println("test");
						if (t instanceof Spawner) {

							if (((Spawner) t).damage()) {

								levelMap.destroyTile(t.getX(), t.getY());
							}
							hit = true;
						}
						destroyed = true;
					}
				}
			}
			if (destroyed)
				updateHistoryBullet(bulletTime, hit);
			b = null;
		}
		if (ai.shoot() && ai.getHeading() != -1 && player.shoot()) {
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
		gameOver = false;
		enemyList.clear();
		b = null;
		try {
			levelMap = new Map(map, enemyList);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		player = new Player((levelMap.getSpawnX() + 0.5), (levelMap.getSpawnY() + 0.5));
	}

}

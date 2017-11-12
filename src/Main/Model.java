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

public class Model {

	Random r = new Random();
	Map levelMap;
	CopyOnWriteArrayList<Enemy> enemyList;
	Player player;
	double mapSize;
	CopyOnWriteArrayList<Bullet> bullets;
	boolean gameOver = false;
	ShortestPathFinder p;
	Queue<State> history;
	AI ai;
	
	public Model(String fileName) {
		enemyList = new CopyOnWriteArrayList<Enemy>();
		try {
			levelMap = new Map(fileName, enemyList);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		System.out.println(enemyList.size());
		p = new ShortestPathFinder(levelMap);
		bullets = new CopyOnWriteArrayList<Bullet>();
		mapSize = (double) levelMap.getSize();
		player = new Player((levelMap.getSpawnX() + 0.5) / mapSize, (levelMap.getSpawnY() + 0.5) / mapSize);

	}
	
	
	public void setHistory(Queue<State> history) {
		this.history=history;
	}

	public double distance(double x1, double x2, double y1, double y2) {

		return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
	}

	public boolean colidesWithEnemy(double x, double y) {

		for (Enemy e : enemyList) {
			if (distance(e.getX(), x, e.getY(), y) < e.diameter / mapSize) {
				return true;
			}
		}

		return false;
	}

	public boolean legalLocation(double x, double y) {
		return (x > 0 && y > 0 && x < mapSize && y < mapSize && !colidesWithEnemy((x) / mapSize, (y) / mapSize)
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
					enemyList.add(new Ghost((dX) / mapSize, (dY) / mapSize, s));
			}
		}
	}

	public void updatePlayer() {
		player.tick();
		movePlayer();
	}

	public void moveEnemies() {
		for (Enemy e : enemyList) {
			if (!levelMap.getTile((int) (e.getX() * levelMap.getSize()), (int) (e.getY() * levelMap.getSize()))
					.reachable()) {
				e.setHeading(-1);
				continue;
			}

			ResultTuple r = p.findPath(e.getX(), e.getY(), player.getX(), player.getY(), e.diameter);
			if (r.distance > 20) {
				e.setHeading(-1);
				continue;
			}

			double heading = e.getHeading();
			if (heading == -1) {
				if (r.distance > 0) {
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

			double distance = e.getSpeed() / mapSize;
			double dx = Math.cos(heading) * distance;
			double dy = -Math.sin(heading) * distance;

			e.move(e.getX() + dx, e.getY() + dy);

			if (distance(e.getX(), player.getX(), e.getY(), player.getY()) < e.diameter / mapSize) {
				gameOver = player.damage(e.getHealth());
				if (e instanceof Ghost) {
					((Ghost) e).getParent().decrementCount();
				}
				enemyList.remove(e);

			}

		}
	}

	public boolean collides(double x, double y) {

		Tile t[] = new Tile[4];

		t[0] = levelMap.getTile((int) (x * mapSize + 0.5), (int) (y * mapSize - 0.5));
		t[1] = levelMap.getTile((int) (x * mapSize + 0.5), (int) (y * mapSize + 0.5));
		t[2] = levelMap.getTile((int) (x * mapSize - 0.5), (int) (y * mapSize - 0.5));
		t[3] = levelMap.getTile((int) (x * mapSize - 0.5), (int) (y * mapSize + 0.5));

		for (int i = 0; i < 4; i++) {
			if (t[i].getSolid()) {
				if (t[i] instanceof Door && player.useKey()) {
					((Door) t[i]).open();
					if (levelMap.getTile(t[i].getX() + 1, t[i].getY()) instanceof Door) {
						((Door) levelMap.getTile(t[i].getX() + 1, t[i].getY())).open();
					}
					if (levelMap.getTile(t[i].getX() - 1, t[i].getY()) instanceof Door) {
						((Door) levelMap.getTile(t[i].getX() - 1, t[i].getY())).open();
					}
					if (levelMap.getTile(t[i].getX(), t[i].getY() - 1) instanceof Door) {
						((Door) levelMap.getTile(t[i].getX(), t[i].getY() - 1)).open();
					}
					if (levelMap.getTile(t[i].getX(), t[i].getY() + 1) instanceof Door) {
						((Door) levelMap.getTile(t[i].getX(), t[i].getY() + 1)).open();
					}
					levelMap.floodFillReachable(t[i].getX(), t[i].getY());

				} else {
					return true;
				}
			}
			if (t[i] instanceof Key) {
				player.addKey();
				levelMap.destroyTile(t[i].getX(), t[i].getY());
			}

			if (t[i] instanceof Health) {
				player.damage(-((Health) t[i]).health);
				levelMap.destroyTile(t[i].getX(), t[i].getY());
			}
			if (t[i] instanceof Exit) {
				gameOver=true;
			}

		}

		return false;
	}

	public void movePlayer() {

		if (ai.getHeading() < 0 || ai.shoot())
			return;

		double x = player.getX();
		double y = player.getY();
		double distance = player.speed / mapSize;
		double dx = Math.cos(ai.getHeading()) * distance;
		double dy = -Math.sin(ai.getHeading()) * distance;
		x += dx;
		y += dy;

		if (!collides(x, y)) {
			player.move(x, y);
		}

	}
	
	private void updateHistoryBullet(int t, boolean hit){
		
		if(history==null)return;
		for(State s:((LinkedList<State>)history)){
			if(s.turnTime==t){
				s.bulletHit(hit);
				continue;
			}
		}
		
	}

	public void updateBullets(int time) {

		for (Bullet b : bullets) {
			b.move();
			int bulletTime=b.getSpawnTime();
			boolean destroyed=false;
			boolean hit=false;
			for (Enemy e : enemyList) {
				if (distance(e.getX(), b.getX(), e.getY(), b.getY()) < e.diameter / mapSize) {
					if (e.hit()) {
						enemyList.remove(e);
						hit=true;
					}
					destroyed=true;
					bullets.remove(b);
					continue;
				}
			}
			Tile t = levelMap.getTile((int) (b.getX() * mapSize), (int) (b.getY() * mapSize));
			if (t.getSolid()) {
				if (t instanceof Spawner) {

					if (((Spawner) t).damage()) {

						levelMap.destroyTile(t.getX(), t.getY());
					}
					hit=true;
				}
				destroyed=true;
				bullets.remove(b);
			}
			if(destroyed)updateHistoryBullet(bulletTime,hit);

		}

		if (ai.shoot() && ai.getHeading() != -1 && player.shoot()) {
			Bullet b = new Bullet(mapSize, player.getX(), player.getY(), ai.getHeading(),time);
			bullets.add(b);
		}
	}

	public CopyOnWriteArrayList<Bullet> getBullets() {
		return bullets;
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

	

}

package Main;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import Assets.*;
import mapTiles.*;

public class Model {

	Random r = new Random();
	Map levelMap;
	CopyOnWriteArrayList<Enemy> enemyList;
	Player player;
	double mapSize;
	CopyOnWriteArrayList<Bullet> bullets;
	boolean gameOver = false;

	public Model(String fileName) {
		try {
			levelMap = new Map(fileName);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		enemyList = new CopyOnWriteArrayList<Enemy>();
		bullets = new CopyOnWriteArrayList<Bullet>();
		mapSize = (double) levelMap.getSize();
		player = new Player((levelMap.getSpawnX() + 0.5) / mapSize, (levelMap.getSpawnY() + 0.5) / mapSize);

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
					enemyList.add(new Enemy((dX) / mapSize, (dY) / mapSize, s));
			}
		}
	}

	public void updatePlayer() {
		player.tick();
		movePlayer();
	}

	public boolean collides(double x, double y) {

		Tile t[] = new Tile[4];

		t[0] = levelMap.getTile((int) (x * mapSize + 0.5), (int) (y * mapSize - 0.5));
		t[1] = levelMap.getTile((int) (x * mapSize + 0.5), (int) (y * mapSize + 0.5));
		t[2] = levelMap.getTile((int) (x * mapSize - 0.5), (int) (y * mapSize - 0.5));
		t[3] = levelMap.getTile((int) (x * mapSize - 0.5), (int) (y * mapSize + 0.5));

		for (Enemy e : enemyList) {
			if (distance(e.getX(), x, e.getY(), y) < e.diameter / mapSize) {
				gameOver = player.damage(e.getHealth());
				e.getParent().decrementCount();
				enemyList.remove(e);
			}
		}

		for (int i = 0; i < 4; i++) {
			if (t[i].getSolid()){
				if(t[i] instanceof Door && player.useKey()){
					((Door)t[i]).open();
					if(levelMap.getTile(t[i].getX()+1, t[i].getY()) instanceof Door){
						((Door)levelMap.getTile(t[i].getX()+1, t[i].getY())).open();
					}
					if(levelMap.getTile(t[i].getX()-1, t[i].getY()) instanceof Door){
						((Door)levelMap.getTile(t[i].getX()-1, t[i].getY())).open();
					}
					if(levelMap.getTile(t[i].getX(), t[i].getY()-1) instanceof Door){
						((Door)levelMap.getTile(t[i].getX(), t[i].getY()-1)).open();
					}
					if(levelMap.getTile(t[i].getX(), t[i].getY()+1) instanceof Door){
						((Door)levelMap.getTile(t[i].getX(), t[i].getY()+1)).open();
					}
					
					
				}else{
					return true;
				}
			}
			if(t[i] instanceof Key){
				player.addKey();
				levelMap.destroyTile(t[i].getX(), t[i].getY());
			}
		}

		return false;
	}

	public void movePlayer() {
		double x = player.getX();
		double y = player.getY();
		double distance = player.speed / mapSize;
		double dx = Math.cos(player.getHeading()) * distance;
		double dy = -Math.sin(player.getHeading()) * distance;
		x += dx;
		y += dy;

		if (!collides(x, y)) {
			player.move(x, y);
		}

	}

	public void updateBullets() {

		for (Bullet b : bullets) {
			b.move();
			for (Enemy e : enemyList) {
				if (distance(e.getX(), b.getX(), e.getY(), b.getY()) < e.diameter / 2 / mapSize) {
					if (e.hit()) {
						enemyList.remove(e);
					}

					bullets.remove(b);
					continue;
				}
			}
			Tile t = levelMap.getTile((int) (b.getX() * mapSize), (int) (b.getY() * mapSize));
			if (t.getSolid()) {
				if (t instanceof Spawner) {

					if (((Spawner) t).damage()) {

						levelMap.destroyTile(t.getX(),t.getY());
					}
				}
				bullets.remove(b);
			}

		}

//		if (player.shoot()) {
//			Bullet b = new Bullet(mapSize, player.getX(), player.getY(), player.getHeading());
//			bullets.add(b);
//			player.setHeading(player.getHeading() + Math.PI / 48);
//		}
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

}

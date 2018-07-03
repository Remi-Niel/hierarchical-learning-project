package mapTiles;

import java.awt.Color;

public class Spawner extends Tile {
	private int health;
	final int spawnTime = 100;
	final int spawnLimit = 3;
	int timer;
	int spawnCount;

	public Spawner(int x, int y) {
		super(Color.MAGENTA, true, x, y);
		this.x = x;
		this.y = y;
		timer = 0;
		spawnCount = 0;
		health = 1;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public boolean spawn() {
//		System.out.println(spawnCount);
		timer--;
		if (timer <= 0) {
			if (spawnCount < spawnLimit) {
				timer = spawnTime;
				spawnCount++;
				return true;
			}
		}
		return false;
	}

	public void decrementCount() {
		spawnCount--;
		// System.out.println("Count: "+spawnCount);
	}

	public boolean damage() {
		return (--health <= 0);
	}

}

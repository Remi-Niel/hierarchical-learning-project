package mapTiles;

import java.awt.Color;

public class Spawner extends Tile {

	final int spawnTime=50;
	final int spawnLimit=10;
	int timer;
	int x,y;
	int spawnCount;
	
	public Spawner(int x, int y) {
		super(Color.MAGENTA, true);
		this.x=x;
		this.y=y;
		timer=spawnTime;
		spawnCount=0;
	}
	
	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public boolean spawn(){
		//System.out.println(spawnCount);
		if(--timer<=0 ){
			timer=spawnTime;
			return spawnCount++ < spawnLimit;
		}
		return false;
	}
	
	public void decrementCount(){
		spawnCount--;
	}
	
	

}

package mapTiles;

import java.awt.Color;

public class Spawner extends Tile {
	private int health;
	final int spawnTime=75;
	final int spawnLimit=24;
	int timer;
	int x,y;
	int spawnCount;
	
	public Spawner(int x, int y) {
		super(Color.MAGENTA, true);
		this.x=x;
		this.y=y;
		timer=spawnTime;
		spawnCount=0;
		health=3;
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
	
	public boolean damage(){
		return (--health<=0);
	}
	
	

}

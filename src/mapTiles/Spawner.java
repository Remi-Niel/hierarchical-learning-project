package mapTiles;

import java.awt.Color;

public class Spawner extends Tile {
	private int health;
	final int spawnTime=200;
	final int spawnLimit=6;
	int timer;
	int spawnCount;
	
	public Spawner(int x, int y) {
		super(Color.MAGENTA, true,x,y);
		this.x=x;
		this.y=y;
		timer=25;
		spawnCount=0;
		health=8;
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
			if(spawnCount<spawnLimit)
				spawnCount++;
				return true;
		}
		return false;
	}
	
	public void decrementCount(){
		spawnCount--;
		//System.out.println("Count: "+spawnCount);
	}
	
	public boolean damage(){
		return (--health<=0);
	}
	
	

}

package mapTiles;

import java.awt.Color;

public class Spawner extends Tile {
	int timer;
	final int spawnTime=50;
	public Spawner() {
		super(Color.RED, true);
		timer=spawnTime;
	}
	
	public boolean spawn(){
		if(--timer<=0){
			timer=spawnTime;
			return true;
		}
		return false;
	}

}

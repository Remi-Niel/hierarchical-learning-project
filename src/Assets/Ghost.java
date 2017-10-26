package Assets;

import mapTiles.Spawner;

public class Ghost extends Enemy {
	
	private final static int startingHealth=6;
	private final static double speed=0.08;
	private Spawner parent;
	
	
	public Ghost(double x, double y, Spawner s) {
		super(x, y,startingHealth,speed);
		parent=s;
		
	}
	
	
	public boolean hit(){
		if(--health<=0){
			parent.decrementCount();
			return true;
		}
		return false;
	}
	
	public Spawner getParent(){
		return parent;
	}
}

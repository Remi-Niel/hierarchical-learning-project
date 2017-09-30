package Assets;

import mapTiles.Spawner;

public class Enemy {
	
	public final double diameter =0.95;
	final int startingHealth=5;
	Spawner parent;
	int health;
	double x,y;
	
	
	public Enemy(double x, double y, Spawner s) {
		//System.out.println(x +" "+y);
		this.x=x;
		this.y=y;
		health=startingHealth;
		parent=s;
	}

	public boolean hit(){
		if(--health<=0){
			parent.decrementCount();
			return true;
		}
		return false;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}
	
	public Spawner getParent(){
		return parent;
	}
	
	public int getHealth(){
		return health;
	}
}

package Assets;

import mapTiles.Spawner;

public abstract class Enemy {
	
	public final double diameter =0.95;
	int health;
	private double speed;
	double x,y;
	
	
	public Enemy(double x, double y, int startingHealth, double speed) {
		//System.out.println(x +" "+y);
		this.x=x;
		this.y=y;
		health=startingHealth;
		this.setSpeed(speed);
	}

	public boolean hit(){
		if(--health<=0){
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
	
	public void move(double x,double y){
		this.x=x;
		this.y=y;
	}

	
	public int getHealth(){
		return health;
	}

	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}
}

package Assets;

import mapTiles.Spawner;

public abstract class Enemy {
	
	final int stepSize=5;
	public final double diameter =0.9;
	int health;
	private double speed;
	double heading;
	double x,y;
	int steps;
	
	
	public Enemy(double x, double y, int startingHealth, double speed) {
		//System.out.println(x +" "+y);
		this.x=x;
		this.y=y;
		health=startingHealth;
		this.setSpeed(speed);
		heading=-1;
	}

	public double getHeading() {
		return heading;
	}

	public void setHeading(double heading) {
		this.heading = heading;
		steps=stepSize;
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
		if(--steps<=0){
			heading=-1;
		}
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

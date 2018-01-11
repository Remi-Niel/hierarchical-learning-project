package Assets;

public class Bullet {
	
	private final double speed;
	private double x,y;
	private final double heading; //radians
	private boolean destroyed=false;
	private int spawnTime;
	private double wallDistance;
	


	public double getWallDistance() {
		return wallDistance;
	}

	public void setWallDistance(double wallDistance) {
		this.wallDistance = wallDistance;
	}

	public int getSpawnTime() {
		return spawnTime;
	}

	public Bullet(double mapSize,double x,double y, double heading, int spawnTime) {
		speed=1/(3);
		this.x=x + (.5/mapSize*Math.cos(heading));
		this.y=y - (.5/mapSize*Math.sin(heading));
		this.heading=heading;
		this.spawnTime=spawnTime;
	}
	
	public void move(){
		x += Math.cos(heading)*speed;
		y -= Math.sin(heading)*speed;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double getHeading() {
		return heading;
	}
	
	public boolean destroyed() {
		return destroyed;
	}

	public void destroy() {
		this.destroyed = true;
	}
}

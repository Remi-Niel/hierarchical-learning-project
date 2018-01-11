package Assets;

public class Player {
	
	public final double speed=1;
	private final int reloadTime=8;
	private long lastFire=reloadTime;
	private int health;
	private double x,y;
	private double heading; //radians
	private int keys;
	private boolean trigger;
	
	public Player(double x, double y) {
		health = 100;
		this.x=x;
		this.y=y;
		heading=-1;
		keys=0;
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public double getHeading() {
		return heading;
	}

	public void setHeading(double heading) {
		this.heading = heading%(2*Math.PI);
	}

	public int getHealth() {
		return health;
	}
	
	public boolean damage(int damage){
		health-= 2*damage;
		//System.out.println("health: "+health);
		return (health<=0);
	}
	
	public void move(double x,double y){
		this.x=x;
		this.y=y;
	}
	
	public boolean loaded(){
		return lastFire>=reloadTime;
	}
	
	public boolean shoot(){
		if(lastFire>=reloadTime){
			lastFire=0;
			return true;
		}
		return false;
	}
	public void tick(){
		lastFire++;
	}
	public boolean useKey(){
		if(keys>0){
			keys--;
			return true;
		}
		return false;
	}
	public int getKeys(){
		return keys;
	}
	
	public void addKey(){
		keys++;
	}

}

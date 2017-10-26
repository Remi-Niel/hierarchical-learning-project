package Assets;

public class Grunt extends Enemy {
	private final static int startingHealth=10;
	private final static double speed=0.04;
	
	public Grunt(double x, double y) {
		super(x, y,startingHealth,speed);
	}

}

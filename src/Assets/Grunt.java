package Assets;

public class Grunt extends Enemy {
	private final static int startingHealth=20;
	private final static double speed=0.09;
	
	public Grunt(double x, double y) {
		super(x, y,startingHealth,speed);
	}

}

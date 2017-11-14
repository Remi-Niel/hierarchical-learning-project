package AI;


public interface AI  {
	
	public void determineAction(int time);
	
	public double getHeading();
	
	public boolean shoot();	
	
	public void reset(boolean train);

}

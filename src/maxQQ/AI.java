package maxQQ;


public interface AI  {
	
	public void determineAction(int time,boolean b);
	
	public double getHeading();
	
	public boolean shoot();	
	
	public void reset(boolean train);

}

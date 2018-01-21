package AI;


public interface AI  {

	public double getHeading();
	
	public boolean shoot();	
	
	public void reset(boolean train,int time);

	public void determineAction(int time, boolean b);

	public double getScore();

	public void save(String fileName, int t, int e);

	public void load(String s);

}

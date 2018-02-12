package AI;

public class State {
	public double[] rewards=new double[13];
	public int action;
	public int behaviour;
	public double[] input;
	public int turnTime;
	public boolean flyingBullet;
	public double distanceDoor;
	public double distanceKey;
	public double distanceExit;
	
	
	public State(int time,double[] input, int action, int behaviour){
		turnTime=time;
		flyingBullet=false;
		this.input=input;
		this.action=action;
		this.behaviour=behaviour;
	}
	
	public void bulletHit(boolean hit){
		flyingBullet=false;
		if(hit)rewards[5]++;
	}
	
	public void gotKey(){
//		System.out.println("Got key");
		rewards[2]++;
	}
	
	public void reachedDoor(){
//		System.out.println("reached door");
		rewards[1]++;
	}
	
	public void openedDoor(){
//		System.out.println("opened door");
		rewards[3]++;
	}
	
	public void health(){
		rewards[4]++;
	}
	
	public void damagedEnemy(){
		rewards[5]++;
	}
	
	public void death(){
		rewards[6]++;
	}
	
	public void damaged(){
		rewards[7]++;
	}
	
	public void win(){
//		System.out.println("reached exit");
		rewards[0]++;
	}
	
	public void hitWall(){
//		System.out.println("Hit wall!");
		rewards[11]++;
	}
	
	public void targetNotReachable(){
		rewards[12]++;
	}
	
	
}

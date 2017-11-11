package AI;

public class State {
	int[] rewards=new int[8];
	int action;
	double[] input;
	int turnTime;
	boolean flyingBullet;
	
	
	public State(int time,boolean shot,double[] input, int a){
		turnTime=time;
		flyingBullet=shot;
		this.input=input;
		action=a;
	}
	
	public void bulletHit(boolean hit){
		flyingBullet=false;
		if(hit)rewards[5]++;
	}
	
	public void gotKey(){
		rewards[2]++;
	}
	
	public void reachedDoor(){
		rewards[1]++;
	}
	
	public void openedDoor(){
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
	
	
	
	
}

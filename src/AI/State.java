package AI;

public class State {
	public double[] rewards=new double[11];
	public int action;
	public int behaviour;
	public double[] input;
	public int turnTime;
	public boolean flyingBullet;
	
	
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
	
	public void win(){
		rewards[0]++;
	}
	
	
	
	
}

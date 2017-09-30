package Main;

import java.util.Observable;

public class Controller extends Observable{
	
	Model m;
	
	public Controller(View v, Model m){
		this.addObserver(v);
		this.m=m;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void update(){
		//System.out.println("update spawners");
		m.tickSpawners();
		//System.out.println("update player");
		m.updatePlayer();
		//System.out.println("update bullets");
		m.updateBullets();
		//System.out.println("Notify observers");
		setChanged();
		notifyObservers();
			
	}
	
}

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
	
}

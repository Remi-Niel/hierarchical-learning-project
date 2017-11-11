package Main;

import java.awt.Frame;
import java.awt.Panel;
import java.util.Observable;

import AI.*;

public class Controller extends Observable {

	Model m;
	AI ai;

	public Controller(View v, Frame f, Model m) {
		this.addObserver(v);
		this.m = m;
		this.setChanged();
		this.notifyObservers();
		ai = new ManualAI();
		m.setAI(ai);
		if (ai instanceof ManualAI) {
			v.setFocusable(true);
			f.setFocusable(true);
			v.addKeyListener((ManualAI) ai);
			f.addKeyListener((ManualAI) ai);
		}
	}

	public void update(int time) {
		// System.out.println("update spawners");
		m.tickSpawners();
		// System.out.println("update player");
		m.updatePlayer();
		// System.out.println("update bullets");
		m.updateBullets(time);
		// System.out.println("Move enemies");
		m.moveEnemies();
		// System.out.println("Notify observers");
		setChanged();
		notifyObservers();

	}

}

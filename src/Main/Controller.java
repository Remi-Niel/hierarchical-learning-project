package Main;

import java.awt.Frame;
import java.awt.Panel;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Observable;

import AI.*;
import maxQQ.MaxQQ_AI;

public class Controller extends Observable {

	Model m;
	AI ai;
	boolean learn;
	String fileName;
	File dir;

	public Controller(View v, Frame f, Model m, String file) {
		fileName = file;
		this.addObserver(v);
		this.m = m;
		this.setChanged();
		this.notifyObservers();
		// ai=new ManualAI();
		// ai = new HierarchicalAI(m);
		dir = new File("AIs");
		dir.mkdirs();
		ai = new MaxQQ_AI(m);
		if (new File(dir, file).exists()) {
			ai.load(file);
		}
	
		m.setAI(ai);
		((MaxQQ_AI)ai).setModel(m, 0);
		if (ai instanceof ManualAI) {
			v.setFocusable(true);
			f.setFocusable(true);
			v.addKeyListener((ManualAI) ai);
			f.addKeyListener((ManualAI) ai);
		}
		learn = true;
	}

	public void update(int time, boolean b,boolean draw) {
		ai.determineAction(time, b);
		m.playerDamaged = false;
		m.enemyDamaged = false;
		m.enemyDied = false;
		// System.out.println("update player");
		m.updatePlayer(time);

		// System.out.println("update spawners");
		m.tickSpawners();
		// System.out.println("update bullets");
		m.updateBullets(time);
		// System.out.println("Move enemies");
		m.moveEnemies();
		
		if (ai instanceof MaxQQ_AI) {
			((MaxQQ_AI) ai).checkTerminate(time, b);
		}
		
		if (draw) {
			// System.out.println("Notify observers");
			setChanged();
			notifyObservers();
		}
	}

	public boolean gameover() {
		return m.gameOver;
	}

	public void reset(boolean train, int time) {
		ai.reset(train, time);
		m.reset();
		learn = train;
	}

	public void storeAI(int t, int e) throws FileNotFoundException, IOException {
		ai.save(fileName, t, e);

	}

	public boolean load(String string) {
		System.out.println("Attempting to restore snapshot "+string);
		ai.load(string);
		
		
		return true;
	}

}

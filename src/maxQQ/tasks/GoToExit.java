package maxQQ.tasks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import Assets.Player;
import Main.Map;
import Main.Model;
import Neural.NeuralNetwork;
import PathFinder.ResultTuple;
import PathFinder.ShortestPathFinder;
import mapTiles.Exit;
import mapTiles.Tile;
import maxQQ.AbstractAction;
import maxQQ.SubTask;

public class GoToExit extends SubTask {
	boolean reachedExit;
	transient ShortestPathFinder path;

	public GoToExit(AbstractAction[] as, int[] size, int[] inputKey, Model m, int time) {
		super(as, size, inputKey, m, time);
		path = new ShortestPathFinder(m.getLevelMap());
		reachedExit = false;
	}

	public void setModel(Model m, int time) {
		super.setModel(m, time);
		path = new ShortestPathFinder(m.getLevelMap());
	}

	public AbstractAction getSubtask(double[] rawInput, int time, boolean b) {
		// TODO Auto-generated method stub
		AbstractAction sub = super.getSubtask(rawInput, time, b);
		Exit nearestExit = null;
		double distanceExit = Double.MAX_VALUE;
		Map m = model.getLevelMap();
		Tile t;
		Player p = model.getPlayer();
		for (int x = 0; x < m.getSize(); x++) {
			for (int y = 0; y < m.getSize(); y++) {
				t = m.getTile(x, y);

				if (t.reachable()) {
					if (t instanceof Exit) {
						ResultTuple res = path.findPath(p.getX(), p.getY(), t.getX(), t.getY(), .95);
						if (res.distance < distanceExit) {
							distanceExit = res.distance;
							nearestExit = (Exit) t;
						}
					}
				}
			}
		}

		if (sub instanceof Navigate) {
			if (nearestExit != null) {
				// System.out.println("Exit x= "+nearestExit.getX()+" Exit y=
				// "+nearestExit.getY());
				((Navigate) sub).setTarget(nearestExit.getX(), nearestExit.getY(), "exit");
			} else {
				((Navigate) sub).setTarget(-1, -1, "exit");
			}

		}

		return sub;
	}

	@Override
	public boolean finished(double[] input, Model model, int time) {
		if (reachedExit) {
			reachedExit = false;
			return true;
		} else if (input[10] == -1) { // No reachable exit
			// System.out.println("Terminating "+this.getClass()+" no exit
			// reachable " +input[10]);
			currentReward += Math.pow(discountfactor, time - this.lastActionTime) * -1;
			return true;
		}
		return false;
	}

	public void reachedExit() {
		// TODO Auto-generated method stub
		reachedExit = true;
	}
	
	public void save(String fileName, int t, int e) throws IOException {
		File dir=new File("AIs");
		File subDir=new File(dir,t+fileName+e);
		subDir.mkdirs();
		
		File f=new File(subDir,"gotoExit");
		System.out.println("Saved AI to file: "+f.getPath());
		ObjectOutputStream oos=new ObjectOutputStream(new FileOutputStream(f));
		oos.writeObject(this.net);
		oos.flush();
		oos.close();
		
	}
	

	private Object readFromFile(String file)
			throws FileNotFoundException, IOException, ClassNotFoundException {
		File dir=new File("AIs");
		File subFolder = new File(dir, file);
		subFolder.mkdirs();
		File f= new File(subFolder,"gotoExit");
		
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f.getAbsoluteFile()));
		Object o = ois.readObject();
		ois.close();
		return o;
	}


	public void load(String fileName) {
		try {
			this.net=(NeuralNetwork) readFromFile(fileName);
		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}

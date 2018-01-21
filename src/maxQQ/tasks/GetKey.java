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
import mapTiles.Key;
import mapTiles.Tile;
import maxQQ.AbstractAction;
import maxQQ.SubTask;

public class GetKey extends SubTask {
	boolean gotKey;
	transient ShortestPathFinder path;

	public GetKey(AbstractAction[] as, int[] size, int[] inputKey, Model m, int time) {
		super(as, size, inputKey, m, time);
		path = new ShortestPathFinder(m.getLevelMap());
		gotKey = false;
		// TODO Auto-generated constructor stub
	}

	public void setModel(Model m, int time) {
		super.setModel(m, time);
		path = new ShortestPathFinder(m.getLevelMap());
	}

	public AbstractAction getSubtask(double[] rawInput, int time, boolean b) {
		// TODO Auto-generated method stub
		AbstractAction sub = super.getSubtask(rawInput, time, b);
		Key nearestKey = null;
		double distanceKey = Double.MAX_VALUE;
		Map m = model.getLevelMap();
		Tile t;
		Player p = model.getPlayer();
		for (int x = 0; x < m.getSize(); x++) {
			for (int y = 0; y < m.getSize(); y++) {
				t = m.getTile(x, y);

				if (t.reachable()) {
					if (t instanceof Key) {
						ResultTuple res = path.findPath(p.getX(), p.getY(), t.getX(), t.getY(), .95);
						if (res.distance < distanceKey) {
							distanceKey = res.distance;
							nearestKey = (Key) t;
						}
					}
				}
			}
		}

		if (sub instanceof Navigate) {
			if (nearestKey != null) {
				((Navigate) sub).setTarget(nearestKey.getX(), nearestKey.getY(), "key");
			} else {
				((Navigate) sub).setTarget(-1, -1, "key");
			}

		}

		return sub;
	}

	@Override
	public boolean finished(double[] input, Model model, int time) {
		if (gotKey) {
			gotKey = false;
			currentReward+= Math.pow(discountfactor, time - this.lastActionTime);
			return true;
		} else if (input[0] == -1) {// No reachable key
			// System.out.println("Terminating "+this.getClass()+" no key
			// reachable " +input[0]);
			currentReward += Math.pow(discountfactor, time - this.lastActionTime) * -1;
			return true;
		}
		return false;
	}

	public void gotKey() {
		gotKey = true;
	}

	public void save(String fileName, int t, int e) throws IOException {
		File dir = new File("AIs");
		File subDir = new File(dir, t + fileName + e);
		subDir.mkdirs();

		File f = new File(subDir, "getKey");
		System.out.println("Saved AI to file: " + f.getPath());
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f));
		oos.writeObject(this.net);
		oos.flush();
		oos.close();

	}

	private Object readFromFile(String file) throws FileNotFoundException, IOException, ClassNotFoundException {
		File dir = new File("AIs");
		File subFolder = new File(dir, file);
		subFolder.mkdirs();
		File f = new File(subFolder, "getKey");

		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f.getAbsoluteFile()));
		Object o = ois.readObject();
		ois.close();
		return o;
	}

	public void load(String fileName) {
		try {
			this.net = (NeuralNetwork) readFromFile(fileName);
		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}

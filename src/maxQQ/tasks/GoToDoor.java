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
import mapTiles.Door;
import mapTiles.Tile;
import maxQQ.AbstractAction;
import maxQQ.SubTask;

public class GoToDoor extends SubTask {

	boolean reached,opened;
	transient ShortestPathFinder path;

	public GoToDoor(AbstractAction[] as, int[] size, int[] inputKey, Model m, int time) {
		super(as, size, inputKey, m, time);
		path = new ShortestPathFinder(m.getLevelMap(),m);
		reached = false;
		opened=false;
		// TODO Auto-generated constructor stub
	}

	public void setModel(Model m, int time) {
		super.setModel(m, time);
		path = new ShortestPathFinder(m.getLevelMap(),m);
	}

	public AbstractAction getSubtask(double[] rawInput, int time, boolean b) {
		// TODO Auto-generated method stub
		reached = false;
		opened=false;
		AbstractAction sub = super.getSubtask(rawInput, time, b);

		Door nearestDoor = null;
		double distanceDoor = Double.MAX_VALUE;
		Map m = model.getLevelMap();
		Tile t;
		Player p = model.getPlayer();
		for (int x = 0; x < m.getSize(); x++) {
			for (int y = 0; y < m.getSize(); y++) {
				t = m.getTile(x, y);

				if (t.reachable()) {
					if (t instanceof Door && t.getSolid()) {
						ResultTuple res = path.findPath(p.getX(), p.getY(), t.getX(), t.getY(), .95);
						if (res.distance < distanceDoor) {
							distanceDoor = res.distance;
							nearestDoor = (Door) t;
						}
					}
				}
			}
		}

		if (sub instanceof Navigate) {
			if (nearestDoor != null) {
				((Navigate) sub).setTarget(nearestDoor.getX(), nearestDoor.getY(), "door");
			} else {
				((Navigate) sub).setTarget(-1, -1, "door");
			}

		}

		return sub;
	}

	@Override
	public boolean finished(double[] input, Model model, int time) {
		if (reached) {
			if(!this.opened){
//				System.out.println("Reached door without key -1");
				currentReward += -1;
			}
			reached = false;
			return true;
		} else if (input[5] == -1) { // No reachable door exists
			// System.out.println("Terminating "+this.getClass()+" no door
			// reachable " +input[5]);
			currentReward += Math.pow(discountfactor, time - this.lastActionTime) * -1;
			return true;
		}
		return false;
	}

	public void reachedDoor(boolean opened) {
		// TODO Auto-generated method stub
		reached = true;
		this.opened=opened;
	}

	public void save(String fileName, int t, int e) throws IOException {
		File dir = new File("AIs");
		File subDir = new File(dir, t + fileName + e);
		subDir.mkdirs();

		File f = new File(subDir, "gotoDoor");
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
		File f = new File(subFolder, "gotoDoor");

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
			System.err.println("File does not exist");
			e.printStackTrace();
			System.exit(2);
		}

	}
}

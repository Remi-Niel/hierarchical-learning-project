package maxQQ.tasks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;

import Main.Model;
import Neural.NeuralNetwork;
import PathFinder.ShortestPathFinder;
import maxQQ.AbstractAction;
import maxQQ.SubTask;

public class Navigate extends SubTask {
	int targetX, targetY;
	double previousDistance;
	int[] key;
	transient ShortestPathFinder path;
	public String target;
	boolean done;

	public Navigate(AbstractAction[] as, int[] size, int[] inputKey, Model m, int time) {
		super(as, size, inputKey, m, time);
		// TODO Auto-generated constructor stub
		key = inputKey;
		path = new ShortestPathFinder(m.getLevelMap(), m);
	}

	public void setModel(Model m, int time) {
		super.setModel(m, time);
		path = new ShortestPathFinder(m.getLevelMap(), m);
	}

	public void setTarget(int targetX, int targetY, String type) {
		target = type;
		super.inputKey = key.clone();
		if (type.equals("door")) {
			for (int i = 0; i < 5; i++) {
				super.inputKey[i] += 5;
			}
		} else if (type.equals("exit")) {
			for (int i = 0; i < 5; i++) {
				super.inputKey[i] += 10;
			}
		}
		this.targetX = targetX;
		this.targetY = targetY;
		previousDistance = path.findPath(model.getPlayer().getX(), model.getPlayer().getX(), targetX, targetY,
				0.95).distance;

		// System.out.print(targetX + ", " + targetY + ", " + type + ": ");
		// for (int i = 0; i < key.length; i++) {
		// System.out.print(super.inputKey[i] + " ");
		// }
		// System.out.println("");
	}

	@Override
	public boolean finished(double[] input, Model model, int time) {
		// TODO Auto-generated method stub
		// System.out.print("Input: ");
		// for(int i=0;i<input.length;i++){
		// System.out.print(input[i]+" ");
		// }
		// System.out.println("");
//		System.out.println(target);

		if (model.playerDamaged) {
			currentPseudoReward -= 2 * Math.pow(discountfactor, time - this.lastActionTime);
		}
		if (model.gameOver && model.getPlayer().getHealth() <= 0) {
			currentPseudoReward -= 5 * Math.pow(discountfactor, time - this.lastActionTime);
		}

		if (done) {
			// System.out.println("Reached "+target);
			currentPseudoReward += Math.pow(discountfactor, time - this.lastActionTime) * 5;
			done = false;
			return true;
		}

		double distance = path.findPath(model.getPlayer().getX(), model.getPlayer().getY(), targetX, targetY,
				0.95).distance;
//		System.out.println("NAVIGATE:" + targetX + " " + targetY + " " + model.getPlayer().getX() + " "
//				+ model.getPlayer().getY() + " " + distance);

		if (previousDistance > distance) {
			currentPseudoReward += 0 * Math.pow(discountfactor, time - this.lastActionTime);
		} else {
			currentPseudoReward -= 1 * Math.pow(discountfactor, time - this.lastActionTime);
		}
		previousDistance = distance;
		// currentReward += Math.pow(discountfactor, time - this.startTime) *
		// -.5;
		return false;
	}

	public void save(String fileName, int t, int e) throws IOException {
		File dir = new File("AIs");
		File subDir = new File(dir, t + fileName + e);
		subDir.mkdirs();

		File f = new File(subDir, "navigate");
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
		File f = new File(subFolder, "navigate");

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

	public void reachedTarget() {
		done = true;

	}

	public void walkedIntoWall(int time) {
		currentPseudoReward -= 5 * Math.pow(discountfactor, time - this.lastActionTime);
	}
}

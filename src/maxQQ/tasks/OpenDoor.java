package maxQQ.tasks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import Main.Model;
import Neural.NeuralNetwork;
import maxQQ.AbstractAction;
import maxQQ.SubTask;

public class OpenDoor extends SubTask {

	private boolean openedDoor = false;

	public OpenDoor(AbstractAction[] as, int[] size, int[] inputKey, Model m, int time) {
		super(as, size, inputKey, m, time);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean finished(double[] input, Model model, int time) {
		// Opened a door
		if (isOpenedDoor()) {
			System.out.println("Opened the door");
			currentReward+=Math.pow(discountfactor, time - this.lastActionTime);
			openedDoor = false;
			return true;
		} else if (input[5] == -1) { // No reachable door exists
			//System.out.println("Terminating "+this.getClass()+" no door reachable " +input[5]);
			currentReward += Math.pow(discountfactor, time - this.lastActionTime) * -1;
			return true;
		}
		return false;
	}

	public boolean isOpenedDoor() {
		return openedDoor;
	}

	public void setOpenedDoor(boolean openedDoor) {
		this.openedDoor = openedDoor;
	}

	public void save(String fileName, int t, int e) throws IOException {
		File dir = new File("AIs");
		File subDir = new File(dir, t + fileName + e);
		subDir.mkdirs();

		File f = new File(subDir, "openDoor");
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
		File f = new File(subFolder, "openDoor");

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

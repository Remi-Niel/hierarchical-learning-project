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

public class Combat extends SubTask {

	public Combat(AbstractAction[] as, int[] size, int[] inputKey, Model m, int time) {
		super(as, size, inputKey, m, time);
	}

	@Override
	public boolean finished(double[] input, Model model, int time) {
		if (model.enemyDied) {
			this.currentReward += Math.pow(discountfactor, time - this.lastActionTime) * 1;
			return true;
		}
		boolean flag = true;
		for (int i = 16; i < 49; i++) {
			if (input[i] > 0) {
				flag = false;
				break;
			}
		}
		if (flag) {
			this.currentReward -= Math.pow(discountfactor, time - this.lastActionTime) * 10;
			return true;
		}

		return false;
	}

	public void save(String fileName, int t, int e) throws IOException {
		File dir = new File("AIs");
		File subDir = new File(dir, t + fileName + e);
		subDir.mkdirs();

		File f = new File(subDir, "combat");
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
		File f = new File(subFolder, "combat");

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

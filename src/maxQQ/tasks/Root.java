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

public class Root extends SubTask {

	public Root(AbstractAction[] as, int[] size, int[] inputKey, Model m, int time) {
		super(as, size, inputKey, m, time);
	}

	@Override
	public boolean finished(double[] input, Model model, int time) {
		if (model.gameOver && (model.getPlayer().getHealth() > 0)) {
			System.out.println("Win, epsilon= "+this.epsilon+", temp= "+this.temp+", rate:"+this.net.learningRate);
			this.currentReward += Math.pow(discountfactor, time-this.lastActionTime)*10;
			return true;
		}else if(model.gameOver && model.win){
			model.win=false;
			System.out.println("Loss, epsilon= "+this.epsilon+", temp= " + this.temp+", rate:"+this.net.learningRate);
			this.currentReward += Math.pow(discountfactor, time-this.lastActionTime)*-10;
			return true;
		}else if(model.gameOver){
			System.out.println("Draw, epsilon= " + this.epsilon + ", temp= " + this.temp+", rate:"+this.net.learningRate);
			return true;
		}
		return false;
	}

	public void save(String fileName, int t, int e) throws IOException {
		File dir=new File("AIs");
		File subDir=new File(dir,t+fileName+e);
		subDir.mkdirs();
		
		File f=new File(subDir,"root");
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
		File f= new File(subFolder,"root");
		
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
			System.err.println("File does not exist");
			e.printStackTrace();
			System.exit(2);
		}
		
	}



}

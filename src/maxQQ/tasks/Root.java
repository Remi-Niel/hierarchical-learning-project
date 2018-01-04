package maxQQ.tasks;

import Main.Model;
import maxQQ.AbstractAction;
import maxQQ.SubTask;

public class Root extends SubTask {

	public Root(AbstractAction[] as, int[] size, int[] inputKey, Model m, int time) {
		super(as, size, inputKey, m, time);
	}

	@Override
	public boolean finished(double[] input, Model model, int time) {
		if (model.gameOver && (model.getPlayer().getHealth() > 0)) {
			System.out.println("Win, epsilon= "+this.epsilon+", temp= "+this.temp);
			this.currentReward += Math.pow(discountfactor, time-this.lastActionTime)*100;
			return true;
		}else if(model.gameOver){
			System.out.println("Loss, epsilon= "+this.epsilon+", temp= " + this.temp);
			this.currentReward += Math.pow(discountfactor, time-this.lastActionTime)*-100;
			return true;
		}
		return false;
	}



}

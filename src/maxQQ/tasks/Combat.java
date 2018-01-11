package maxQQ.tasks;

import Main.Model;
import maxQQ.AbstractAction;
import maxQQ.SubTask;

public class Combat extends SubTask {

	public Combat(AbstractAction[] as, int[] size, int[] inputKey, Model m, int time) {
		super(as, size, inputKey, m, time);
	}

	@Override
	public boolean finished(double[] input, Model model, int time) {
		if (model.enemyDied) {
			this.currentReward += Math.pow(discountfactor, time - this.lastActionTime) * 5;
			return true;
		}
		boolean flag=true;
		for(int i=16;i<49;i++){
			if(input[i]>0){
				flag=false;
				break;
			}
		}
		if(flag){
			this.currentReward -= Math.pow(discountfactor, time - this.lastActionTime) * 5;
		}
		

		if (model.enemyDamaged) {
			currentPseudoReward += Math.pow(discountfactor, time - this.lastActionTime);
		}
		if (model.playerDamaged) {

			currentPseudoReward -= Math.pow(discountfactor, time - this.lastActionTime);
		}

		return false;
	}

}

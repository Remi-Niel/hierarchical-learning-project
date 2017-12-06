package maxQQ.tasks;

import Main.Model;
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
			openedDoor = false;
			currentReward += Math.pow(discountfactor, time - this.startTime) * 10;
			return true;
		} else if (input[5] == -1) { // No reachable door exists
			System.out.println("Terminating "+this.getClass()+" no door reachable " +input[5]);
			currentReward += Math.pow(discountfactor, time - this.startTime) * -10;
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

}

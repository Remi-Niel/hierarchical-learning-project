package maxQQ.tasks;

import Assets.Player;
import Main.Map;
import Main.Model;
import PathFinder.ResultTuple;
import PathFinder.ShortestPathFinder;
import mapTiles.Door;
import mapTiles.Tile;
import maxQQ.AbstractAction;
import maxQQ.SubTask;

public class GoToDoor extends SubTask {

	boolean reached;
	ShortestPathFinder path;

	public GoToDoor(AbstractAction[] as, int[] size, int[] inputKey, Model m, int time) {
		super(as, size, inputKey, m, time);
		path = new ShortestPathFinder(m.getLevelMap());
		reached = false;
		// TODO Auto-generated constructor stub
	}

	public AbstractAction getSubtask(double[] rawInput, int time,boolean b) {
		// TODO Auto-generated method stub
		AbstractAction sub = super.getSubtask(rawInput, time,b);

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
			}else{
				((Navigate) sub).setTarget(-1, -1, "door");
			}

		}

		return sub;
	}

	@Override
	public boolean finished(double[] input, Model model, int time) {
		if (reached) {
			//System.out.println("Reached door");
			reached = false;
			return true;
		} else if (input[5] == -1) { // No reachable door exists
			//System.out.println("Terminating "+this.getClass()+" no door reachable " +input[5]);
			currentReward += Math.pow(discountfactor, time - this.lastActionTime) * -5;
			return true;
		}
		return false;
	}

	public void reachedDoor() {
		// TODO Auto-generated method stub
		reached = true;
	}

}

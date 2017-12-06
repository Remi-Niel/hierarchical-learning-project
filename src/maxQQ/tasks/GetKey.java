package maxQQ.tasks;

import Assets.Player;
import Main.Map;
import Main.Model;
import PathFinder.ResultTuple;
import PathFinder.ShortestPathFinder;
import mapTiles.Key;
import mapTiles.Tile;
import maxQQ.AbstractAction;
import maxQQ.SubTask;

public class GetKey extends SubTask {
	boolean gotKey;
	ShortestPathFinder path;

	
	public GetKey(AbstractAction[] as, int[] size, int[] inputKey, Model m, int time) {
		super(as, size, inputKey, m, time);
		path = new ShortestPathFinder(m.getLevelMap());
		gotKey=false;
		// TODO Auto-generated constructor stub
	}
	
	public AbstractAction getSubtask(double[] rawInput, int time,boolean b) {
		// TODO Auto-generated method stub
		AbstractAction sub= super.getSubtask(rawInput, time,b);
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
			}else{
				((Navigate) sub).setTarget(-1, -1, "key");
			}

		}

		return sub;
	}

	@Override
	public boolean finished(double[] input, Model model, int time) {
		if (gotKey) {
			gotKey = false;
			currentReward += Math.pow(discountfactor, time - this.startTime) * 10;
			return true;
		} else if (input[0] == -1) {//No reachable key
			//System.out.println("Terminating "+this.getClass()+" no key reachable " +input[0]);
			currentReward += Math.pow(discountfactor, time - this.startTime) * -10;
			return true;
		}
		return false;
	}
	
	public void gotKey(){
		gotKey=true;
	}

}

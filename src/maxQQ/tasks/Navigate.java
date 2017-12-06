package maxQQ.tasks;

import Main.Model;
import PathFinder.ShortestPathFinder;
import maxQQ.AbstractAction;
import maxQQ.SubTask;

public class Navigate extends SubTask {
	int targetX, targetY;
	double previousDistance;
	int[] key;
	ShortestPathFinder path;
	public Navigate(AbstractAction[] as, int[] size, int[] inputKey, Model m, int time) {
		super(as, size, inputKey, m, time);
		// TODO Auto-generated constructor stub
		key=inputKey;
		path=new ShortestPathFinder(m.getLevelMap());
	}
	
	public void setTarget(int targetX,int targetY,String type){
		super.inputKey=key.clone();
		if(type.equals("door")){
			for(int i=0;i<key.length;i++){
				super.inputKey[i]+=5;
			}
		}else if(type.equals("exit")){
			for(int i=0;i<key.length;i++){
				super.inputKey[i]+=10;
			}
		}
		this.targetX=targetX;
		this.targetY=targetY;
		previousDistance=path.findPath(model.getPlayer().getX(), model.getPlayer().getX(), targetX, targetY, 0.95).distance;
		
//		System.out.print(targetX+", "+targetY+", " +type+": ");
//		for(int i=0;i<key.length;i++){
//			System.out.print(super.inputKey[i]+" ");
//		}
	//	System.out.println("");
	}
	
	@Override
	public boolean finished(double[] input, Model model, int time) {
		// TODO Auto-generated method stub
//		System.out.print("Input: ");
//		for(int i=0;i<input.length;i++){
//			System.out.print(input[i]+"   ");
//		}
//		System.out.println("");
		if(((int)model.getPlayer().getX())==targetX && ((int)model.getPlayer().getY())==targetY){
			return true;
		}
		double distance=path.findPath(model.getPlayer().getX(), model.getPlayer().getX(), targetX, targetY, 0.95).distance;
		//System.out.println("NAVIGATE:"+targetX+" "+targetY+" "+model.getPlayer().getX()+" "+model.getPlayer().getY()+" "+distance);
		currentPseudoReward+=Math.pow(discountfactor, time - this.lastActionTime) * previousDistance-distance;
		//System.out.println("Pseudoreward: "+this.currentPseudoReward);
		previousDistance=distance;
		//currentReward += Math.pow(discountfactor, time - this.startTime) * -.5;
		return false;
	}

}

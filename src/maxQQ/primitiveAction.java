package maxQQ;

import Main.Model;

public class primitiveAction implements AbstractAction {
	
	int action;
	
	public primitiveAction(int a){
		action=a;
	}
	
	
	@Override
	public int getAction() {
		// TODO Auto-generated method stub
		return action;
	}

	@Override
	public boolean primitive() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public AbstractAction getSubtask(double[] input, int time,boolean b) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public boolean finished(double[] input, Model model, int time) {
		// TODO Auto-generated method stub
		return true;
	}

}

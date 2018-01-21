package maxQQ;

import java.io.Serializable;

import Main.Model;

public interface AbstractAction {
	public int getAction();

	public AbstractAction getSubtask(double[] input, int time, boolean b);

	public boolean primitive();

	public boolean finished(double[] input, Model model, int time);

}

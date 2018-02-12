package Main;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

public class Main {
	final static double frameLim = 10000;
	final static int gameLim = 500;

	public static void main(String[] args) throws InterruptedException {
		// TODO Auto-generated method stub
		String fileName = JOptionPane.showInputDialog("Input neural network id to store network to.");

		JFrame frame = new JFrame("Game");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JLabel emptyLabel = new JLabel("");
		frame.getContentPane().add(emptyLabel, BorderLayout.CENTER);

		Model model = new Model("map");
		View view = new View(model);
		Controller c = new Controller(view, frame, model, fileName);
		frame.add(view);
		view.setPreferredSize(new Dimension(300, 300));
		frame.pack();
		// frame.setSize(1024, 1000);
		frame.setVisible(true);
		int i = 0;
		int wins = 0;
		int games = 0;
		int f = 0;
		Long lastFrame = System.currentTimeMillis();
		while (true) {

			// if(f%500==0){
			// System.out.println("Frame: "+f);
			// }
			//
			if (games > gameLim) {
				model.reset();
				c = new Controller(view, frame, model, fileName);
				f = 0;
				games = 0;
				i++;
			}

			if (games % 10 == 0 && f == 0) {
				System.out.println("Network: " + i + ", Game: " + games);
				try {
					c.storeAI(i, games);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			c.update(f, true, games % 1 == 0);

//			if (games > 300)
//				Thread.sleep(1000);

			f++;

			if (c.gameover()) {
				System.out.println("Frames: " + f + ", score:" + model.score);
				games++;
				f = 0;
				c.reset(true, i);
			}

		}

	}

}

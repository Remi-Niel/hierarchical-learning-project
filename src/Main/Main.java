package Main;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;

public class Main {
	final static double frameLim=Double.POSITIVE_INFINITY;
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		JFrame frame = new JFrame("Game");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JLabel emptyLabel = new JLabel("");
		frame.getContentPane().add(emptyLabel, BorderLayout.CENTER);

		Model model = new Model("map");
		View view = new View(model);
		Controller c = new Controller(view, frame, model);
		frame.add(view);
		frame.pack();
		frame.setSize(1024, 1000);
		frame.setVisible(true);
		int i = 0;
		int wins = 0;
		int games = 0;
		Long lastFrame = System.currentTimeMillis();
		while (true) {
			if (c.gameover()) {
				if (c.m.player.getHealth() > 0) {
					wins++;
				}
				games++;
				c.reset(true, i);

				System.out.println("Wins: " + wins + ", losses: " + (games - wins) + ", frames: " + i);

				i = 0;
				continue;
			} else if (i > frameLim) {
				games++;
				c.reset(true, i);
				System.out.println("Wins: " + wins + ", losses: " + (games - wins) + ", frames: " + i);
				i = 0;
				continue;
			}

			lastFrame = System.currentTimeMillis();
			// System.out.println(++i);
			c.update(i,games>0 && (games % 1000 == 0));

			if (games>0 &&(games % 1000 == 0)) {
//				 if (true) {
				try {
					Thread.sleep(Math.max(0, 20 - (System.currentTimeMillis() - lastFrame)));
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			i++;
		}

	}

}

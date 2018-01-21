package Main;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

public class MainTest {
	final static double frameLim = 10000;
	final static int gameLim = 10000;
	final static int epochs = 100;

	public static void main(String[] args) throws FileNotFoundException {
		// TODO Auto-generated method stub

		int i = 0;
		int wins = 0;
		int games = 0;
		int inc = 100;
		int f = 0;

		String fileName = JOptionPane.showInputDialog("Input neural network id to load network from.");

		JFrame frame = new JFrame("Game");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JLabel emptyLabel = new JLabel("");
		frame.getContentPane().add(emptyLabel, BorderLayout.CENTER);

		Model model = new Model("map");
		View view = new View(model);
		Controller c = new Controller(view, frame, model, i + fileName + games);
		frame.add(view);
		view.setPreferredSize(new Dimension(1080, 1080));
		frame.pack();
		// frame.setSize(1024, 1000);
		frame.setVisible(true);
		Long lastFrame = System.currentTimeMillis();
		PrintStream stdout = System.out;
		PrintStream out = new PrintStream(new FileOutputStream(fileName + "_performance.csv"));
		// System.setOut(out);
		System.setOut(out);
		System.out.println("trial,games,epoch,points,result");
		System.setOut(stdout);

		for (int t = 0; t < 10000; t++) {
			while (c.load(t + fileName + games)) {
				System.out.println("games: " + games);
				for (int e = 0; e < epochs; e++) {
					while (!c.gameover() && f < frameLim) {
						c.update(f, false, false);

						f++;
					}
					System.setOut(out);
					System.out.print(i + "," + games + "," + e + ",");

					if (f >= frameLim) {
						System.out.print(1 + ",");
					} else if (model.getPlayer().getHealth() <= 0) {
						System.out.print(0 + ",");
					} else {
						System.out.print(3 + ",");
					}

					System.setOut(stdout);
					System.out.println("frames: "+f);
					f = 0;
					c.reset(false, f);
					System.setOut(out);
					System.out.println(model.ai.getScore());
					System.setOut(stdout);
				}
				games += inc;
			}
			games = 0;

		}
	}

}

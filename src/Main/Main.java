package Main;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;

public class Main {
	
	

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		JFrame frame= new JFrame("Game");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JLabel emptyLabel = new JLabel("");
		frame.getContentPane().add(emptyLabel, BorderLayout.CENTER);
		
		Model model=new Model("map1");
		View view = new View(model);
		frame.add(view);
		frame.pack();
		frame.setSize(1024, 1000);
		frame.setVisible(true);
		
		Controller c=new Controller(view,model);
		
		while(true){
			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			c.update();
		}
		
	}

}

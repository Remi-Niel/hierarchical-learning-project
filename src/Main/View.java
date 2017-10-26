package Main;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Observable;
import java.util.Observer;
import java.util.ArrayList;
import javax.swing.JPanel;

import Assets.*;
import mapTiles.Tile;

/**
 * This Class draws the view of the game, it assumes the input map is square.
 * Diameter of units is taken as a factor of the size of the map tiles. So an
 * asset with a diameter of 0.9 becomes a circle with a diameter which equals
 * 90% * tileWidth
 * 
 * @author voldelord
 *
 */
public class View extends JPanel implements Observer {

	Double heading=0.0;
	private static final long serialVersionUID = -7251401100069949488L;
	Model model;

	public View(Model m) {
		super();
		model = m;
		setBackground(Color.BLACK);
	}

	@Override
	public void update(Observable o, Object arg) {
		this.repaint();
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		paintView(g);
	}

	/**
	 * This function draws a single Asset
	 * 
	 * @param g
	 * @param a
	 * @param squareSize
	 * @param horOff
	 * @param verOff
	 */

	/**
	 * xc This function draws the entire model in the panel
	 * 
	 * @param g
	 */
	private void paintView(Graphics g) {
		Map map = model.getLevelMap();
		int minDimension = Math.min(getWidth(), getHeight());
		double relativeSize = 1.0 / map.getSize();
		int absoluteSquareSize = (int) Math.floor(relativeSize * minDimension);
		minDimension=absoluteSquareSize*map.getSize();
		int horizontalOffset = getWidth() - map.getSize() * absoluteSquareSize;
		int verticalOffset = getHeight() - map.getSize() * absoluteSquareSize;
		Tile tile;
		Color c;
		for (int x = 0; x < map.getSize(); x++) {
			for (int y = 0; y < map.getSize(); y++) {
				tile = map.getTile(x, y);

				c = tile.getColor();

				// Floodfill test
				// if(tile.reachable())c=Color.green;

				g.setColor(c);
				g.fillRect(x * absoluteSquareSize + horizontalOffset / 2, y * absoluteSquareSize + verticalOffset / 2,
						absoluteSquareSize, absoluteSquareSize);

			}
		}

		// Paint player
		int diameter = (int) Math.floor(minDimension / map.getSize());

		Player p = model.getPlayer();

		int x = (int) Math.floor((p.getX()) * minDimension) + horizontalOffset / 2;
		int y = (int) Math.floor((p.getY()) * minDimension) + verticalOffset / 2;
		if(model.getHeading()!=-1){
			heading=model.getHeading();
		}
		double headingL = heading;
		double headingH = heading - Math.PI;
		double cornerL = headingL - Math.PI / 4;
		double cornerR = headingL + Math.PI / 4;
		double radius = (int) Math.floor(minDimension / (map.getSize() * 2));

		if (p.getHealth() > 0) {
			g.setColor(Color.blue);
			g.fillPolygon(
					new int[] { (int) (x - Math.cos(cornerL) * radius), (int) (x - Math.cos(cornerR) * radius),
							(int) (x - Math.cos(headingH) * radius) },
					new int[] { (int) (y + Math.sin(cornerL) * radius), y + (int) (Math.sin(cornerR) * radius),
							y + (int) (Math.sin(headingH) * radius) },
					3);
			g.setColor(Color.BLACK);

			g.drawPolygon(
					new int[] { (int) (x - Math.cos(cornerL) * radius), (int) (x - Math.cos(cornerR) * radius),
							(int) (x - Math.cos(headingH) * radius) },
					new int[] { (int) (y + Math.sin(cornerL) * radius), y + (int) (Math.sin(cornerR) * radius),
							y + (int) (Math.sin(headingH) * radius) },
					3);
		}
		// Paint Enemies

		//System.out.println(model.getEnemyList().size());
		for (Enemy e : model.getEnemyList()) {
			g.setColor(Color.red);
			x = (int) Math.floor((e.getX()) * minDimension) + (horizontalOffset - diameter) / 2;
			y = (int) Math.floor((e.getY()) * minDimension) + (verticalOffset - diameter) / 2;

			if (e instanceof Ghost) {
				g.fillOval(x, y, (int) Math.floor(diameter * e.diameter), (int) Math.floor(diameter * e.diameter));
				g.setColor(Color.BLACK);
				g.drawOval(x, y, (int) Math.floor(diameter * e.diameter), (int) Math.floor(diameter * e.diameter));
			} else {
				g.fillRoundRect(x, y, (int) Math.floor(diameter * e.diameter), (int) Math.floor(diameter * e.diameter),
						absoluteSquareSize*3/4, absoluteSquareSize*3/4);
				g.setColor(Color.BLACK);
				g.drawRoundRect(x, y, (int) Math.floor(diameter * e.diameter), (int) Math.floor(diameter * e.diameter),
						absoluteSquareSize*3/4, absoluteSquareSize*3/4);
			}
		}

		// Paint bullets

		for (Bullet b : model.getBullets()) {
			diameter = Math.max((int) Math.floor(minDimension / map.getSize() / 8), 1);
			x = (int) Math.floor((b.getX()) * minDimension) + horizontalOffset / 2;
			y = (int) Math.floor((b.getY()) * minDimension) + verticalOffset / 2;
			g.fillOval(x, y, diameter, diameter);
			g.setColor(Color.BLACK);
		}
		g.setColor(Color.RED);
		g.drawString("Health: "+model.getPlayer().getHealth()+" Keys: "+model.getPlayer().getKeys(), 5, 15);
		g.drawString("Game Over: "+model.gameOver, getWidth()-100, 15);

	}

}

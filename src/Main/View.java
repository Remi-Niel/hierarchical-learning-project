package Main;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JPanel;

import mapTiles.Tile;
/**
 * This Class draws the view of the game, it assumes the input map is square.
 * Diameter of units is taken as a factor of the size of the map tiles.
 * So an asset with a diameter of 0.9 becomes a circle with a diameter
 * which equals 90% * tileWidth
 * @author voldelord
 *
 */
public class View extends JPanel implements Observer {

	/**
	 * 
	 */
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
	 * @param g
	 * @param a
	 * @param squareSize
	 * @param horOff
	 * @param verOff
	 */

	/**xc 
	 * This function draws the entire model in the panel
	 * @param g
	 */
	private void paintView(Graphics g) {
		Map map = model.getLevelMap();
		int minDimension = Math.min(getWidth(), getHeight());
		double relativeSize = 1.0 / map.size;
		int absoluteSquareSize = (int) Math.floor(relativeSize * minDimension);
		int horizontalOffset = getWidth() - map.size * absoluteSquareSize;
		int verticalOffset = getHeight() - map.size * absoluteSquareSize;
		Tile tile;
		Color c;
		for (int x = 0; x < map.size; x++) {
			for (int y = 0; y < map.size; y++) {
				tile = map.getTile(x, y);

				c=tile.getColor();
				g.setColor(c);
				g.fillRect(x * absoluteSquareSize + horizontalOffset / 2, y * absoluteSquareSize + verticalOffset / 2,
						absoluteSquareSize, absoluteSquareSize);
				
			}
		}
		
		//Paint player
		
		//Paint Enemies
		
		//Paint bullets
		
		//Paint 

	}

}

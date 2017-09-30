package mapTiles;

import java.awt.Color;

public class Health extends Tile {
	public final int health=20;
	
	public Health(int x, int y) {
		super(Color.BLUE, false,x,y);
	}
}

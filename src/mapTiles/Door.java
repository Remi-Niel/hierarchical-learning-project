package mapTiles;

import java.awt.Color;

public class Door extends Tile {

	public Door(int x, int y) {
		super(Color.ORANGE, true,x,y);
	}
	
	public void open(){
		this.c=Color.GREEN;
		this.solid=false;
	}

}

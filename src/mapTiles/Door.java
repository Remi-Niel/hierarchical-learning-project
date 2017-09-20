package mapTiles;

import java.awt.Color;

public class Door extends Tile {

	public Door() {
		super(Color.ORANGE, true);
	}
	
	public void open(){
		this.c=Color.GREEN;
		this.solid=false;
	}

}

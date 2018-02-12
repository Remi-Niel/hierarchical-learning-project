package mapTiles;

import java.awt.Color;

public class Door extends Tile {
	
	public boolean entry;
	
	public Door(int x, int y, boolean b) {
		super(Color.ORANGE, true,x,y);
		entry=b;
	}
	
	public void open(){
		this.c=Color.GREEN;
		this.solid=false;
	}

}

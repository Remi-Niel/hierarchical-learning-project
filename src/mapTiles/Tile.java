package mapTiles;

import java.awt.Color;

public class Tile {
	protected Color c;
	protected Boolean solid;
	private boolean reachable;
	
	public Tile(Color c, boolean solid){
		this.c=c;
		this.solid=solid;
		reachable=false;
	}

	public Color getColor() {
		return c;
	}

	public Boolean getSolid() {
		return solid;
	}
	
	public void setReachable(Boolean b){
		reachable=b;
	}
	public Boolean reachable(){
		return reachable;
	}
}

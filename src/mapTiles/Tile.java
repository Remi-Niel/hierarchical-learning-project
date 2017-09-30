package mapTiles;

import java.awt.Color;

public class Tile {
	protected Color c;
	protected Boolean solid;
	int x, y;
	
	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	private boolean reachable;

	public Tile(Color c, boolean solid, int x, int y) {
		this.c = c;
		this.solid = solid;
		this.x=x;
		this.y=y;
		reachable = false;
	}

	public Color getColor() {
		return c;
	}

	public Boolean getSolid() {
		return solid;
	}

	public void setReachable(Boolean b) {
		reachable = b;
	}

	public Boolean reachable() {
		return reachable;
	}
}

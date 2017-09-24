package Main;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import mapTiles.*;

public class Map {
	private Tile[][] tileMap;
	private char[][] levelmap;
	private int spawnX, spawnY;
	private int size;
	private ArrayList<Spawner> spawnerList;

	public Map(String fileName) throws FileNotFoundException {

		Scanner input = new Scanner(new File("maps/" + fileName));
		size = Integer.parseInt(input.nextLine());
		levelmap = new char[getSize()][getSize()];
		for (int i = 0; i < getSize(); i++) {
			String line = input.nextLine();
			levelmap[i] = line.toCharArray();
		}
		input.close();
		spawnerList = new ArrayList<Spawner>();

		tileMap = new Tile[getSize()][getSize()];
		for (int x = 0; x < getSize(); x++) {
			for (int y = 0; y < getSize(); y++) {
				switch (levelmap[y][x]) {
				case '#':
					tileMap[x][y] = new Wall();
					break;
				case 'D':
					tileMap[x][y] = new Door();
					break;
				case 'K':
					tileMap[x][y] = new Key();
					break;
				case 'S':
					tileMap[x][y] = new Spawner(x, y);
					spawnerList.add((Spawner) tileMap[x][y]);
					break;
				case 'E':
					tileMap[x][y] = new Exit();
					break;
				case 'P':
					spawnX = x;
					spawnY = y;
				default:
					tileMap[x][y] = new Floor();
				}
			}
		}
		long t=System.currentTimeMillis();
		this.floodFillReachable(spawnX, spawnY);
		System.out.println(System.currentTimeMillis()-t+" ms");
		System.out.println(spawnX + " " + spawnY);
	}

	public void printMap() {
		System.out.println(getSize());
		for (int i = 0; i < getSize(); i++) {
			for (int j = 0; j < getSize(); j++) {
				System.out.print(levelmap[i][j]);
			}
			System.out.println("");
		}
	}

	public Tile getTile(int x, int y) {
		return tileMap[x][y];
	}
	
	public void destroyTile(int x, int y) {
		if(tileMap[x][y]instanceof Spawner){
			spawnerList.remove(tileMap[x][y]);
		}
		tileMap[x][y]= new Floor();
		tileMap[x][y].setReachable(true);
	}

	public ArrayList<Spawner> getSpawners() {
		return spawnerList;
	}

	public void floodFillReachable(int x, int y) {
		getTile(x, y).setReachable(true);
		
		
		if (x<getSize() && !getTile(x + 1, y).getSolid() && !getTile(x + 1, y).reachable())
			floodFillReachable(x + 1, y);
		if (x>0 && !getTile(x - 1, y).getSolid() && !getTile(x - 1, y).reachable())
			floodFillReachable(x - 1, y);
		if (y<getSize() && !getTile(x, y + 1).getSolid() && !getTile(x, y + 1).reachable())
			floodFillReachable(x, y+1);
		if (y>0 && !getTile(x, y - 1).getSolid() && !getTile(x, y - 1).reachable())
			floodFillReachable(x, y-1);

	}

	public int getSize() {
		return size;
	}

	public Tile[][] getTileMap() {
		return tileMap;
	}

	public int getSpawnX() {
		return spawnX;
	}

	public int getSpawnY() {
		return spawnY;
	}

	public ArrayList<Spawner> getSpawnerList() {
		return spawnerList;
	}

}

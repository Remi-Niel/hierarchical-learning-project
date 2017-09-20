package Main;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import mapTiles.*;

public class Map {
	Tile[][] tileMap;
	char[][] levelmap;
	int spawnX,spawnY;
	public int size;
	
	public Map(String fileName) throws FileNotFoundException {
		
		Scanner input = new Scanner(new File("maps/" + fileName));
		size = Integer.parseInt(input.nextLine());
		levelmap = new char[size][size];
		for(int i=0;i<size;i++){
			String line=input.nextLine();
			levelmap[i]=line.toCharArray();
		}
		input.close();
		
		tileMap=new Tile[size][size];
		for(int x=0;x<size;x++){
			for(int y=0;y<size;y++){
				switch (levelmap[y][x]) {
				case '#':
					tileMap[x][y]=new Wall();
					break;
				case 'D':
					tileMap[x][y]=new Door();
					break;
				case 'K':
					tileMap[x][y]=new Key();
					break;
				case 'S':
					tileMap[x][y]=new Spawner();
					break;
				case 'E':
					tileMap[x][y]=new Exit();
					break;
				case 'P':
					spawnX=x;
					spawnY=y;
				default:
					tileMap[x][y]=new Floor();
				}
			}
		}
		System.out.println(spawnX +" "+spawnY);
	}
	
	public void printMap(){
		System.out.println(size);
		for(int i=0;i<size;i++){
			for(int j=0;j<size;j++){
				System.out.print(levelmap[i][j]);
			}
			System.out.println("");
		}
	}
	
	public Tile getTile(int x,int y){
		return tileMap[x][y];
	}
}

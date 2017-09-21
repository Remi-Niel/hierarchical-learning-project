package Main;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Random;

import Assets.Enemy;
import mapTiles.Spawner;

public class Model {
	
	Random r=new Random();
	Map levelMap;
	ArrayList<Enemy> enemyList;
	double mapSize;
	
	public Model(String fileName) {
		try {
			levelMap = new Map(fileName);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		enemyList=new ArrayList<Enemy>();
		mapSize = (double) levelMap.size;

	}
	
	public double distance(double x1, double x2, double y1, double y2){
		
		return Math.sqrt((x1-x2)*(x1-x2)+(y1-y2)*(y1-y2));
	}
	
	public boolean colides(double x,double y){
		
		for(Enemy e: enemyList){
			if(distance(e.getX(),x,e.getY(),y)<e.diameter/mapSize)return true;
		}
		
		return false;
	}
	
	public void tickSpawners(){
		for (Spawner s : levelMap.getSpawners()) {
			if(s.spawn()){
				double dX=r.nextInt(4)-2;
				double dY=r.nextInt(4)-2;
				int i=0;
				while(colides((s.getX()+dX)/mapSize,(s.getY()+dY)/mapSize)||dX+dY==0){
					dX=r.nextInt(4)-2;
					dY=r.nextInt(4)-2;
				}
				enemyList.add(new Enemy((s.getX()+dX)/mapSize,(s.getY()+dY)/mapSize,s));
			}
		}
	}

	public Map getLevelMap() {
		return levelMap;
	}
	
	public ArrayList<Enemy> getEnemyList(){
		return enemyList;
	}

}

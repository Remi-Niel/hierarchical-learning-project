package Main;

import java.io.FileNotFoundException;

public class Model {
	
	Map levelMap;
	double mapSize;
	
	public Model(String fileName) {
		try {
			levelMap = new Map(fileName);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		mapSize = (double) levelMap.size;

	}

	public Map getLevelMap() {
		return levelMap;
	}

}

package Main;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Map {
	char[][] levelmap;
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
	
	public char getTile(int x,int y){
		return levelmap[x][y];
	}
}

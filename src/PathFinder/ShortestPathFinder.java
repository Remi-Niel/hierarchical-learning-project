package PathFinder;

import java.util.PriorityQueue;
import java.util.Random;

import Main.Map;

public class ShortestPathFinder {
	
	class Position implements Comparable<Position>{
		int x,y;
		int travelled;
		int heuristic;
		int total;
		char direction;
		Random rand=new Random();
		
		public Position(int x, int y,char direction, int travelled,int heuristic){
			this.x=x;
			this.y=y;
			this.direction=direction;
			this.travelled=travelled;
			this.heuristic=heuristic;
			total=heuristic+travelled;
		}
		public void set(char direction, int travelled){
			if(travelled<this.travelled){
				this.direction=direction;
				this.travelled=travelled;
				total=heuristic+travelled;
				addState(this);
			}
		}
		@Override
		public int compareTo(Position s) {	       
			if(this.total-total==0){
				return 1-(rand.nextInt(2))*2;
			}
			return this.total-s.total;
		}
	}
	
	private PriorityQueue<Position> queue;
	private Map m;
	
	public ShortestPathFinder(Map levelMap) {
		m=levelMap;
	}

	private void addState(Position s){
		if(m.getTile(s.x, s.y)!= '#'){
			queue.offer(s);
		}
	}
	
	public char findPath(double x1,double y1,double x2,double y2,double diameter){
		char direction='n';
		queue= new PriorityQueue<Position>();
		Position[][] map=new Position[m.size][m.size];
		
		int startX=(int)(x1*m.size);
		int startY=(int)(y1*m.size);
		int goalX=(int)(x2*m.size);
		int goalY=(int)(y2*m.size);
		
		//System.out.println(startX +" "+startY +" " + goalX +" "+goalY);
		
		//if already on correct tile return 'no move'
		if(this.heuristic(startX, startY, goalX, goalY)==0){
			if(Math.abs(x1-x2)>Math.abs(y1-y2)){
				//System.out.println(unitX-targetX);
				if(x1-x2<0){
					return 'r';
				}else{
					return 'l';
				}
			}else{
				if(y1-y2<0){
					return 'd';
				}else{
					return 'u';
				}
			}
		}
		
		for(int i=0;i<m.size;i++){
			for(int j=0;j<m.size;j++){
				map[i][j]=new Position(i,j,'n',1000*m.size,this.heuristic(i, j, goalX, goalY));
			}
		}
		if(startY>0)map[startX][startY-1].set('u', 1);
		if(startX<m.size-1)map[startX+1][startY].set('r', 1);
		if(startX>0)map[startX-1][startY].set('l', 1);
		if(startY<m.size-1)map[startX][startY+1].set('d', 1);
		
		Position s;
		while(queue.size()>0){
			
			s = queue.poll();
			//System.out.println(s.x +" "+s.y+" "+x2+" "+y2+" "+s.travelled+" "+queue.size()+" "+s.direction);
			if(s.x==goalX && s.y==goalY){
				//System.out.println(s.direction);
				direction=s.direction;
				continue;
			}
			if(s.x<m.size){
				map[s.x+1][s.y].set(s.direction, s.travelled+1);
			}
			if(s.x > 0){
				map[s.x-1][s.y].set(s.direction, s.travelled+1);
			}
			if(s.y<m.size){
				map[s.x][s.y+1].set(s.direction, s.travelled+1);
			}
			if(s.y>0){
				map[s.x][s.y-1].set(s.direction, s.travelled+1);
			}
		}
		//System.out.println(direction);
		
		//System.out.print(direction);
		if(direction == 'd' || direction == 'u'){
			if(startX != (int)((x1+diameter)*m.size)){
				direction = 'l';
			}
		}else{
			if(startY != (int)((y1+diameter)*m.size)){
				direction = 'u';
			}
		}
		//System.out.println(direction);
		
		return direction;
	}
	
	public int findDistance(double x1,double y1,double x2,double y2,double diameter){
		int distance=Integer.MAX_VALUE;
		queue= new PriorityQueue<Position>();
		Position[][] map=new Position[m.size][m.size];
		
		int startX=(int)(x1*m.size);
		int startY=(int)(y1*m.size);
		int goalX=(int)(x2*m.size);
		int goalY=(int)(y2*m.size);
		
		//System.out.println(startX +" "+startY +" " + goalX +" "+goalY);
		
		//if already on correct tile return 'no move'
		if(this.heuristic(startX, startY, goalX, goalY)==0){
			if(Math.abs(x1-x2)>Math.abs(y1-y2)){
				//System.out.println(unitX-targetX);
				if(x1-x2<0){
					return 'r';
				}else{
					return 'l';
				}
			}else{
				if(y1-y2<0){
					return 'd';
				}else{
					return 'u';
				}
			}
		}
		
		for(int i=0;i<m.size;i++){
			for(int j=0;j<m.size;j++){
				map[i][j]=new Position(i,j,'n',1000*m.size,this.heuristic(i, j, goalX, goalY));
			}
		}
		if(startY>0)map[startX][startY-1].set('u', 1);
		if(startX<m.size-1)map[startX+1][startY].set('r', 1);
		if(startX>0)map[startX-1][startY].set('l', 1);
		if(startY<m.size-1)map[startX][startY+1].set('d', 1);
		
		Position s;
		while(queue.size()>0){
			
			s = queue.poll();
			//System.out.println(s.x +" "+s.y+" "+x2+" "+y2+" "+s.travelled+" "+queue.size()+" "+s.direction);
			if(s.x==goalX && s.y==goalY){
				//System.out.println(s.direction);
				distance=s.total;
				continue;
			}
			if(s.x<m.size){
				map[s.x+1][s.y].set(s.direction, s.travelled+1);
			}
			if(s.x > 0){
				map[s.x-1][s.y].set(s.direction, s.travelled+1);
			}
			if(s.y<m.size){
				map[s.x][s.y+1].set(s.direction, s.travelled+1);
			}
			if(s.y>0){
				map[s.x][s.y-1].set(s.direction, s.travelled+1);
			}
		}
		
		return distance;
	}
	
	private int heuristic(int x1,int y1,int x2,int y2){
		return (Math.abs(x2-x1)+Math.abs(y2-y1));
	}
	
	
}

package AI;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class ManualAI implements AI, KeyListener {

	boolean left,right,up,down,shoot;
	double heading=-1;
	
	public ManualAI(){
		left=right=up=down=shoot=false;
	}
	
	
	public void setHeading() {
		
		double vertical=-1;
		double horizontal=-1;
		double heading=0;
		
		if(up && !down){
			vertical=Math.PI/2;
		}else if(!up && down){
			vertical=Math.PI*3/2;
		}
		
		if(left && !right){
			horizontal=Math.PI;
		}else if(!left && right){
			horizontal=0;
		}
		
		int count=0;
		
		if(vertical != -1){
			heading += vertical;
			count++;
		}
		
		if(horizontal != -1){
			if(horizontal == 0){
				if(vertical > Math.PI){
					heading+= 2*Math.PI;
				}
			}else{
				heading+=horizontal;
			}
			count++;
		}
		
		if(count==0){
			this.heading=-1;
		}else{
			this.heading=heading/count;
		}
	}
	
	@Override
	public double getHeading() {
		return heading;
	}

	@Override
	public boolean shoot() {
		return shoot;
	}

	@Override
	public void reset(boolean t,int time) {
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode()==KeyEvent.VK_UP){
			up=true;
			this.setHeading();
		}else if(e.getKeyCode()==KeyEvent.VK_DOWN){
			down=true;
			this.setHeading();
		}else if(e.getKeyCode()==KeyEvent.VK_LEFT){
			left=true;
			this.setHeading();
		}else if(e.getKeyCode()==KeyEvent.VK_RIGHT){
			right=true;
			this.setHeading();
		}else if(e.getKeyCode()==KeyEvent.VK_SPACE){
			shoot=true;
		}
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if(e.getKeyCode()==KeyEvent.VK_UP){
			up=false;
			this.setHeading();
		}else if(e.getKeyCode()==KeyEvent.VK_DOWN){
			down=false;
			this.setHeading();
		}else if(e.getKeyCode()==KeyEvent.VK_LEFT){
			left=false;
			this.setHeading();
		}else if(e.getKeyCode()==KeyEvent.VK_RIGHT){
			right=false;
			this.setHeading();
		}else if(e.getKeyCode()==KeyEvent.VK_SPACE){
			shoot=false;
		}
	}

	public void determineAction(int time,boolean b) {
	}


}

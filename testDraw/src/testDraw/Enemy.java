package testDraw;

import cbl.quickdraw.QuickDraw;

public class Enemy {
	private int spdX, spdY=0, life, index, size, x, y, gravity=1; 
	public boolean abtToDie = false;
	public int attack = 1;

	public Enemy(int spdX, int life, int x, int y, int size) {
		index = (int)(Math.random()*5000); //index in ndjson
		this.spdX = spdX;
		this.life = life;
		this.size = size;
		this.x=x; this.y=y;
	}
	
	public void drawEnemy(QuickDraw qd) {
		qd.create(x,y,size,size, index, 0, 1);
	}
	
	public void updatePosition() {
		this.x += spdX;
		this.y += spdY;
	}
	
	void applyGravity() {
		  spdY += gravity;
		  keepInScreen();
		}
	
	void makeBounceBottom(int surface) {
		  y = surface-(size);
		  spdY/=1.3;
		  spdY*=-1;
		}
	
	void makeBounceLeft(int surface){
		  x = surface+(size);
		  spdX*=-1;
		}
		void makeBounceRight(int surface){
			 x = surface-(size);
			spdX*=-1;
		}
		
	public void setSpdX(int sx) {
		spdX = sx;
	}
	
	public int getSpdX() {
		return spdX;
	}
		
		// keep ball above floor
	void keepInScreen() {
		  // ball hits floor
		  if (this.y+(size) > UsingProcessing.floorY) { 
		    makeBounceBottom(UsingProcessing.floorY);
		  }
		  if (x-size < -size/2){
			    makeBounceLeft(0);
			  }
	     if (x+size > UsingProcessing.width){
			    makeBounceRight(UsingProcessing.width);
			  }
		}
		
	boolean checkCollision(int dx, int dy, int ds) {
		if(x+size < dx || x>dx+ds || y+size<dy || y>dy+ds || abtToDie) {
			return false;
		}else {
			return true;
		}
	}
		
}

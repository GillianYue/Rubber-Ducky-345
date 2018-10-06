package testDraw;
import processing.core.PApplet;
import processing.core.PImage;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.json.simple.parser.ParseException;

import cbl.quickdraw.*;

public class UsingProcessing extends PApplet{
	
	QuickDraw qd; //specifically for drawing ndjson files
	int savedTime, interval;
	PImage bgImg, ducky, flipped;
	HTTPrequest request;
	TextParser tp;
	ArrayList<String> categories, levelKeywords;
	int numLevels = 5;
	Color[] levelColors = new Color[numLevels];
	ArrayList<Enemy> enemies;
	int duckyFacing, duckyX, duckyY, dLife = 5;
	ArrayList<Enemy> gbg = new ArrayList<Enemy>();
	boolean newLevelReady = true, colorReady = true;
	
	public static int currLevel = -1, floorY = 450,
			width = 900, height = 600;

    public static void main(String[] args) {
       PApplet.main("testDraw.UsingProcessing");
    }

    public void settings(){
        size(width, height);
        savedTime = millis();
        interval = 1000;
        println("datapath: "+dataPath("/"));
    }

    public void setup(){
    	
        request = new HTTPrequest();
        tp = new TextParser();

        
        levelKeywords = new ArrayList<String>();
        levelKeywords.add("horse");
        
        ducky = loadImage("../../ducky.png");
        println(dataPath(""));
        levelColors[0] = new Color(126,76,55);
        
        SetUpThread su = new SetUpThread();
        su.start();
        
        enemies = new ArrayList<Enemy>();
    }

    public void draw(){
    	if(currLevel == -1) {
    		loadScreen();
    	}else if(currLevel ==-2){
    		gameOver();
    	}else { //normal game
    	
    		clear();
    		background(255);
    		if(bgImg!=null) {
    			tint(255, 50);
    			image(bgImg,0,0);
    		}else println("should be showing bg");
    		

    		if(ducky!=null) {
        		imageMode(CENTER);
    		noTint();
    			duckyX = width/2+(duckyFacing*50);
    			duckyY = floorY-ducky.height/2;
    			image(ducky, duckyX, duckyY, 
        				100, 100);
    		imageMode(CORNER);
    		}
    		
    		fill(levelColors[currLevel].getRGB());
    		rectMode(0);
    		rect(0, floorY, width, 100);
    		if (enemies!=null) {
    		for(Enemy e: enemies) {
    			e.applyGravity();
    			e.updatePosition();
    			e.drawEnemy(qd);
    			if(e.checkCollision(duckyX, duckyY, ducky.width)) {
    				if(duckyFacing==0) dLife-= e.attack;
    			gbg.add(e);
    			e.attack = 0;
    			}
    		}
    		if(gbg!=null) {
    			for(Enemy e: gbg) {
    				  enemies.remove(e);
    			}
    		}
    		}
    		
    		updateLife();
    		
    	if(millis()-savedTime > interval) {

    		savedTime = millis();
    		spawnEnemy();
    	}
    	}
    }

    public void spawnEnemy() {
    	int eSize = (int)(Math.random()*30)+30;
    	int x;
    	do {
    	x = (int)(Math.random()*width-60);
    	}while(Math.abs(x - width/2)<150);
    	int dir = (x>width/2)? -1:1;
    	enemies.add(new Enemy(
    			(int)(dir*((Math.random()*3)+ 2)),1,
    			Math.max(0,x),
    			Math.max(floorY/2,
    					(int)(Math.random()*floorY-60+floorY/2))
    			,eSize));
    }
    
    public void setUpLevel(int l) {
    	
    	if(newLevelReady && colorReady) { //means ndjson file loaded for this level
    	String keyword = levelKeywords.get(l);
    	println("setting up...the keyword for level "+l+" is "+keyword);
    	//set up quick draw data for level, assuming ndjson file is there
        qd = new QuickDraw(this, "ndjson/"+
    keyword+".ndjson"); //will change the enemies generated
        qd.mode(0);
        
        try {
        	int count = 0;
        	while(//gets random relevant bg image from google
     !setBackgroundImg(request.googleImageRequest(keyword+" background").get(count))) {
        		count++;
        		println("loading another background");
        		if(count>=100) 
        		println("Something's definitely wrong");
        	}
        
        newLevelReady = false;
        colorReady = false;
        
        //start preparing for next level
        NdjsonThread t2 = new NdjsonThread(l+1); 
        t2.start();
        
        ColorFinder t3 = new ColorFinder(l+1, levelKeywords.get(l+1));
        t3.start();
        
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
    	}
    }
    
    public boolean setBackgroundImg(String url) {
    	bgImg = loadImage(url);
    	if(bgImg==null || Math.abs(width/height - bgImg.width/bgImg.height)>0.5) {
    		return false;
    	}
    	println(bgImg.width+" "+bgImg.height);
    	bgImg.resize(width, height);
    	return true;
    }
    
    public void updateLife() {
    	//if(dLife<=0) currLevel = -2;
    	//else {
    	textAlign(CORNER);
    	textSize(30);
    	fill(0, 102, 153);
    	text("Life: "+dLife, 30, 30); 
    	textAlign(CORNER);
    	textSize(20);
    	fill(0,0,0);
    	text("NextWorldReady: "+(newLevelReady&&colorReady), 30, 60);
    	textAlign(CORNER);
    	textSize(20);
    	fill(0,0,0);
    	text("WorldName: "+ levelKeywords.get(currLevel), 30, 90);
    	//}
    }
    
    public Color findLevelColor(String keyword) {
    	int numPics = 10; //averaging the color results of this many pics
    	int r=0, g=0, b=0;
    	
		try {
    	List<String> urls = request.googleImageRequest(keyword);
    	
    	for(int p=0; p<numPics; p++) {
    	BufferedImage img;
    	URL url = new URL(urls.get(p));
    	URLConnection openC = url.openConnection();
    	openC.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");
		try {
    	img = ImageIO.read(url.openStream()); 
		} catch (IOException e) {
			img = null;
		}
		if(img==null) { 
			numPics++;
		}else {
    	//img.resize(1, 1);
    	img.getScaledInstance(1, 1, Image.SCALE_FAST);
    	Color col = new Color(img.getRGB(0, 0));
    	r+=col.getRed();
    	g+=col.getGreen();
    	b+=col.getBlue();
    	System.out.println("success getting one color");
		}
    	}
    	
    	} catch (ParseException | IOException e) {
			e.printStackTrace();
		}
    	
    	r/=numPics; g/=numPics; b/=numPics; //gets the average
    	return new Color(r,g,b);
    }
    
    void loadScreen() {
    	background(0, 168, 107);
    	textAlign(CENTER);
    	textSize(60);
    	fill(0, 102, 153);
    	text("Loading...", width/2, height/2); 
    }
    
    void gameOver() {
    	background(0);
    	textAlign(CENTER);
    	textSize(60);
    	fill(0, 102, 153);
    	text("Game Over!!", width/2, height/2); 
    }
    
    /**
     * Thread for loading ndjson file; want game to run while doing this
     * @author gillianyue
     *
     */
    class NdjsonThread extends Thread {
    	int l;
    	
    	NdjsonThread(int l){
    		this.l = l;
    	}
    	
    	public void run() {
            //start retrieving data for next level, since it takes a while
            try {
	            println("starting to fetch ndjson for "+levelKeywords.get(l));
				request.googleCloudRequest(levelKeywords.get(l));
				newLevelReady = true;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    }
    
    class ColorFinder extends Thread {
    	int l;
    	String keyword;
    	
    	ColorFinder(int l, String keyword){
    		this.l = l;
    		this.keyword = keyword;
    	}
    	
    	public void run() { 
    	levelColors[l] = findLevelColor(keyword);
        println("Color for level "+l+" is "+levelColors[l]);
        colorReady = true;
    	}
    }
    
    class SetUpThread extends Thread {
    	
    	SetUpThread(){
            try {
                NdjsonThread first = new NdjsonThread(0); 
                first.start();
            //parse all possible object names
         	categories = tp.readDoc("categories.txt");
         	
         	for(int i=1; i<numLevels; i++) {
         		//randomly get keywords for each level
         		//i starts from 1 because for 0 (level 1) is always "duck"
         		String rdm = categories.get((int)(Math.random()*categories.size()));
         		while(rdm.contains(" ")) {
               rdm = categories.get((int)(Math.random()*categories.size()));
         		}
         		levelKeywords.add(rdm);
         	}
         	
            setUpLevel(0);//fixed beginning level
            
            flipped = createImage(ducky.width,ducky.height,RGB);//create a new image with the same dimensions
			for(int i = 0 ; i < flipped.pixels.length; i++){       //loop through each pixel
			  int srcX = i % flipped.width;                        //calculate source(original) x position
			  int dstX = flipped.width-srcX-1;                     //calculate destination(flipped) x position = (maximum-x-1)
			  int y    = i / flipped.width;                        //calculate y coordinate
			  flipped.pixels[y*flipped.width+dstX] = ducky.pixels[i];//write the destination(x flipped) pixel based on the current pixel  
			}
			
			currLevel++;
            
     	} catch (IOException e) {
     		e.printStackTrace();
     	}
    	}
    }

	 public void keyPressed() {
		 
		if(keyCode == LEFT) {
			new Thread(){
			    public void run(){
					duckyFacing = -1;
					try {
						Thread.sleep(300);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					duckyFacing = 0;
			    }
			  }.start();
		}
		else if(keyCode == RIGHT) {
			new Thread(){
			    public void run(){
					duckyFacing = 1;
					try {
						Thread.sleep(300);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					duckyFacing = 0;
			    }
			  }.start();
		}else if(keyCode == UP) {
			if(currLevel == -2) //game over
			currLevel = 1; //restart
			currLevel++;
			setUpLevel(currLevel);
		}
	}

}
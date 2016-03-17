package sonetta.apache;

import android.graphics.Bitmap;
import android.graphics.Canvas;

/**
 * Created by ksook on 3/16/2016.
 */
public class Player extends GameObject {

    private Bitmap spritesheet;
    private int score;
    private long startTime;
    private boolean up, playing;
    private Animation animation = new Animation();

    public Player(Bitmap res, int w, int h, int numFrames) {
        this.x = 100;
        this.y = GamePanel.HEIGHT/2;
        dy = 0;
        height = h;
        width = w;

        Bitmap[] image = new Bitmap[numFrames];
        this.spritesheet = res;

        for(int i = 0; i<image.length; i++) {
            image[i] = Bitmap.createBitmap(spritesheet,i*width,0,width,height);
        }

        animation.setFrames(image);
        animation.setDelay(10);
        startTime = System.nanoTime();
    }

    public void setUp(boolean b) { up = b;}

    public void update() {

        long elapsed = (System.nanoTime()-startTime)/1000000;
        if(elapsed>100) {
            score++;
            startTime = System.nanoTime();
        }
        animation.update();

        if(up) {
            dy -=1;
        } else {
            dy +=1;
        }

        if(dy>14) { dy = 14;}
        if(dy<-14) {dy = -14;}

        y+=dy*2;
    }

    public void draw(Canvas canvas) {
        canvas.drawBitmap(animation.getImage(),x,y,null);
    }

    public int getScore() { return score;}
    public boolean getPlaying() { return playing;}
    public void setPlaying(boolean p) { this.playing = p;}
    public void resetDY() { dy = 0;}
    public void resetScore() { score = 0;}

}

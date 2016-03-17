package sonetta.apache;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import java.util.Random;

/**
 * Created by ksook on 3/16/2016.
 */
public class Missile extends GameObject{

    private Bitmap spritesheet;
    private int speed, score;
    private Random random = new Random();
    private Animation animation = new Animation();

    public Missile(Bitmap res, int x, int y, int w, int h, int s, int numFrames) {

        this.x = x;
        this.y = y;
        width = w;
        height = h;
        this.score = s;
        this.speed = 7+(int)(random.nextDouble()*score/30);
        this.spritesheet = res;

        if(speed>40) { speed = 40;}

        Bitmap[] image = new Bitmap[numFrames];
        for(int i = 0; i<image.length; i++) {
            image[i] = Bitmap.createBitmap(spritesheet,0,i*height,width,height);
        }
        animation.setFrames(image);
        animation.setDelay(100-speed);
    }

    public void update() {
        x-=speed;
        animation.update();
    }

    public void draw(Canvas canvas) {
        try {
            canvas.drawBitmap(animation.getImage(),x,y,null);
        } catch (Exception e) {

        }
    }

    @Override
    public int getWidth() {
        return width-10;
    }
}

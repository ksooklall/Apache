package sonetta.apache;

import android.graphics.Bitmap;
import android.graphics.Canvas;

/**
 * Created by ksook on 3/16/2016.
 */
public class BottomBorder extends GameObject {

    private Bitmap image;


    public BottomBorder(Bitmap res, int x, int y) {
        this.x = x;
        this.y = y;
        this.height = 200;
        this.width = 20;
        this.image = Bitmap.createBitmap(res,0,0,width,height);
        this.dx = GamePanel.MOVESPEED;
    }

    public void update() {
        x+=dx;
    }

    public void draw(Canvas canvas) {
        canvas.drawBitmap(image,x,y,null);
    }
    public BottomBorder() {

    }
}

package sonetta.apache;

import android.graphics.Bitmap;
import android.graphics.Canvas;

/**
 * Created by ksook on 3/16/2016.
 */
public class TopBorder extends GameObject {

    private Bitmap image;


    public TopBorder(Bitmap res, int x, int y, int h) {
        this.x = x;
        this.y = y;
        this.height = h;
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
}

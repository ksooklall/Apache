package sonetta.apache;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * Created by ksook on 3/16/2016.
 */
public class Smokepuff extends GameObject {

    public int r = 5;

    public Smokepuff(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void update() {
        x-=10;
    }

    public void draw(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(Color.GRAY);
        paint.setStyle(Paint.Style.FILL);

        canvas.drawCircle(x-r,y-r,r,paint);
        canvas.drawCircle(x-r+2,y+r-2,r,paint);
        canvas.drawCircle(x-r+4,y-r-4,r,paint);
    }
}
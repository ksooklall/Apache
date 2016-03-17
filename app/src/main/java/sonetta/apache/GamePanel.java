package sonetta.apache;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by ksook on 3/16/2016.
 */
public class GamePanel extends SurfaceView implements SurfaceHolder.Callback {

    public static final int WIDTH = 856, HEIGHT = 480, MOVESPEED=-5;
    private int mili = 1000000;
    private MainThread thread;
    private Background background;
    private Player player;
    private ArrayList<Smokepuff> smokepuffs;
    private long smokeStartTime;
    private ArrayList<Missile> missiles;
    private long missileStartTime;
    private ArrayList<TopBorder> topBorders;
    private ArrayList<BottomBorder> bottomBorders;
    private Random random = new Random();
    private int maxBorderHeight;
    private int minBorderHeight;
    private boolean topDown = true;
    private boolean botDown = true;
    private int progressDenom = 20;

    public GamePanel(Context context) {
        super(context);
        getHolder().addCallback(this);
        setFocusable(true);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        background = new Background(BitmapFactory.decodeResource(getResources(),R.drawable.grassbg1));
        player = new Player((BitmapFactory.decodeResource(getResources(),R.drawable.helicopter)),65,25,3);
        smokepuffs = new ArrayList<Smokepuff>();
        smokeStartTime = System.nanoTime();
        missiles = new ArrayList<>();
        missileStartTime = System.nanoTime();
        topBorders = new ArrayList<>();
        bottomBorders = new ArrayList<>();

        thread = new MainThread(getHolder(), this);
        thread.setRunning(true);
        thread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        int counter = 0;
        while(retry && counter<1000) {
            try {
                thread.setRunning(false);
                thread.join();
                retry = false;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void update() {
        if(player.getPlaying()) {
            background.update();
            player.update();

            minBorderHeight = 5+player.getScore()/progressDenom;
            maxBorderHeight = 30+player.getScore()/progressDenom;
            if(maxBorderHeight > HEIGHT/4) {
                maxBorderHeight = HEIGHT/4;
            }
            this.updateBottomBorder();
            this.updateTopBorder();

            long missilesElapsed = (System.nanoTime()-missileStartTime)/mili;
            if(missilesElapsed>(2000-player.getScore()/4)) {
                if(missiles.size()==1) {
                    missiles.add(new Missile(BitmapFactory.decodeResource(getResources(),R.drawable.missile),
                            WIDTH+10,HEIGHT/2,45,15,player.getScore(),13));
                } else {
                    missiles.add(new Missile(BitmapFactory.decodeResource(getResources(),R.drawable.missile),
                            WIDTH+10,(int) (random.nextDouble()*HEIGHT),45,15,player.getScore(),13));
                }
                missileStartTime = System.nanoTime();
            }

            for(int i = 0; i<missiles.size(); i++) {
                missiles.get(i).update();
                if(collision(missiles.get(i),player)) {
                    missiles.remove(i);
                    player.setPlaying(false);
                    break;
                }
                if(missiles.get(i).getX()<-100) {
                    missiles.remove(i);
                    break;
                }
            }

            long elapsed = (System.nanoTime()-smokeStartTime)/mili;
            if(elapsed>120) {
                smokepuffs.add(new Smokepuff(player.getX(),player.getY()+10));
                smokeStartTime = System.nanoTime();
            }
            for(int i = 0; i<smokepuffs.size(); i++) {
                smokepuffs.get(i).update();
                if(smokepuffs.get(i).getX()<-10) {
                    smokepuffs.remove(i);
                }
            }
        }
    }

    public boolean collision(GameObject a, GameObject b) {
        return Rect.intersects(a.getRect(),b.getRect())?true:false;
    }

    @Override
    public void draw(Canvas canvas) {

        final float scaleX = getWidth()/(WIDTH*1.f);
        final float scaleY = getHeight()/(HEIGHT*1.f);
        if(canvas!=null) {
            final int savedState = canvas.save();
            canvas.scale(scaleX, scaleY);
            background.draw(canvas);
            player.draw(canvas);
            for(Smokepuff sp:smokepuffs) {
                sp.draw(canvas);
            }
            for(Missile m:missiles) {
                m.draw(canvas);
            }
            for(TopBorder tb:topBorders) {
                tb.draw(canvas);
            }
            for(BottomBorder bb:bottomBorders) {
                bb.draw(canvas);
            }
            canvas.restoreToCount(savedState);
        }
    }

    public void updateBottomBorder() {
        if(player.getScore()%50==0) {
            topBorders.add(new TopBorder(BitmapFactory.decodeResource(getResources(),R.drawable.brick),
                    topBorders.get(topBorders.size()-1).getX()+20,0,(int)(random.nextDouble()*maxBorderHeight)+1));
        }
        for(int i = 0; i<bottomBorders.size(); i++) {
            bottomBorders.get(i).update();
            if(bottomBorders.get(i).getX()<-bottomBorders.get(i).getWidth()) {
                bottomBorders.remove(i);
            }
        }
    }

    public void updateTopBorder() {
        //int currentSize = topBorders.size()-1;
        if(player.getScore()%40==0) {
            bottomBorders.add(new BottomBorder(BitmapFactory.decodeResource(getResources(),R.drawable.brick),
                    bottomBorders.get(bottomBorders.size()-1).getX()+20,(int)(random.nextDouble()*maxBorderHeight)+
                    (HEIGHT-maxBorderHeight)));
        }
        for(int i = 0; i<bottomBorders.size(); i++) {
            topBorders.get(i).update();
            if(topBorders.get(i).getX()<-topBorders.get(i).getWidth()) {
                topBorders.remove(i);
                if(topBorders.get(topBorders.size()-1).getHeight()>=maxBorderHeight) {
                    topDown = false;
                } else if (topBorders.get(topBorders.size()-1).getHeight()<=maxBorderHeight) {
                    topDown = true;
                }
                if(topDown) {
                    topBorders.add(new TopBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                            topBorders.get(topBorders.size()-1).getX()+20,0,topBorders.get(topBorders.size()-1).getHeight()+1));
                } else {
                    topBorders.add(new TopBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                            topBorders.get(topBorders.size()-1).getX()+20,0,topBorders.get(topBorders.size()-1).getHeight()-1));
                }
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if(event.getAction()==MotionEvent.ACTION_DOWN) {
            if(!player.getPlaying()) {
                player.setPlaying(true);
            } else {
                player.setUp(true);
            }
            return true;
        }
        if(event.getAction()==MotionEvent.ACTION_UP) {
            player.setUp(false);
            return true;
        }
        return super.onTouchEvent(event);
    }
}

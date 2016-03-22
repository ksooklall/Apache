package sonetta.apache;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
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
    private boolean newGameCreated;
    private int progressDenom = 20;
    private Explosion explosion;
    private long startReset;
    private boolean reset;
    private boolean disappear;
    private boolean started;
    private int best;

    public GamePanel(Context context) {
        super(context);
        getHolder().addCallback(this);
        setFocusable(true);
    }

    public void begin() {
        disappear = false;
        topBorders.clear();
        bottomBorders.clear();
        missiles.clear();
        smokepuffs.clear();

        minBorderHeight = 5;
        maxBorderHeight = 30;

        player.setY(HEIGHT/2);
        player.resetScore();
        player.resetDY();

        if(player.getScore()>best) {
            best = player.getScore();
        }

        // Create initial topBorders to fill start screen
        for(int i = 0; i*20<WIDTH+40; i++) {
            if(i==0) {
                topBorders.add(new TopBorder(BitmapFactory.decodeResource(getResources(),R.drawable.brick),
                        i*20,0,10));
            } else {
                topBorders.add(new TopBorder(BitmapFactory.decodeResource(getResources(),R.drawable.brick),
                        i*20,0,topBorders.get(i-1).height+1));
            }
        }

        // Create initial bottomBorders to fill start screen
        for(int i = 0; i*20<WIDTH+40; i++) {
            if(i==0) { // First border created
                bottomBorders.add(new BottomBorder(BitmapFactory.decodeResource(getResources(),R.drawable.brick),
                        i*20,HEIGHT-minBorderHeight));
            } else { // Adding bordering screen is filled
                bottomBorders.add(new BottomBorder(BitmapFactory.decodeResource(getResources(),R.drawable.brick),
                        i*20,bottomBorders.get(i-1).getY()-1));
            }
        }

        newGameCreated = true;
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
                // Set thread to null so garbage collector can pick up
                thread = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void update() {
        if(player.getPlaying()) {

            if(bottomBorders.isEmpty() || topBorders.isEmpty()) {
                player.setPlaying(false);
                return;
            }

            background.update();
            player.update();

            minBorderHeight = 5+player.getScore()/progressDenom;
            maxBorderHeight = 30+player.getScore()/progressDenom;
            if(maxBorderHeight > HEIGHT/4) {
                maxBorderHeight = HEIGHT/4;
            }

            for(int i = 0; i<topBorders.size(); i++) {
                if(collision(topBorders.get(i),player)) {
                    player.setPlaying(false);
                }
            }

            for(int i = 0; i<bottomBorders.size(); i++) {
                if(collision(bottomBorders.get(i),player)) {
                    player.setPlaying(false);
                }
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
                            WIDTH+10,(int) (random.nextDouble()*(HEIGHT-(2*maxBorderHeight))+maxBorderHeight),45,15,player.getScore(),13));
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
        } else {
            if(!reset) {
                newGameCreated = false;
                startReset = System.nanoTime();
                reset = true;
                disappear = true;
                explosion = new Explosion(BitmapFactory.decodeResource(getResources(), R.drawable.explosion),
                        player.getX(),player.getY()-player.getWidth()/2,100,100,25);
            }

            explosion.update();
            long resetElapsed = (System.nanoTime()-startReset)/mili;
            if(resetElapsed>2500 && !newGameCreated) {
                this.begin();
            }
        }
    }

    public boolean collision(GameObject a, GameObject b) {
        return Rect.intersects(a.getRect(),b.getRect());
    }

    @Override
    public void draw(Canvas canvas) {

        final float scaleX = getWidth()/(WIDTH*1.f);
        final float scaleY = getHeight()/(HEIGHT*1.f);
        if(canvas!=null) {
            final int savedState = canvas.save();
            canvas.scale(scaleX, scaleY);
            background.draw(canvas);
            if(!disappear) {
                player.draw(canvas);
            }
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
            if(started) {
                explosion.draw(canvas);
            }
            this.drawText(canvas);
            canvas.restoreToCount(savedState);
        }
    }

    public void drawText(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(30);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD));
        canvas.drawText("DISTANCE: " + (player.getScore()*3),10,HEIGHT-10,paint);
        canvas.drawText("BEST: " + best, WIDTH-215, HEIGHT-10, paint);

        // Once new game is created
        if(!player.getPlaying() && newGameCreated && reset) {
            Paint paint1 = new Paint();
            paint1.setTextSize(50);
            paint1.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD));
            canvas.drawText("PRESS TO START",WIDTH/2,HEIGHT/2,paint);

            paint1.setTextSize(30);
            canvas.drawText("PRESS AND HOLD TO MOVE", WIDTH/2, HEIGHT/2+60,paint);
        }
    }

    public void updateTopBorder() {
        if(player.getScore()%50==0) {
            topBorders.add(new TopBorder(BitmapFactory.decodeResource(getResources(),R.drawable.brick),
                    topBorders.get(topBorders.size()-1).getX()+20,0,(int)(random.nextDouble()*maxBorderHeight)+1));
        }
        for(int i = 0; i<topBorders.size(); i++) {
            topBorders.get(i).update();
            // Remove topBorders once off screen
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

    public void updateBottomBorder() {
        if(player.getScore()%40==0) {
            bottomBorders.add(new BottomBorder(BitmapFactory.decodeResource(getResources(),R.drawable.brick),
                    bottomBorders.get(bottomBorders.size()-1).getX()+20,(int)(random.nextDouble()*maxBorderHeight)+
                    (HEIGHT-maxBorderHeight)));
        }
        for(int i = 0; i<bottomBorders.size(); i++) {
            // Update borders to move
            bottomBorders.get(i).update();
            // Remove bottomBorders once off screen
            if(bottomBorders.get(i).getX()<-bottomBorders.get(i).getWidth()) {
                bottomBorders.remove(i);
                if(bottomBorders.get(bottomBorders.size()-1).getY()<=HEIGHT-maxBorderHeight) {
                    botDown = true;
                } else if (bottomBorders.get(bottomBorders.size()-1).getY()>=HEIGHT-minBorderHeight) {
                    botDown = false;
                }
                if(botDown) {
                    bottomBorders.add(new BottomBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                            bottomBorders.get(bottomBorders.size()-1).getX()+20,bottomBorders.get(bottomBorders.size()-1).getY()+1));
                } else {
                    bottomBorders.add(new BottomBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                            bottomBorders.get(bottomBorders.size()-1).getX()+20,bottomBorders.get(bottomBorders.size()-1).getY()-1));
                }
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if(event.getAction()==MotionEvent.ACTION_DOWN) {
            if(!player.getPlaying()&&newGameCreated&&reset) {
                player.setPlaying(true);
                player.setUp(true);
            }
            if(player.getPlaying()) {
                if(!started) { started = true; }
                reset = false;
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

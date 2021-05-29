package com.mobina.cocorun.core;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.mobina.cocorun.R;

import java.util.ArrayList;

public class GameSurface extends SurfaceView implements SurfaceHolder.Callback {

  private GameThread gameThread;
  private Bitmap bgBitmap;
  private Coconut coco;
  private Bitmap buttonBitmap;
  private Bitmap aliveHeart;
  private Bitmap deadHeart;
  private ArrayList<Bitmap> livesBitmap;
  private ArrayList<Barrier> barriers;
  int lives = GameConfig.NUM_OF_LIVES;
  int lastHit = -1;

  public GameSurface(Context context)  {
    super(context);

    this.setFocusable(true);

    this.getHolder().addCallback(this);
  }

  public void update()  {
    if (lives == 0) {
      return;
    }
    this.coco.update();
    for (Barrier barrier : barriers) {
      barrier.update();
      if (barrier.doesHit(coco.getX(), coco.getY())) {
        int idx = barriers.indexOf(barrier);
        if (idx != lastHit) {
          lives--;
          lastHit = idx;
        }
      }
    }
    updateLives();
  }

  @Override
  public void draw(Canvas canvas)  {
    super.draw(canvas);
    if (lives == 0) {
      Paint paint = new TextPaint();
      paint.setStyle(Paint.Style.FILL);
      paint.setColor(Color.GRAY);
      canvas.drawPaint(paint);
      return;
    }
    canvas.drawBitmap(bgBitmap, 0, 0, null);
    for (Barrier barrier : barriers)
      barrier.draw(canvas);
    this.coco.draw(canvas);
    canvas.drawBitmap(buttonBitmap, getWidth() / 2 - buttonBitmap.getWidth() / 2, 0, null);
    drawScore(canvas);
    for (int i = 0; i < livesBitmap.size(); i++)
      canvas.drawBitmap(livesBitmap.get(i),
          100 * i + 20, buttonBitmap.getHeight() / 2 - aliveHeart.getHeight() / 2, null);
  }

  private void drawScore(Canvas canvas) {
    Paint paint = new TextPaint();
    paint.setStyle(Paint.Style.FILL);
    paint.setColor(Color.BLACK);
    paint.setTextSize(44);
    paint.setTypeface(Typeface.DEFAULT_BOLD);

    Paint stkPaint = new Paint();
    stkPaint.setStyle(Paint.Style.STROKE);
    stkPaint.setTextSize(44);
    stkPaint.setStrokeWidth(4);
    stkPaint.setTypeface(Typeface.DEFAULT_BOLD);
    stkPaint.setColor(Color.WHITE);
    stkPaint.setShadowLayer(2, 2, 2, Color.BLACK);

    int xOffset = (int) ((buttonBitmap.getWidth() / 10) * getResources().getDisplayMetrics().density + 0.5f);
    int yOffset = (int) (5f * getResources().getDisplayMetrics().density + 0.5f);
    canvas.drawText("Score: " + lives,
        getWidth() / 2 - buttonBitmap.getWidth() / 2 + xOffset,
        buttonBitmap.getHeight() / 2 + yOffset, stkPaint);
    canvas.drawText("Score: " + lives,
        getWidth() / 2 - buttonBitmap.getWidth() / 2 + xOffset,
        buttonBitmap.getHeight() / 2 + yOffset, paint);
  }

  @Override
  public void surfaceCreated(SurfaceHolder holder) {
    createBackground();
    createCoconut();
    createBarriers();
    createScoreBoard();
    createLives();

    this.gameThread = new GameThread(this, holder);
    this.gameThread.setRunning(true);
    this.gameThread.start();
  }

  private void createBackground() {
    bgBitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.grass);
    float scale = (float)bgBitmap.getHeight()/(float)getHeight();
    int newWidth = Math.round(bgBitmap.getWidth()/scale) + 40;
    int newHeight = Math.round(bgBitmap.getHeight()/scale);
    bgBitmap = Bitmap.createScaledBitmap(bgBitmap, newWidth, newHeight, true);
  }

  private void createCoconut() {
    Bitmap cocoBitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.coconut);
    cocoBitmap = Bitmap.createScaledBitmap(cocoBitmap, GameConfig.COCO_SIZE, GameConfig.COCO_SIZE, false);
    this.coco = new Coconut(this, cocoBitmap,100, (int) (getHeight() - 1.5 * GameConfig.COCO_SIZE));
  }

  private void createBarriers() {
    Bitmap barrierLeftBitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.barrier_left);
    Bitmap barrierRightBitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.barrier_right);
    barriers = new ArrayList<>();
    this.barriers.add(new Barrier(this, barrierLeftBitmap,
        -barrierLeftBitmap.getWidth() / 2, getHeight() / 4,
        GameConfig.BARRIER_TYPE.LEFT));
    this.barriers.add(new Barrier(this,
        barrierRightBitmap, getWidth() - barrierRightBitmap.getWidth() / 3, getHeight() / 2,
        GameConfig.BARRIER_TYPE.RIGHT));
    this.barriers.add(new Barrier(this, barrierLeftBitmap,
        -barrierLeftBitmap.getWidth() / 2, getHeight() * 3 / 4,
        GameConfig.BARRIER_TYPE.LEFT));
    this.barriers.add(new Barrier(this,
        barrierRightBitmap, getWidth() - barrierRightBitmap.getWidth() / 3, getHeight(),
        GameConfig.BARRIER_TYPE.RIGHT));
  }

  private void createScoreBoard() {
    buttonBitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.button1);
    buttonBitmap = Bitmap.createScaledBitmap(buttonBitmap, getWidth() / 3, 200, false);
  }

  private void createLives() {
    livesBitmap = new ArrayList<>();
    aliveHeart = BitmapFactory.decodeResource(this.getResources(), R.drawable.heart_alive);
    aliveHeart = Bitmap.createScaledBitmap(aliveHeart, GameConfig.HEART_SIZE, GameConfig.HEART_SIZE, false);
    deadHeart = BitmapFactory.decodeResource(this.getResources(), R.drawable.heart_dead);
    deadHeart = Bitmap.createScaledBitmap(deadHeart, GameConfig.HEART_SIZE, GameConfig.HEART_SIZE, false);
    livesBitmap.add(aliveHeart);
    livesBitmap.add(aliveHeart);
    livesBitmap.add(deadHeart);
  }

  private void updateLives() {
    for (int i = 0; i < livesBitmap.size(); i++) {
      if (i < lives) {
        livesBitmap.set(i, aliveHeart);
      } else {
        livesBitmap.set(i, deadHeart);
      }
    }
  }

  @Override
  public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

  }

  @Override
  public void surfaceDestroyed(SurfaceHolder holder) {
    boolean retry = true;
    while (retry) {
      try {
        this.gameThread.setRunning(false);

        this.gameThread.join();
      } catch (InterruptedException e)  {
        e.printStackTrace();
      }
      retry = true;
    }
  }

}

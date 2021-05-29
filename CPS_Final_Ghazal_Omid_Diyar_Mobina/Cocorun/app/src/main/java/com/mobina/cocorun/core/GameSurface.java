package com.mobina.cocorun.core;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.mobina.cocorun.R;

import java.util.ArrayList;
import java.util.Collections;

public class GameSurface extends SurfaceView implements SurfaceHolder.Callback {

  private GameThread gameThread;
  private Bitmap bgBitmap;
  private Coconut coco;
  private ArrayList<Barrier> barriers;
  Bitmap buttonBitmap;

  public GameSurface(Context context)  {
    super(context);

    this.setFocusable(true);

    this.getHolder().addCallback(this);
  }

  public void update()  {
    this.coco.update();
    for (Barrier barrier : barriers)
      barrier.update();
  }

  @Override
  public void draw(Canvas canvas)  {
    super.draw(canvas);
    canvas.drawBitmap(bgBitmap, 0, 0, null);
    for (Barrier barrier : barriers)
      barrier.draw(canvas);
    this.coco.draw(canvas);
    canvas.drawBitmap(buttonBitmap, getWidth() / 2 - buttonBitmap.getWidth() / 2, 0, null);
    drawScore(canvas);
  }

  public void drawScore(Canvas canvas) {
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
    canvas.drawText("Score: 0",
        getWidth() / 2 - buttonBitmap.getWidth() / 2 + xOffset,
        buttonBitmap.getHeight() / 2 + yOffset, stkPaint);
    canvas.drawText("Score: 0",
        getWidth() / 2 - buttonBitmap.getWidth() / 2 + xOffset,
        buttonBitmap.getHeight() / 2 + yOffset, paint);
  }

  @Override
  public void surfaceCreated(SurfaceHolder holder) {
    bgBitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.grass);
    float scale = (float)bgBitmap.getHeight()/(float)getHeight();
    int newWidth = Math.round(bgBitmap.getWidth()/scale) + 40;
    int newHeight = Math.round(bgBitmap.getHeight()/scale);
    bgBitmap = Bitmap.createScaledBitmap(bgBitmap, newWidth, newHeight, true);

    Bitmap cocoBitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.coconut);
    cocoBitmap = Bitmap.createScaledBitmap(cocoBitmap, GameConfig.COCO_SIZE, GameConfig.COCO_SIZE, false);
    this.coco = new Coconut(this, cocoBitmap,100, (int) (getHeight() - 1.5 * GameConfig.COCO_SIZE));

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

    buttonBitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.button1);
    buttonBitmap = Bitmap.createScaledBitmap(buttonBitmap, getWidth() / 3, 200, false);

    this.gameThread = new GameThread(this, holder);
    this.gameThread.setRunning(true);
    this.gameThread.start();
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

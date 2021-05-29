package com.mobina.cocorun.core;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.mobina.cocorun.R;

public class GameSurface extends SurfaceView implements SurfaceHolder.Callback {

  private GameThread gameThread;
  private Bitmap bgBitmap;
  private Coconut coco;
  private Barrier barrier;

  public GameSurface(Context context)  {
    super(context);

    this.setFocusable(true);

    this.getHolder().addCallback(this);
  }

  public void update()  {
    this.coco.update();
    this.barrier.update();
  }

  @Override
  public void draw(Canvas canvas)  {
    super.draw(canvas);
    canvas.drawBitmap(bgBitmap, 0, 0, null);
    this.barrier.draw(canvas);
    this.coco.draw(canvas);
  }

  @Override
  public void surfaceCreated(SurfaceHolder holder) {
    bgBitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.grass);
    float scale = (float)bgBitmap.getHeight()/(float)getHeight();
    int newWidth = Math.round(bgBitmap.getWidth()/scale);
    int newHeight = Math.round(bgBitmap.getHeight()/scale);
    bgBitmap = Bitmap.createScaledBitmap(bgBitmap, newWidth, newHeight, true);

    Bitmap cocoBitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.coconut);
    this.coco = new Coconut(this, cocoBitmap,100,300);

    Bitmap barrierBitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.middle);
    this.barrier = new Barrier(this, barrierBitmap,0,20);

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

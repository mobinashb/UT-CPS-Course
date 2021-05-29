package com.mobina.cocorun.core;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
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
  }

  @Override
  public void surfaceCreated(SurfaceHolder holder) {
    bgBitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.grass);
    float scale = (float)bgBitmap.getHeight()/(float)getHeight();
    int newWidth = Math.round(bgBitmap.getWidth()/scale);
    int newHeight = Math.round(bgBitmap.getHeight()/scale);
    bgBitmap = Bitmap.createScaledBitmap(bgBitmap, newWidth, newHeight, true);

    Bitmap cocoBitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.coconut);
    this.coco = new Coconut(this, cocoBitmap,100, getHeight() - 200);

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

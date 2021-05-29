package com.mobina.cocorun.core;

import android.graphics.Bitmap;
import android.graphics.Canvas;

public class Barrier extends GameObject {

  public static final float VELOCITY = GameConfig.VELOCITY;

  private long lastDrawNanoTime =-1;

  private GameSurface gameSurface;

  public Barrier(GameSurface gameSurface, Bitmap image, int x, int y) {
    super(image, x, y);

    this.gameSurface = gameSurface;
  }

  public void update()  {
    long now = System.nanoTime();

    if (lastDrawNanoTime == -1) {
      lastDrawNanoTime= now;
    }
    int deltaTime = (int) ((now - lastDrawNanoTime)/ 1000000 );

    float distance = VELOCITY * deltaTime;

    this.y = (int) (y +  distance);

    if (this.y > this.gameSurface.getHeight() - height)  {
      this.y = 20;
    }
  }

  public void draw(Canvas canvas)  {
    Bitmap bitmap = this.image;
    canvas.drawBitmap(bitmap, x, y, null);
    this.lastDrawNanoTime= System.nanoTime();
  }
}

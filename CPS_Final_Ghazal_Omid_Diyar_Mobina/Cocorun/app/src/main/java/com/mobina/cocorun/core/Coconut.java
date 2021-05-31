package com.mobina.cocorun.core;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import com.mobina.cocorun.utils.GameConfig;

public class Coconut extends GameObject {

  public static final float VELOCITY = GameConfig.VELOCITY;

  private int movingVectorX = 10;
  private int movingVectorY = 5;

  private long lastDrawNanoTime = -1;

  private GameSurface gameSurface;

  public Coconut(GameSurface gameSurface, Bitmap image, int x, int y) {
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

    double movingVectorLength = Math.sqrt(movingVectorX* movingVectorX + movingVectorY*movingVectorY);

    this.x = x +  (int)(distance* movingVectorX / movingVectorLength);

    if (this.x < 0 )  {
      this.x = 0;
      this.movingVectorX = - this.movingVectorX;
    } else if (this.x > this.gameSurface.getWidth() -width)  {
      this.x = this.gameSurface.getWidth()-width;
      this.movingVectorX = - this.movingVectorX;
    }

    if (this.y < 0 )  {
      this.y = 0;
      this.movingVectorY = - this.movingVectorY;
    } else if (this.y > this.gameSurface.getHeight()- height)  {
      this.y = this.gameSurface.getHeight()- height;
      this.movingVectorY = - this.movingVectorY ;
    }
    updateRect();
  }

  public void draw(Canvas canvas)  {
    Bitmap bitmap = this.image;
    canvas.drawBitmap(bitmap, x, y, null);
    this.lastDrawNanoTime= System.nanoTime();
  }
}

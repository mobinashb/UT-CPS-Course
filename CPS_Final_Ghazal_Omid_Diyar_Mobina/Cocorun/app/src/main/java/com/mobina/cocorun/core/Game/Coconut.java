package com.mobina.cocorun.core;

import android.graphics.Bitmap;

import com.mobina.cocorun.utils.GameConfig;

public class Coconut extends GameObject {

  public static final float VELOCITY = GameConfig.VELOCITY;

  private int movingVectorX = 10;
  private int movingVectorY = 0;

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
    int deltaTime = (int) ((now - lastDrawNanoTime)/ 1000000);

    float distance = VELOCITY * deltaTime;

    double movingVectorLength = Math.sqrt(movingVectorX* movingVectorX + movingVectorY*movingVectorY);

    x = x + (int)(distance * movingVectorX);

    if (x < 0)  {
      x = 0;
      movingVectorX = - movingVectorX;
    } else if (x > gameSurface.getWidth() -width)  {
      x = gameSurface.getWidth()-width;
      movingVectorX = - movingVectorX;
    }

    if (y < 0 )  {
      y = 0;
      movingVectorY = - movingVectorY;
    } else if (y > gameSurface.getHeight()- height)  {
      y = gameSurface.getHeight()- height;
      movingVectorY = - movingVectorY ;
    }
    updateRect();
  }

  public void setMovingVectorX(int movingVectorXNew) {
    movingVectorX = movingVectorXNew;
  }
}

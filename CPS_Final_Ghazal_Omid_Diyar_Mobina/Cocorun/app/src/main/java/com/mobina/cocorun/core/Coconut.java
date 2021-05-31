package com.mobina.cocorun.core;

import android.graphics.Bitmap;

import com.mobina.cocorun.utils.GameConfig;

public class Coconut extends GameObject {

  public static final float VELOCITY = GameConfig.VELOCITY;

  private int movingVectorX = 10;
  private int movingVectorY = 5;

  private GameSurface gameSurface;

  public Coconut(GameSurface gameSurface, Bitmap image, int x, int y) {
    super(image, x, y);

    this.gameSurface = gameSurface;
  }

  public void update(GameConfig.COMMAND command, int intensity)  {
    long now = System.nanoTime();

    if (lastDrawNanoTime == -1) {
      lastDrawNanoTime= now;
    }
    int direction = command == GameConfig.COMMAND.R ? 1 : -1;
    float distance = direction * VELOCITY * intensity;

    double movingVectorLength = Math.sqrt(movingVectorX* movingVectorX + movingVectorY*movingVectorY);

    x = x + (int)(distance * movingVectorX / movingVectorLength);

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
}

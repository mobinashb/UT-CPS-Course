package com.mobina.cocorun.core.Game;

import android.graphics.Bitmap;
import android.graphics.Rect;

import com.mobina.cocorun.utils.GameConfig;

public class Barrier extends GameObject {

  public static final float VELOCITY = GameConfig.VELOCITY;

  private GameSurface gameSurface;
  private GameConfig.BARRIER_TYPE type;
  boolean hit = false;

  public Barrier(GameSurface gameSurface, Bitmap image, int x, int y, GameConfig.BARRIER_TYPE type) {
    super(image, x, y);

    this.gameSurface = gameSurface;
    this.type = type;
  }

  public boolean update()  {
    long now = System.nanoTime();

    if (lastDrawNanoTime == -1) {
      lastDrawNanoTime = now;
    }
    int deltaTime = GameConfig.CONST_DELTA_TIME;

    float distance = VELOCITY * deltaTime;

    y = (int) (y +  distance);

    if (y > gameSurface.getHeight() + height)  {
     y = -height / 2;
     x = getRandomX();
     hit = false;
     updateRect();
     return true;
    }
    updateRect();
    return false;
  }

  private int getRandomX() {
    int maxWidth;
    if (type == GameConfig.BARRIER_TYPE.LEFT) {
      maxWidth = 0;
    } else {
      maxWidth = gameSurface.getWidth();
    }
    int maxBarLength = (int) (getWidth() * 0.6);
    int minBarLength = (int) (getWidth() * 0.4);
    return (int) (maxWidth - Math.floor(Math.random()*(maxBarLength-minBarLength+1)+minBarLength));
  }

  public boolean doesHit(Rect obj) {
    return rect.intersect(obj);
  }
}

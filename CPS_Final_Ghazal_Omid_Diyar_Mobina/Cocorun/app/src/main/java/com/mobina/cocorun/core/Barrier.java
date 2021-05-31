package com.mobina.cocorun.core;

import android.graphics.Bitmap;
import android.graphics.Rect;

import com.mobina.cocorun.utils.GameConfig;

public class Barrier extends GameObject {

  public static final float VELOCITY = GameConfig.VELOCITY / 4;

  private GameSurface gameSurface;
  private GameConfig.BARRIER_TYPE type;
  boolean hit = false;

  public Barrier(GameSurface gameSurface, Bitmap image, int x, int y, GameConfig.BARRIER_TYPE type) {
    super(image, x, y);

    this.gameSurface = gameSurface;
    this.type = type;
  }

  public void update()  {
    long now = System.nanoTime();

    if (lastDrawNanoTime == -1) {
      lastDrawNanoTime = now;
    }
//    int deltaTime = (int) ((now - lastDrawNanoTime)/ 1000000 );
    int deltaTime = 70;

    float distance = VELOCITY * deltaTime;

   y = (int) (y +  distance);

    if (y >gameSurface.getHeight() + height)  {
     y = -height / 2;
     x = getRandomX();
     hit = false;
    }
    updateRect();
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

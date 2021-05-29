package com.mobina.cocorun.core;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

public class Barrier extends GameObject {

  public static final float VELOCITY = GameConfig.VELOCITY / 3;

  private long lastDrawNanoTime =-1;

  private GameSurface gameSurface;
  private GameConfig.BARRIER_TYPE type;
  private Rect rect;
  boolean hit = false;

  public Barrier(GameSurface gameSurface, Bitmap image, int x, int y, GameConfig.BARRIER_TYPE type) {
    super(image, x, y);

    this.gameSurface = gameSurface;
    this.type = type;
    rect = new Rect();
    rect.set(x, y, x + image.getWidth(), y + image.getHeight());
  }

  public void update()  {
    long now = System.nanoTime();

    if (lastDrawNanoTime == -1) {
      lastDrawNanoTime = now;
    }
//    int deltaTime = (int) ((now - lastDrawNanoTime)/ 1000000 );
    int deltaTime = 70;

    float distance = VELOCITY * deltaTime;

    this.y = (int) (y +  distance);

    if (this.y > this.gameSurface.getHeight() + height)  {
      this.y = -height / 2;
      this.x = getRandomX();
      this.hit = false;
    }

    rect.set(x, y, x + image.getWidth(), y + image.getHeight());
  }

  public int getRandomX() {
    int maxWidth;
    if (type == GameConfig.BARRIER_TYPE.LEFT) {
      maxWidth = 0;
    } else {
      maxWidth = this.gameSurface.getWidth();
    }
    int maxBarLength = (int) (getWidth() * 0.6);
    int minBarLength = (int) (getWidth() * 0.4);
    return (int) (maxWidth - Math.floor(Math.random()*(maxBarLength-minBarLength+1)+minBarLength));
  }

  public void draw(Canvas canvas)  {
    Bitmap bitmap = this.image;
    canvas.drawBitmap(bitmap, x, y, null);
    this.lastDrawNanoTime= System.nanoTime();
//    Paint paint = new Paint();
//    paint.setColor(Color.BLUE);
//    canvas.drawRect(rect, paint);
  }

  public boolean doesHit(int x, int y) {
//    && image.getPixel(x, y) != Color.TRANSPARENT
    return rect.contains(x, y) ;
  }
}

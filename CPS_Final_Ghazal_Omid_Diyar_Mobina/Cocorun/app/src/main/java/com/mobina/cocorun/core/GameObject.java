package com.mobina.cocorun.core;

import android.graphics.Bitmap;
import android.graphics.Rect;

public abstract class GameObject {

  protected Bitmap image;
  protected final int width;
  protected final int height;
  protected int x;
  protected int y;
  protected Rect rect;

  public GameObject(Bitmap image, int x, int y)  {

    this.image = image;

    this.x = x;
    this.y = y;

    this.width = image.getWidth();
    this.height = image.getHeight();

    rect = new Rect(x, y, x + width, y + height);
  }

  public int getX()  {
    return this.x;
  }

  public int getY()  {
    return this.y;
  }

  public int setX()  {
    return this.x;
  }

  public int setY()  {
    return this.y;
  }

  public int getHeight() {
    return height;
  }

  public int getWidth() {
    return width;
  }

  public void updateRect() {
    rect.set(x, y, x + width, y + height);
  }

  public Rect getRect() { return rect; }

}

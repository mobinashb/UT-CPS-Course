package com.mobina.cocorun.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;

public class Helper {

  public static void drawStrokedText(Canvas canvas, String text, int xPos, int yPos, int fontSize) {
    Paint paint = new TextPaint();
    paint.setStyle(Paint.Style.FILL);
    paint.setColor(Color.BLACK);
    paint.setTextSize(fontSize);
    paint.setTypeface(Typeface.DEFAULT_BOLD);

    Paint stkPaint = new TextPaint();
    stkPaint.setStyle(Paint.Style.STROKE);
    stkPaint.setTextSize(fontSize);
    stkPaint.setStrokeWidth(4);
    stkPaint.setTypeface(Typeface.DEFAULT_BOLD);
    stkPaint.setColor(Color.WHITE);
    stkPaint.setShadowLayer(2, 2, 2, Color.BLACK);

    yPos -= (paint.descent() + paint.ascent()) / 2;
    paint.setTextAlign(Paint.Align.CENTER);
    stkPaint.setTextAlign(Paint.Align.CENTER);
    canvas.drawText(text,
        xPos,
        yPos, stkPaint);
    canvas.drawText(text,
        xPos,
        yPos, paint);
  }

  public static Bitmap createResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
    int width = bm.getWidth();
    int height = bm.getHeight();
    float scaleWidth = ((float) newWidth) / width;
    float scaleHeight = ((float) newHeight) / height;
    Matrix matrix = new Matrix();
    matrix.postScale(scaleWidth, scaleHeight);
    Bitmap resizedBitmap = Bitmap.createBitmap(
        bm, 0, 0, width, height, matrix, false);
    bm.recycle();
    return resizedBitmap;
  }

  public static int getDirctionFromCommand(GameConfig.COMMAND command) {
    return command == GameConfig.COMMAND.R ? 1 : -1;
  }

}

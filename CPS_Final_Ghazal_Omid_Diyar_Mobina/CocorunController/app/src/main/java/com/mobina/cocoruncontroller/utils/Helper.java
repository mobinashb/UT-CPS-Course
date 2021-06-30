package com.mobina.cocoruncontroller.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class Helper {

  public static void overrideFonts(final Context context, final View v) {
    try {
      if (v instanceof ViewGroup) {
        ViewGroup vg = (ViewGroup) v;
        for (int i = 0; i < vg.getChildCount(); i++) {
          View child = vg.getChildAt(i);
          overrideFonts(context, child);
        }
      } else if (v instanceof TextView) {
        ((TextView) v).setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/whatever_it_takes.ttf"));
      } if (v instanceof Button) {
        ((Button) v).setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/whatever_it_takes.ttf"));
      }
    } catch (Exception e) {
    }
  }

}

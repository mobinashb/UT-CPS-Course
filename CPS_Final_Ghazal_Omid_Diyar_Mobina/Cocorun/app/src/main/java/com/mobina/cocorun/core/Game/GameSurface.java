package com.mobina.cocorun.core.Game;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.preference.PreferenceManager;
import android.text.TextPaint;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.mobina.cocorun.layout.MainActivity;
import com.mobina.cocorun.utils.GameConfig;
import com.mobina.cocorun.utils.Helper;
import com.mobina.cocorun.R;

import java.util.ArrayList;

public class GameSurface extends SurfaceView implements SurfaceHolder.Callback {

  private Bitmap bgBitmap;
  private Coconut coco;
  private Bitmap buttonBitmap;
  private Bitmap aliveHeart;
  private Bitmap deadHeart;
  private ArrayList<Bitmap> livesBitmap;
  private ArrayList<Barrier> barriers;
  private int lives = GameConfig.NUM_OF_LIVES;
  private int lastHit = -1;
  private boolean gameStarted = false;
  private long timestamp = -1;
  private int score = 0;

  public GameSurface(Context context)  {
    super(context);

    this.setFocusable(true);

    this.getHolder().addCallback(this);
  }

  public void update()  {
    if (lives == 0 || !gameStarted) {
      return;
    }
//    this.coco.setMovingVectorX(Helper.getDirctionFromCommand(command) * intensity);
    this.coco.update();
    for (int i = 0; i < barriers.size(); i++) {
      barriers.get(i).update();
      if (barriers.get(i).doesHit(coco.getRect())) {
        if (i != lastHit) {
          lives--;
          MainActivity.getInstance().hitWallNotif();
          score--;
          lastHit = i;
        }
      }
      else if (hasPassedBarrier(barriers.get(i)) && lastHit != i) {
        score++;
      }
    }
    updateLives();
  }

  private boolean hasPassedBarrier(Barrier barrier) {
    return barrier.y > getHeight() + 2 * barrier.getHeight();
  }

  @Override
  public void draw(Canvas canvas)  {
    super.draw(canvas);
    if (lives == 0) {
      gameStarted = false;
      saveScore();
      drawGameOverScreen(canvas);
      return;
    }
    canvas.drawBitmap(bgBitmap, 0, 0, null);
    for (Barrier barrier : barriers)
      barrier.draw(canvas);
    this.coco.draw(canvas);
    canvas.drawBitmap(buttonBitmap, getWidth() / 2 - buttonBitmap.getWidth() / 2, 0, null);
    drawScore(canvas);
    for (int i = 0; i < livesBitmap.size(); i++)
      canvas.drawBitmap(livesBitmap.get(i),
          100 * i + 20, buttonBitmap.getHeight() / 2 - aliveHeart.getHeight() / 2, null);
  }

  private void drawGameOverScreen(Canvas canvas) {
    canvas.drawBitmap(bgBitmap, 0, 0, null);
    int xPos = (canvas.getWidth() / 2);
    int yPos = (canvas.getHeight() / 2);
    Helper.drawStrokedText(canvas, "Game Over", xPos, yPos, 72, this.getContext());
    Helper.drawStrokedText(canvas, "Highscore: " + getSavedScore(), xPos, yPos + 100, 64, this.getContext());
    Helper.drawStrokedText(canvas, "Tap to replay", xPos, yPos + 300, 54, this.getContext());
  }

  private void resetGame() {
    gameStarted = true;
    lives = GameConfig.NUM_OF_LIVES;
    score = 0;
    lastHit = -1;
    timestamp = -1;
    for (int i = 0; i < livesBitmap.size(); i++) {
      livesBitmap.set(i, aliveHeart);
    }
  }

  private void drawScore(Canvas canvas) {
    int xPos = (canvas.getWidth() / 2);
    int yPos = (buttonBitmap.getHeight() / 2);
    Helper.drawStrokedText(canvas, "Score: " + score, xPos, yPos, 44, this.getContext());
  }

  @Override
  public void surfaceCreated(SurfaceHolder holder) {
    createBackground();
    createCoconut();
    createBarriers();
    createScoreBoard();
    createLives();
  }

  private void createBackground() {
    bgBitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.grass);
    float scale = (float)bgBitmap.getHeight()/(float)getHeight();
    int newWidth = Math.round(bgBitmap.getWidth()/scale) + 40;
    int newHeight = Math.round(bgBitmap.getHeight()/scale);
    bgBitmap = Bitmap.createScaledBitmap(bgBitmap, newWidth, newHeight, true);
  }

  private void createCoconut() {
    Bitmap cocoBitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.coconut);
    cocoBitmap = Bitmap.createScaledBitmap(cocoBitmap, GameConfig.COCO_SIZE, GameConfig.COCO_SIZE, false);
    this.coco = new Coconut(this, cocoBitmap,100, (int) (getHeight() - 1.5 * GameConfig.COCO_SIZE));
  }

  private void createBarriers() {
    Bitmap barrierLeftBitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.barrier_left);
    Bitmap barrierRightBitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.barrier_right);
    barriers = new ArrayList<>();
    this.barriers.add(new Barrier(this, barrierLeftBitmap,
        -barrierLeftBitmap.getWidth() / 2, getHeight() / GameConfig.NUM_OF_BARRIERS,
        GameConfig.BARRIER_TYPE.LEFT));
    this.barriers.add(new Barrier(this,
        barrierRightBitmap, getWidth() - barrierRightBitmap.getWidth() / 3,
        getHeight() * 2 / GameConfig.NUM_OF_BARRIERS,
        GameConfig.BARRIER_TYPE.RIGHT));
    this.barriers.add(new Barrier(this, barrierLeftBitmap,
        -barrierLeftBitmap.getWidth() / 2, getHeight() * 3 / GameConfig.NUM_OF_BARRIERS,
        GameConfig.BARRIER_TYPE.LEFT));
    this.barriers.add(new Barrier(this,
        barrierRightBitmap, getWidth() - barrierRightBitmap.getWidth() / 3, getHeight(),
        GameConfig.BARRIER_TYPE.RIGHT));
  }

  private void createScoreBoard() {
    buttonBitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.button);
    buttonBitmap = Helper.createResizedBitmap(buttonBitmap, getWidth() / 3, 180);
  }

  private void createLives() {
    livesBitmap = new ArrayList<>();
    aliveHeart = BitmapFactory.decodeResource(this.getResources(), R.drawable.coconut);
    aliveHeart = Bitmap.createScaledBitmap(aliveHeart, GameConfig.HEART_SIZE, GameConfig.HEART_SIZE, false);
    deadHeart = BitmapFactory.decodeResource(this.getResources(), R.drawable.coconut_dead);
    deadHeart = Bitmap.createScaledBitmap(deadHeart, GameConfig.HEART_SIZE, GameConfig.HEART_SIZE, false);
    livesBitmap.add(aliveHeart);
    livesBitmap.add(aliveHeart);
    livesBitmap.add(aliveHeart);
  }

  private void updateLives() {
    for (int i = 0; i < livesBitmap.size(); i++) {
      if (i < lives) {
        livesBitmap.set(i, aliveHeart);
      } else {
        livesBitmap.set(i, deadHeart);
      }
    }
  }

  @Override
  public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

  }

  @Override
  public void surfaceDestroyed(SurfaceHolder holder) {
//    boolean retry = true;
//    while (retry) {
//      try {
//        this.gameThread.setRunning(false);
//
//        this.gameThread.join();
//      } catch (InterruptedException e)  {
//        e.printStackTrace();
//      }
//      retry = true;
//    }
  }

  public void processCommand(GameConfig.COMMAND cmd, int intensity, long ts) {
    if (ts != timestamp) {
      this.coco.setMovingVectorX(Helper.getDirctionFromCommand(cmd) * intensity);
      timestamp = ts;
    }
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    if (event.getAction() == MotionEvent.ACTION_DOWN) {
      resetGame();
      return true;
    }
    return false;
  }

  public int getSavedScore() {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getContext());
    return prefs.getInt("highscore", 0);
  }

  public void saveScore() {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getContext());
    SharedPreferences.Editor editor = prefs.edit();
    editor.putInt("highscore", score);
    editor.commit();
  }
}

package com.mobina.legendofbounca.core.components;

import android.util.Pair;

import static com.mobina.legendofbounca.core.config.GameConfig.*;

public class Box {
    private int width;
    private int height;
    public Box(Pair size) {
        this.width = (int)size.first;
        this.height = (int)size.second;
    }
    public boolean[] checkWallCollision(_3dVector newPos) {
        boolean[] wallCollided = {false, false};
        if (newPos.x - BALL_RADIUS <= 0 || newPos.x + BALL_RADIUS >= height) {
            wallCollided[0] = true;
        }
        if (newPos.y - BALL_RADIUS <= 0 || newPos.y + BALL_RADIUS >= width) {
            wallCollided[1] = true;
        }
        return wallCollided;
    }
}

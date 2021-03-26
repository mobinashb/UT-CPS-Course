package com.mobina.legendofbounca.core.components;

import static com.mobina.legendofbounca.core.config.GameConfig.*;

public class Box {
    public boolean[] checkWallCollision(_3dVector newPos) {
        boolean[] wallCollided = {false, false};
        if (newPos.x-BALL_RADIUS <= 0 || newPos.x+BALL_RADIUS >= BOX_LENGTH) {
            wallCollided[0] = true;
        }
        if (newPos.y-BALL_RADIUS <= 0 || newPos.y+BALL_RADIUS >= BOX_WIDTH) {
            wallCollided[1] = true;
        }
        return wallCollided;
    }
}

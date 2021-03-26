package components;

import static Configs.gameConfigs.*;

public class Board {
    public boolean[] checkWallCollision(_3dVector newPos){
        boolean[] wallCollided = {false, false};
        if (newPos.x-BALL_RADIUS <= 0 || newPos.x+BALL_RADIUS >= BOARD_LENGTH ){
            wallCollided[0] = true;
        }
        if (newPos.y-BALL_RADIUS <= 0 || newPos.y+BALL_RADIUS >= BOARD_WIDTH ){
            wallCollided[1] = true;
        }
        return wallCollided;
    }
}

package com.mobina.legendofbounca.core.components;

import java.lang.Math;


public class Ball {
    _3dVector position;
    _3dVector velocity;
    _3dVector acceleration;

    public Ball(_3dVector x){
        this.position = x;
    }

    public Ball(_3dVector x, _3dVector v, _3dVector a) {
        this.position = x;
        this.velocity = v;
        this.acceleration = a;
    }

    public void updatePosition(double deltaT) {
        _3dVector amountToAdd1 = acceleration.multiplyVectorByNum(0.5*(Math.pow(deltaT, 2)));
        _3dVector amountToAdd2 = velocity.multiplyVectorByNum(deltaT);
        amountToAdd1.vectorAddition(amountToAdd2);
        position.vectorAddition(amountToAdd1);
    }

    public void updateVelocity(double deltaT) {
        _3dVector amountToAdd = acceleration.multiplyVectorByNum(deltaT);
        velocity.vectorAddition(amountToAdd);
    }

    public void handleWallCollision(_3dVector newPos, Box box) {
        boolean[] wallCollided = box.checkWallCollision(newPos);
        if (wallCollided[0]) {
            velocity.x = -velocity.x;
        }
        if (wallCollided[1]) {
            velocity.y = -velocity.y;
        }
    }
}

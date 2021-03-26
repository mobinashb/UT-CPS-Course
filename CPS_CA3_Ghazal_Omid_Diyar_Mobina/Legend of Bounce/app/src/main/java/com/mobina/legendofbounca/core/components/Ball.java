package com.mobina.legendofbounca.core.components;

import android.widget.ImageView;

import com.mobina.legendofbounca.core.config.GameConfig;
import com.mobina.legendofbounca.core.config.GamePhysicsConfig;

import java.lang.Math;


public class Ball {
    private _3dVector position;
    private _3dVector velocity;
    private _3dVector acceleration;
    private ImageView imgView;
    private _3dVector theta;

    public Ball(_3dVector x){
        this.position = x;
    }

    public Ball(_3dVector x, _3dVector v, _3dVector a, ImageView imgView) {
        this.position = x;
        this.velocity = v;
        this.acceleration = a;
        this.imgView = imgView;
        this.theta = new _3dVector(0, 0, 0);
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

    public void updateImgView() {
        imgView.setX((float) position.x);
        imgView.setY((float) position.y);
    }

    public void handleSensorEvent(_3dVector vec, GameConfig.sensor sensor, double deltaT) {
        if (sensor == GameConfig.sensor.GYROSCOPE) {
            handleGyroscopeSensorEvent(vec, deltaT);
        } else {
            handleGravitySensorEvent(vec, deltaT);
        }
    }

    public void handleGyroscopeSensorEvent(_3dVector vec, double deltaT) {
        theta = new _3dVector(vec.x * deltaT + theta.x,
            vec.y * deltaT + theta.y,
            vec.z * deltaT + theta.z);
        _3dVector gravityVec = new _3dVector(
            GamePhysicsConfig.earthGravity * Math.sin(theta.x),
            GamePhysicsConfig.earthGravity * Math.sin(theta.y),
            GamePhysicsConfig.earthGravity * Math.sin(theta.z));

        handleGravitySensorEvent(gravityVec, deltaT);
    }

    public void handleGravitySensorEvent(_3dVector vec, double deltaT) {
        vec.x = -vec.x;
        double nx = vec.x * (GameConfig.BALL_WEIGHT / 1000);
        double ny = vec.y * (GameConfig.BALL_WEIGHT / 1000);
        double nF = vec.z * (GameConfig.BALL_WEIGHT / 1000);

        double frictionS, frictionM, frictionMnx, frictionMny;
        frictionM = nF * GamePhysicsConfig.Uk;
        frictionS = nF * GamePhysicsConfig.Us;

        frictionMnx = (vec.x > 0) ?  - frictionM  : frictionM ;
        frictionMny = (vec.y > 0) ?  - frictionM :  frictionM;

        double ax, ay;
        ax = vec.x + (frictionMnx / GameConfig.BALL_WEIGHT / 1000);
        ay = vec.y + (frictionMny / GameConfig.BALL_WEIGHT / 1000);

        boolean canMoveX = false;
        if (velocity.x < 5 && velocity.x > -5) {
            if (Math.abs(nx) > Math.abs(frictionS)) {
                canMoveX = true;
            }
        } else {
            canMoveX = true;
        }

        boolean canMoveY = false;
        if (velocity.y < 5 && velocity.y > -5)
        {
            if(Math.abs(ny) > Math.abs(frictionS)) {
                canMoveY = true;
            }
        } else {
            canMoveY = true;
        }

        if (canMoveX) {
            double xNew = ((0.5 * ax * deltaT * deltaT) + (velocity.x * deltaT)) + position.x;
            if (((xNew + GameConfig.BALL_RADIUS / 2) > GameConfig.BOX_WIDTH / 2) ||
                ((xNew - GameConfig.BALL_RADIUS / 2) < -GameConfig.BOX_WIDTH / 2)) {
                velocity.x = 0;
            }
            else {
                position.x = xNew ;
                velocity.x = ax * deltaT + velocity.x;
            }
        }
        else {
            velocity.x = 0;
        }

        if (canMoveY) {
            double yNew = ((0.5 * ay * deltaT * deltaT) + (velocity.y * deltaT)) + position.y;
            if (((yNew + GameConfig.BALL_RADIUS / 2) > GameConfig.BOX_LENGTH / 2) ||
                ((yNew - GameConfig.BALL_RADIUS / 2) < -GameConfig.BOX_LENGTH / 2)) {
                velocity.y = 0;
            } else {
                position.y = yNew;
                velocity.y = ay * deltaT + velocity.y;
            }

        }
        else {
            velocity.y = 0;
        }
    }
}

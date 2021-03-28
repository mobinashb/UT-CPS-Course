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
    private Box box;

    public Ball(_3dVector x){
        this.position = x;
    }

    public Ball(_3dVector x, _3dVector v, _3dVector a, ImageView imgView, Box box) {
        this.position = x;
        this.velocity = v;
        this.acceleration = a;
        this.imgView = imgView;
        this.theta = new _3dVector(0, 0, 0);
        this.box = box;
    }

    private void updatePosition(double deltaT) {
        _3dVector amountToAdd1 = acceleration.multiplyVectorByNum(0.5*(Math.pow(deltaT, 2)));
        _3dVector amountToAdd2 = velocity.multiplyVectorByNum(deltaT);
        amountToAdd1.vectorAddition(amountToAdd2);
        position.vectorAddition(amountToAdd1);
    }

    private void updateVelocity(double deltaT) {
        _3dVector amountToAdd = acceleration.multiplyVectorByNum(deltaT);
        velocity.vectorAddition(amountToAdd);
    }

    private boolean handleWallCollision() {
        boolean[] wallCollided = box.checkWallCollision(position);
        if (wallCollided[0]) {
            velocity.x = -velocity.x;
            return true;
        }
        if (wallCollided[1]) {
            velocity.y = -velocity.y;
            return true;
        }
        return false;
    }

    public void updateImgView() {
        imgView.setX((float) position.x);
        imgView.setY((float) position.y);
    }

    public void handleSensorEvent(_3dVector vec, GameConfig.sensor sensor, double deltaT) {
//        System.out.println("x: " + position.x + " y: " + position.y);
        if (sensor == GameConfig.sensor.GYROSCOPE) {
            handleGyroscopeSensorEvent(vec, deltaT);
        } else {
            handleGravitySensorEvent(vec, deltaT);
        }
        handlePhysics();
        updateVelocity(deltaT);
        updatePosition(deltaT);
        boolean collided = handleWallCollision();
        if (collided) updatePosition(deltaT);
        updateImgView();
    }

    private void handlePhysics() {
        double fX = Math.sin(theta.y) * GamePhysicsConfig.earthGravity * GameConfig.BALL_WEIGHT;
        double fY = Math.sin(theta.x) * GamePhysicsConfig.earthGravity * GameConfig.BALL_WEIGHT;
        double N = Math.cos(Math.atan(
            Math.sqrt(Math.pow(Math.sin(theta.x), 2) + Math.pow(Math.sin(theta.y), 2))
                / (Math.cos(theta.x) + Math.cos(theta.y))))
            * GamePhysicsConfig.earthGravity * GameConfig.BALL_WEIGHT;;

        if (!this.isStopped() || this.canMove(fX, fY, N)) {
            double frictionMagnitude = N * GamePhysicsConfig.Uk;
            double frictionX = 0;
            double frictionY = 0;
            double len = Math.sqrt(Math.pow(velocity.x, 2) + Math.pow(velocity.y, 2));
            if (len > 0) {
                frictionX = frictionMagnitude * velocity.x / len;
                frictionY = frictionMagnitude * velocity.y / len;
            }
            fX += -Math.signum(velocity.x) * Math.abs(frictionX);
            fY += -Math.signum(velocity.y) * Math.abs(frictionY);
        } else {
            fX = 0;
            fY = 0;
        }
        acceleration.x = (fX / GameConfig.BALL_WEIGHT) * GameConfig.ACCELERATION_FACTOR;
        acceleration.y = (fY / GameConfig.BALL_WEIGHT) * GameConfig.ACCELERATION_FACTOR;
    }

    private void handleGyroscopeSensorEvent(_3dVector vec, double deltaT) {
        theta = new _3dVector(vec.x * deltaT + theta.x,
            vec.y * deltaT + theta.y,
            vec.z * deltaT + theta.z);
//        _3dVector gravityVec = new _3dVector(
//            GamePhysicsConfig.earthGravity * Math.sin(theta.x),
//            GamePhysicsConfig.earthGravity * Math.sin(theta.y),
//            GamePhysicsConfig.earthGravity * Math.sin(theta.z));
//
//        handleGravitySensorEvent(gravityVec, deltaT);
    }

    private void handleGravitySensorEvent(_3dVector vec, double deltaT) {
        vec.x = -vec.x;
        theta = new _3dVector(Math.asin(vec.x / GamePhysicsConfig.earthGravity),
            Math.asin(vec.y / GamePhysicsConfig.earthGravity),
            Math.asin(vec.z / GamePhysicsConfig.earthGravity));

//        vec.x = -vec.x;
//        double nx = vec.x * (GameConfig.BALL_WEIGHT / 1000);
//        double ny = vec.y * (GameConfig.BALL_WEIGHT / 1000);
//        double nF = vec.z * (GameConfig.BALL_WEIGHT / 1000);
//
//        double frictionS, frictionM, frictionMnx, frictionMny;
//        frictionM = nF * GamePhysicsConfig.Uk;
//        frictionS = nF * GamePhysicsConfig.Us;
//
//        frictionMnx = (vec.x > 0) ?  - frictionM  : frictionM ;
//        frictionMny = (vec.y > 0) ?  - frictionM :  frictionM;
//
//        double ax, ay;
//        ax = vec.x + (frictionMnx / (GameConfig.BALL_WEIGHT / 1000));
//        ay = vec.y + (frictionMny / (GameConfig.BALL_WEIGHT / 1000));
//
//        boolean canMoveX = false;
//        if (velocity.x < 5 && velocity.x > -5) {
//            if (Math.abs(nx) > Math.abs(frictionS)) {
//                canMoveX = true;
//            }
//        } else {
//            canMoveX = true;
//        }
//
//        boolean canMoveY = false;
//        if (velocity.y < 5 && velocity.y > -5)
//        {
//            if (Math.abs(ny) > Math.abs(frictionS)) {
//                canMoveY = true;
//            }
//        } else {
//            canMoveY = true;
//        }
//
//        if (canMoveX) {
//            double xNew = ((0.5 * ax * deltaT * deltaT) + (velocity.x * deltaT)) + position.x;
//            if (((xNew + GameConfig.BALL_RADIUS / 2) > GameConfig.BOX_WIDTH / 2) ||
//                ((xNew - GameConfig.BALL_RADIUS / 2) < -GameConfig.BOX_WIDTH / 2)) {
//                velocity.x = 0;
//            }
//            else {
//                position.x = xNew ;
//                velocity.x = ax * deltaT + velocity.x;
//            }
//        }
//        else {
//            velocity.x = 0;
//        }
//
//        if (canMoveY) {
//            double yNew = ((0.5 * ay * deltaT * deltaT) + (velocity.y * deltaT)) + position.y;
//            if (((yNew + GameConfig.BALL_RADIUS / 2) > GameConfig.BOX_LENGTH / 2) ||
//                ((yNew - GameConfig.BALL_RADIUS / 2) < -GameConfig.BOX_LENGTH / 2)) {
//                velocity.y = 0;
//            } else {
//                position.y = yNew;
//                velocity.y = ay * deltaT + velocity.y;
//            }
//
//        }
//        else {
//            velocity.y = 0;
//        }
    }

    private boolean canMove(double fX, double fY, double N) {
        double fMagnitude = Math.sqrt(Math.pow(fX, 2) + Math.pow(fY, 2));
        double frictionMagnitude = N * GamePhysicsConfig.Uk;
        return fMagnitude > frictionMagnitude;
    }

    private boolean isStopped() {
        return Math.sqrt(Math.pow(velocity.x, 2) + Math.pow(velocity.y, 2)) <
            GameConfig.BALL_STOP_SPEED;
    }
}

package com.mobina.legendofbounca.core.components;

import android.util.Pair;
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
    private int width;
    private int height;
    private float radius;

    public Ball(_3dVector x){
        this.position = x;
    }

    public Ball(_3dVector x, _3dVector v, _3dVector a,
                ImageView imgView, Pair displaySize, float radius) {
        this.position = x;
        this.velocity = v;
        this.acceleration = a;
        this.imgView = imgView;
        this.theta = new _3dVector(0, 0, 0);
        this.width = (int)displaySize.first;
        this.height = (int)displaySize.second;
        this.radius = radius;
    }

    private _3dVector getNextPosition(double deltaT) {
        _3dVector amountToAdd1 = acceleration.multiplyVectorByNum(0.5*(Math.pow(deltaT, 2)));
        _3dVector amountToAdd2 = velocity.multiplyVectorByNum(deltaT);
        amountToAdd1.vectorAddition(amountToAdd2);
        return new _3dVector(position.x+amountToAdd1.x,
            position.y+amountToAdd1.y,
            position.z+amountToAdd1.z);
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

    public boolean checkWallCollision(double x, double y) {
        return x >= width || x <= 0 || y >= height || y <= 0;
    }

    private void handleWallCollision(_3dVector position) {
        if (checkWallCollision(position.x + radius, position.y)) {
            velocity.y = Math.abs(velocity.y);
        }
        if (checkWallCollision(position.x + radius,
            position.y + radius * 2)) {
            velocity.y = -Math.abs(velocity.y);
        }
        if (checkWallCollision(position.x, position.y + radius)) {
            velocity.x = Math.abs(velocity.x);
        }
        if (checkWallCollision(position.x + radius * 2,
            position.y + radius)) {
            velocity.x = -Math.abs(velocity.x);
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
        handlePhysics();
        updateVelocity(deltaT);
        _3dVector nextPosition = getNextPosition(deltaT);
        handleWallCollision(nextPosition);
        handlePhysics();
        updateVelocity(deltaT);
        updatePosition(deltaT);
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
    }

    private void handleGravitySensorEvent(_3dVector vec, double deltaT) {
        vec.x = -vec.x;
        theta = new _3dVector(Math.asin(vec.x / GamePhysicsConfig.earthGravity),
            Math.asin(vec.y / GamePhysicsConfig.earthGravity),
            Math.asin(vec.z / GamePhysicsConfig.earthGravity));
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

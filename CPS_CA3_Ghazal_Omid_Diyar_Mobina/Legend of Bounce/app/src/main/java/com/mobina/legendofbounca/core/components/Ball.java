package com.mobina.legendofbounca.core.components;

import android.util.Pair;
import android.widget.ImageView;

import com.mobina.legendofbounca.core.utils.RandomGenerator;
import com.mobina.legendofbounca.core.config.GameConfig;
import com.mobina.legendofbounca.core.config.GamePhysicsConfig;

import java.lang.Math;


public class Ball {
    private _3dVector position;
    private _3dVector velocity;
    private _3dVector acceleration;
    private ImageView imgView;
    private _3dVector theta;
    private int displayWidth;
    private int displayHeight;
    private float radius;
    private float leftMarginSize;

    public Ball(_3dVector x, _3dVector v, _3dVector a,
                ImageView imgView, Pair displaySize, float radius, float leftMarginSize) {
        this.position = x;
        this.velocity = v;
        this.acceleration = a;
        this.imgView = imgView;
        this.theta = new _3dVector(0, 0, 0);
        this.displayWidth = (int)displaySize.first;
        this.displayHeight = (int)displaySize.second;
        this.radius = radius;
        this.leftMarginSize = leftMarginSize;
    }

    private _3dVector getNextPosition(double deltaT) {
        _3dVector amountToAdd1 = acceleration.multiplyVectorByNum(0.5*(Math.pow(deltaT, 2)));
        _3dVector amountToAdd2 = velocity.multiplyVectorByNum(deltaT);
        amountToAdd1.vectorAddition(amountToAdd2);
        return new _3dVector(position.x + amountToAdd1.x,
            position.y + amountToAdd1.y,
            position.z + amountToAdd1.z);
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

    public void generateRandomVelocity() {
        double randomFloat = Math.random();
        int randomSign = (randomFloat > 0.5) ? 1 : -1;
        velocity = RandomGenerator.random3dVector(GameConfig.RANDOM_VELOCITY_LOW,
            GameConfig.RANDOM_VELOCITY_HIGH,
            GameConfig.RANDOM_VELOCITY_LOW,
            GameConfig.RANDOM_VELOCITY_HIGH, 0, 0);
        velocity.y *= randomSign;
    }

    public boolean checkWallCollision(_3dVector position) {
        boolean xCollided = (position.x <= leftMarginSize) ||
            (position.x >= displayWidth);
        boolean yCollided = (position.y <= -radius / 2) ||
            (position.y >= displayHeight);
        return xCollided || yCollided;
    }

    private boolean handleWallCollision(_3dVector position) {
        boolean collided = false;
        if (checkWallCollision(new _3dVector(position.x + radius, position.y, 0))) {
            velocity.y = Math.abs(velocity.y);
            collided = true;
        }
        if (checkWallCollision(new _3dVector(position.x, position.y + radius, 0))) {
            velocity.x = Math.abs(velocity.x);
            collided = true;
        }
        if (checkWallCollision(new _3dVector(position.x + radius,
            position.y + radius * 2, 0))) {
            velocity.y = -Math.abs(velocity.y);
            collided = true;
        }
        if (checkWallCollision(new _3dVector(position.x + radius * 2,
            position.y + radius, 0))) {
            velocity.x = -Math.abs(velocity.x);
            collided = true;
        }
        if (collided) {
            double kineticEnergyReductionFactor = 3/Math.sqrt(10);
            velocity = velocity.multiplyVectorByNum(kineticEnergyReductionFactor);
        }
        return collided;
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
        boolean collided = handleWallCollision(nextPosition);
        if (collided) {
            handlePhysics();
            updateVelocity(deltaT);
        }
        updatePosition(deltaT);
        updateImgView();
    }

    private _3dVector getForces() {
        double fX = Math.sin(theta.y) * GamePhysicsConfig.earthGravity * GameConfig.BALL_WEIGHT;
        double fY = Math.sin(theta.x) * GamePhysicsConfig.earthGravity * GameConfig.BALL_WEIGHT;
        return new _3dVector(fX, fY, 0);
    }

    private double getN() {
        double N = Math.cos(Math.atan(
            Math.sqrt(Math.pow(Math.sin(theta.x), 2) + Math.pow(Math.sin(theta.y), 2))
                / (Math.cos(theta.x) + Math.cos(theta.y))))
            * GamePhysicsConfig.earthGravity * GameConfig.BALL_WEIGHT;
        return N;
    }

    private _3dVector handleFriction(_3dVector F, double N) {
        if (!this.isStopped() || this.canMove(F, N)) {
            double frictionMagnitude = N * GamePhysicsConfig.Uk;
            double velocitySize = velocity.getSize();
            double frictionX = 0;
            double frictionY = 0;
            if (velocitySize > 0) {
                frictionX = frictionMagnitude * velocity.x / velocitySize;
                frictionY = frictionMagnitude * velocity.y / velocitySize;
            }
            F.x += -Math.signum(velocity.x) * Math.abs(frictionX);
            F.y += -Math.signum(velocity.y) * Math.abs(frictionY);
        } else {
            F.x = 0;
            F.y = 0;
        }
        return F;
    }

    private void updateAcceleration(_3dVector F) {
        acceleration.x = (F.x / GameConfig.BALL_WEIGHT) * GameConfig.ACCELERATION_FACTOR;
        acceleration.y = (F.y / GameConfig.BALL_WEIGHT) * GameConfig.ACCELERATION_FACTOR;
    }

    private void handlePhysics() {
        _3dVector F = getForces();
        double N = getN();
//        F = handleFriction(F, N);
        updateAcceleration(F);
    }

    private void handleGyroscopeSensorEvent(_3dVector vec, double deltaT) {
        theta = new _3dVector(vec.x * deltaT + theta.x,
            vec.y * deltaT + theta.y,
            vec.z * deltaT + theta.z);
    }

    private void handleGravitySensorEvent(_3dVector vec, double deltaT) {
        theta = new _3dVector(Math.asin(vec.y / GamePhysicsConfig.earthGravity),
            Math.asin(-vec.x / GamePhysicsConfig.earthGravity),
            Math.asin(vec.z / GamePhysicsConfig.earthGravity));
    }

    private boolean canMove(_3dVector f, double N) {
        double fMagnitude = f.getSize();
        double frictionMagnitude = N * GamePhysicsConfig.Us;
        return fMagnitude > frictionMagnitude;
    }

    private boolean isStopped() {
        return velocity.getSize() < GameConfig.BALL_STOP_SPEED;
    }
}

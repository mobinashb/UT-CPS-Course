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

    public Ball(_3dVector x, _3dVector v, _3dVector a,
                ImageView imgView, Pair<Integer, Integer> displaySize, float radius) {
        this.position = x;
        this.velocity = v;
        this.acceleration = a;
        this.imgView = imgView;
        this.theta = new _3dVector(0, 0, 0);
        this.displayWidth = displaySize.first;
        this.displayHeight = displaySize.second;
        this.radius = radius;
    }

    private _3dVector getNextPosition(double deltaT) {
        _3dVector amountToAdd1 = acceleration.multiplyVectorByNum(0.5*(Math.pow(deltaT, 2)));
        _3dVector amountToAdd2 = velocity.multiplyVectorByNum(deltaT);
        amountToAdd1.vectorAddition(amountToAdd2);
        return new _3dVector(position.x + amountToAdd1.x,
            position.y + amountToAdd1.y,
            position.z + amountToAdd1.z);
    }

    private void updateVelocity(double deltaT) {
        _3dVector amountToAdd = acceleration.multiplyVectorByNum(deltaT);
        velocity.vectorAddition(amountToAdd);
    }

    private void updateAcceleration(_3dVector F) {
        acceleration.x = (F.x / GameConfig.BALL_WEIGHT) * GameConfig.ACCELERATION_FACTOR;
        acceleration.y = (F.y / GameConfig.BALL_WEIGHT) * GameConfig.ACCELERATION_FACTOR;
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
        boolean xCollided = (position.x <= 0) ||
            (position.x >= displayWidth);
        boolean yCollided = (position.y <= 0) ||
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
            velocity = velocity.multiplyVectorByNum(GamePhysicsConfig.kineticEnergyReductionFactor);
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
        position = getNextPosition(deltaT);
        updateImgView();
    }

    private void handlePhysics() {
        _3dVector F = getForces();
        double N = getN();
        F = handleFriction(F, N);
        updateAcceleration(F);
    }

    private _3dVector getForces() {
        double fX = GamePhysicsConfig.earthGravity * GameConfig.BALL_WEIGHT * Math.sin(theta.y);
        double fY = GamePhysicsConfig.earthGravity * GameConfig.BALL_WEIGHT * Math.sin(theta.x);
        return new _3dVector(fX, fY, 0);
    }

    private double getN() {
        _3dVector sinTheta = new _3dVector(Math.sin(theta.x), Math.sin(theta.y), 0);
        double N = GamePhysicsConfig.earthGravity * GameConfig.BALL_WEIGHT *
        Math.cos(Math.atan(sinTheta.getSize() / (Math.cos(theta.x) + Math.cos(theta.y))));
        return N;
    }

    private _3dVector handleFriction(_3dVector F, double N) {
        if (((position.x == 0) ||
            (position.x == displayWidth) && Math.abs(velocity.y) < GameConfig.BALL_STOP_SPEED) ||
            ((position.y == 0) || (position.y == displayHeight) &&
                Math.abs(velocity.x) < GameConfig.BALL_STOP_SPEED)) {
            if (canMove(F, N)) {
                double frictionSize = N * GamePhysicsConfig.Uk;
                double velocitySize = velocity.getSize();
                double frictionX = 0;
                double frictionY = 0;
                if (velocitySize > 0) {
                    frictionX = frictionSize * velocity.x / velocitySize;
                    frictionY = frictionSize * velocity.y / velocitySize;
                }
                if (velocity.x != 0)
                    F.x += velocity.x > 0 ? -Math.abs(frictionX): Math.abs(frictionX);
                if (velocity.y != 0)
                    F.y += velocity.y > 0 ? -Math.abs(frictionY): Math.abs(frictionY);
            }
            else {
                F = new _3dVector(0, 0, 0);
            }
        }
        return F;
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
        double frictionSize = N * GamePhysicsConfig.Us;
        return f.getSize() > frictionSize;
    }
}

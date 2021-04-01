package com.mobina.legendofbounca.core.config;

public class GameConfig {
    public enum sensor {GYROSCOPE, GRAVITY};

    public static final double BALL_WEIGHT = 0.01;
    public static final float BALL_RADIUS = 40;

    public static final int REFRESH_RATE = 10;

    public static final int ACCELERATION_FACTOR = 20;

    public static final int RANDOM_VELOCITY_HIGH = 240;
    public static final int RANDOM_VELOCITY_LOW = 120;

    public static final int BALL_STOP_SPEED = 5;

    public static final float JUMP_BTN_SIZE = 60;
}

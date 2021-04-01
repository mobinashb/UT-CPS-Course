package com.mobina.legendofbounca.core.utils;

import com.mobina.legendofbounca.core.components._3dVector;

public final class RandomGenerator {
    public static double  generateRandomNum(double low, double high){
        return Math.floor((Math.random()*(high-low+1) + low));
    }
    public static _3dVector random3dVector(double xLow, double xHigh, double yLow, double yHigh, double zLow, double zHigh){
        return new _3dVector(generateRandomNum(xLow, xHigh), generateRandomNum(yLow, yHigh), generateRandomNum(zLow, zHigh));
    }

}

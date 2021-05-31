package com.mobina.cocoruncontroller;

public class _3dVector {
    public Double x, y, z;

    public _3dVector(double x, double y, double z, double scaling_factor) {
        this.x = x * scaling_factor;
        this.y = y * scaling_factor;
        this.z = z * scaling_factor;
    }

    public _3dVector(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void vectorAddition(_3dVector newVec) {
        this.x += newVec.x;
        this.y += newVec.y;
        this.z += newVec.z;
    }

    public _3dVector multiplyVectorByNum(double number) {
        return new _3dVector(this.x * number, this.y * number, this.z * number);
    }
}

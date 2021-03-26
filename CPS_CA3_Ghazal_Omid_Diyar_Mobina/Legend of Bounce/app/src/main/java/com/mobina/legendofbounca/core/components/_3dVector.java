package components;

public class _3dVector {
    double x, y, z;

    public _3dVector(double x, double y, double z){
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void vectorAddition(_3dVector newVec){
        this.x += newVec.x;
        this.y += newVec.y;
        this.z += newVec.z;
    }

    public _3dVector multiplyVectorByNum(double number){
        return new _3dVector(this.x*number, this.y*number, this.z*number);
    }
}

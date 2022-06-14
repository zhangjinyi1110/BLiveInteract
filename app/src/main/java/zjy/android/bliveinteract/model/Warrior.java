package zjy.android.bliveinteract.model;

import android.graphics.PointF;

import java.util.Random;

public class Warrior {

    private float speed;
    private double xSpeed;
    private double ySpeed;
    private float angle;
    private float radius;
    private int nation;
    private final PointF currPoint;
    private boolean attacked = false;
    private final Random random = new Random();

    public Warrior(Territory territory) {
        this.speed = 3;
        this.radius = 25;
        this.nation = territory.nation;
        this.currPoint = new PointF(territory.rectF.centerX(), territory.rectF.centerY());
        int angle;
        do {
            angle = random.nextInt(360);
        } while (angle % 90 == 0);
        this.angle = angle;
        updateSpeed();
    }

    private void updateSpeed() {
        float wx = currPoint.x;
        float wy = currPoint.y;
        double k = Math.tan(Math.toRadians(angle));
        double b = wy - k * wx;
        double l = speed;
        double A = 1 + k * k;
        double B = -2 * wx + 2 * b * k - 2 * k * wy;
        double C = wx * wx + b * b - 2 * b * wy + wy * wy - l * l;
        double sqrt = Math.sqrt(B * B - 4 * A * C);
        double sx = (-B + sqrt) / (2 * A);
        double xAbs = Math.abs(sx - wx);
        if (angle < 90 || angle > 270) {
            xSpeed = -xAbs;
        } else {
            xSpeed = xAbs;
        }
        double yAbs = Math.abs(k * sx + b - wy);
        if (angle < 180) {
            ySpeed = -yAbs;
        } else {
            ySpeed = yAbs;
        }
    }

    public float getSpeed() {
        return speed;
    }

    public double getXSpeed() {
        return xSpeed;
    }

    public double getYSpeed() {
        return ySpeed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
        updateSpeed();
    }

    public float getAngle() {
        return angle;
    }

    public void setAngle(float angle) {
        this.angle = angle;
        updateSpeed();
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public int getNation() {
        return nation;
    }

    public void setNation(int nation) {
        this.nation = nation;
    }

    public void setCurrPoint(float x, float y) {
        this.currPoint.x = x;
        this.currPoint.y = y;
        this.attacked = false;
    }

    public float getX() {
        return currPoint.x;
    }

    public float getY() {
        return currPoint.y;
    }

    public void updateAngle(int type) {
        if (type == 1) {
            setAngle(360 - angle);
        } else if (type == 3) {
            setAngle(360 - angle);
        } else if (type == 2) {
            if (angle < 180) {
                setAngle(180 - angle);
            } else {
                setAngle(360 - angle + 180);
            }
        } else if (type == 0) {
            if (angle < 180) {
                setAngle(180 - angle);
            } else {
                setAngle(360 - angle + 180);
            }
        } else {
            int angle;
            do {
                angle = random.nextInt(90);
            } while (angle % 90 == 0);
            if (this.angle < 90) {
                angle += 180;
            } else if (this.angle < 180) {
                angle += 180;
            } else if (this.angle < 270) {
                angle = 90 - angle;
            } else {
                angle = 180 - angle;
            }
            setAngle(angle);
        }

        this.attacked = true;
    }

    public boolean isAttacked() {
        return attacked;
    }
}

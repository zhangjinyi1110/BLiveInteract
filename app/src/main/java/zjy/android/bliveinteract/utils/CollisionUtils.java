package zjy.android.bliveinteract.utils;

import android.graphics.RectF;

import zjy.android.bliveinteract.model.Territory;
import zjy.android.bliveinteract.model.Warrior;

public class CollisionUtils {

    //上左 上 上右
    // 左  中  右
    //下左 下 下右

    //1 2 3
    //4 5 6
    //7 8 9

    public static int shouldCollision(Territory territory, Warrior warrior) {
        float wr =  warrior.getRadius(), wx = warrior.getX(), wy = warrior.getY();
        float minWx = wx - wr, maxWx = wx + wr, minWy = wy - wr, maxWy = wy + wr;
        RectF rectF = territory.rectF;
        float minTx = rectF.left, maxTx = rectF.right, minTy = rectF.top, maxTy = rectF.bottom;

        boolean br = circleContains(wx, wy, wr, maxTx, maxTy);
        boolean bl = circleContains(wx, wy, wr, minTx, maxTy);
        boolean tr = circleContains(wx, wy, wr, maxTx, minTy);
        boolean tl = circleContains(wx, wy, wr, minTx, minTy);

        if (tl && tr && bl && br) {
            return 5;
        }

        boolean top = minTy <= minWy && minWy <= maxTy;
        if (top) {
            if (br && bl) {
                return 2;
            } else if (br) {
                return 1;
            } else if (bl) {
                return 3;
            } else {
                if (minTx <= wx && wx <= maxTx) {
                    return 2;
                }
                return 0;
            }
        }

        boolean bottom = minTy <= maxWy && maxWy <= maxTy;
        if (bottom) {
            if (tr && tl) {
                return 8;
            } else if (tr) {
                return 7;
            } else if (tl) {
                return 9;
            } else {
                if (minTx <= wx && wx <= maxTx) {
                    return 8;
                }
                return 0;
            }
        }

        boolean left = minTx <= minWx && minWx <= maxTx;
        if (left) {
            if (minTy <= wy && wy <= maxTy) {
                return 4;
            }
            return 0;
        }

        boolean right = minTx <= maxWx && maxWx <= maxTx;
        if (right) {
            if (minTy <= wy && wy <= maxTy) {
                return 6;
            }
            return 0;
        }

        return 0;
    }

    private static boolean circleContains(float x, float y, float r, float left, float top) {
        float xl = x - left;
        float yl = y - top;
        return xl * xl + yl * yl <= r * r;
    }

}

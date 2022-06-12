package zjy.android.bliveinteract.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import zjy.android.bliveinteract.model.Territory;
import zjy.android.bliveinteract.model.Warrior;

public class WarGameView extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    private boolean isDraw;
    private SurfaceHolder holder;

    private Paint mapPaint, shuMapPaint, weiMapPaint, wuMapPaint, qunMapPaint;
    private Paint shuWarriorPaint, weiWarriorPaint, wuWarriorPaint, qunWarriorPaint;
    private List<Territory> mapTerrs;
    private Territory shuCapital, weiCapital, wuCapital, qunCapital;
    private List<Warrior> weiWarriors, shuWarriors, wuWarriors, qunWarriors;
    private Path warriorPath, terrPath;

    public WarGameView(Context context) {
        super(context);
        init();
    }

    public WarGameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WarGameView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        holder = getHolder();
        holder.addCallback(this);

        warriorPath = new Path();
        terrPath = new Path();

        mapTerrs = new ArrayList<>();
        weiCapital = new Territory();
        shuCapital = new Territory();
        wuCapital = new Territory();
        qunCapital = new Territory();

        weiWarriors = new ArrayList<>();
        shuWarriors = new ArrayList<>();
        wuWarriors = new ArrayList<>();
        qunWarriors = new ArrayList<>();

        mapPaint = new Paint();
        mapPaint.setColor(Color.WHITE);

        weiMapPaint = new Paint();
        weiMapPaint.setColor(Color.argb(100, 0, 0, 255));

        shuMapPaint = new Paint();
        shuMapPaint.setColor(Color.argb(100, 255, 0, 0));

        wuMapPaint = new Paint();
        wuMapPaint.setColor(Color.argb(100, 0, 255, 0));

        qunMapPaint = new Paint();
        qunMapPaint.setColor(Color.argb(100, 100, 100, 100));

        shuWarriorPaint = new Paint();
        shuWarriorPaint.setColor(Color.YELLOW);
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
        initTerritory();
        initWarrior();
        isDraw = true;
        new Thread(this).start();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {
        isDraw = false;
    }

    private void drawUI() {
        Canvas canvas = holder.lockCanvas();
        try {
//            canvas.drawColor(Color.WHITE);
            drawMap(canvas);
            drawShu(canvas);
            drawWei(canvas);
            drawWu(canvas);
            drawQun(canvas);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            holder.unlockCanvasAndPost(canvas);
        }
    }

    private void drawQun(Canvas canvas) {
        for (Warrior warrior : qunWarriors) {
            canvas.drawCircle(warrior.currPoint.x, warrior.currPoint.y, warrior.radius,
                    qunWarriorPaint);
        }
    }

    private void drawWu(Canvas canvas) {
        for (Warrior warrior : wuWarriors) {
            canvas.drawCircle(warrior.currPoint.x, warrior.currPoint.y, warrior.radius,
                    wuWarriorPaint);
        }
    }

    private void drawWei(Canvas canvas) {
        for (Warrior warrior : weiWarriors) {
            canvas.drawCircle(warrior.currPoint.x, warrior.currPoint.y, warrior.radius,
                    weiWarriorPaint);
        }
    }

    private void drawShu(Canvas canvas) {
        for (Warrior warrior : shuWarriors) {
            canvas.drawCircle(warrior.currPoint.x, warrior.currPoint.y, warrior.radius,
                    shuWarriorPaint);
        }
    }

    private void drawMap(Canvas canvas) {
        for (Territory info : mapTerrs) {
            if (info.nation == 0) {
                canvas.drawRect(info.rectF, mapPaint);
            } else if (info.nation == 1) {
                canvas.drawRect(info.rectF, weiMapPaint);
            } else if (info.nation == 2) {
                canvas.drawRect(info.rectF, shuMapPaint);
            } else if (info.nation == 3) {
                canvas.drawRect(info.rectF, wuMapPaint);
            } else if (info.nation == 4) {
                canvas.drawRect(info.rectF, qunMapPaint);
            }
        }
    }

    private void initTerritory() {
        int territoryWidth = 50;
        float territoryHeight = 35;
        float size = getHeight() / territoryHeight;
        for (int i = 0; i < territoryWidth; i++) {
            float left = i * size;
            float right = left + size;
            for (int j = 0; j < territoryHeight; j++) {
                float top = j * size;
                float bottom = top + size;
                RectF rectF = new RectF(left, top, right, bottom);
                Territory info = new Territory();
                info.rectF = rectF;
                if (i == 12) {
                    if (j == 7) {
                        weiCapital = info;
                        info.nation = 1;
                    } else if (j == 25) {
                        shuCapital = info;
                        info.nation = 2;
                    }
                } else if (i == 37) {
                    if (j == 7) {
                        qunCapital = info;
                        info.nation = 4;
                    } else if (j == 25) {
                        wuCapital = info;
                        info.nation = 3;
                    }
                }
                mapTerrs.add(info);
            }
        }
    }

    private void initWarrior() {
        float size = getHeight() / 70f;

        Warrior weiWarrior = new Warrior();
        weiWarrior.speed = 1;
        weiWarrior.nation = 1;
        weiWarrior.radius = size;
        weiWarrior.angle = 60;
        weiWarrior.currPoint = new PointF(weiCapital.rectF.centerX(), weiCapital.rectF.centerY());
        weiWarriors.add(weiWarrior);

        Warrior shuWarrior = new Warrior();
        shuWarrior.speed = 1;
        shuWarrior.nation = 2;
        shuWarrior.radius = size;
        shuWarrior.angle = 45;
        shuWarrior.currPoint = new PointF(shuCapital.rectF.centerX(), shuCapital.rectF.centerY());
        shuWarriors.add(shuWarrior);

        Warrior wuWarrior = new Warrior();
        wuWarrior.speed = 1;
        wuWarrior.nation = 3;
        wuWarrior.radius = size;
        wuWarrior.angle = 70;
        wuWarrior.currPoint = new PointF(wuCapital.rectF.centerX(), wuCapital.rectF.centerY());
        wuWarriors.add(wuWarrior);

        Warrior qunWarrior = new Warrior();
        qunWarrior.speed = 1;
        qunWarrior.nation = 4;
        qunWarrior.radius = size;
        qunWarrior.angle = 80;
        qunWarrior.currPoint = new PointF(qunCapital.rectF.centerX(), qunCapital.rectF.centerY());
        qunWarriors.add(qunWarrior);
    }

//    private void attack() {
//        advance(weiWarriors);
//        advance(shuWarriors);
//        advance(wuWarriors);
//        advance(qunWarriors);
//        Path terrPath = new Path();
//        Path warriorPath = new Path();
//        for (Territory terr : mapTerrs) {
//            terrPath.addRect(terr.rectF, Path.Direction.CW);
//            for (Warrior w : weiWarriors) {
//                warriorPath.addCircle(w.currPoint.x, w.currPoint.y, w.radius, Path.Direction.CW);
//                if (warriorPath.op(terrPath, Path.Op.XOR)) {
//                    terr.nation = w.nation;
//
////                    float cx = w.currPoint.x;
////                    float cy = w.currPoint.y;
////                    float px = terr.rectF.centerX();
////                    float py = terr.rectF.centerY();
////                    float k = (cy - py) / (cx - px);
////                    float b = cy - cx * k;
////                    float r = w.radius;
////                    float A = k * k + 1;
////                    float B = -2 * (cx - b * k + k * cy);
////                    float C = -(r * r) + (cx * cx) + (cy * cy) + (b * b) - (2 * b * cy);
////                    double sqrt = Math.sqrt(B * B - 4 * A * C);
////                    double x1 = (sqrt - B) / (2 * A);
////                    double x2 = (sqrt + B) / (2 * A);
////                    double y;
////                    if (px - x1 < px - x2) {
////                        y = k * x1 + b;
////                    } else {
////                        y = k * x2 + b;
////                    }
//                    if (w.angle < 90) {
//                        w.angle = 90 - w.angle + 90;
//                    } else if (w.angle < 180) {
//
//                    } else if (w.angle < 270) {
//
//                    } else {
//
//                    }
//                }
//            }
//        }
//    }

    private void attack() {
        for (Territory terr : mapTerrs) {
//            advance(weiWarriors, terr);
            advance(shuWarriors, terr);
//            advance(wuWarriors, terr);
//            advance(qunWarriors, terr);
        }
    }

    private void advance(List<Warrior> warriors, Territory territory) {
        terrPath.reset();
        terrPath.addRect(territory.rectF, Path.Direction.CW);
        for (Warrior warrior : warriors) {
            double xSpeed;
            double ySpeed;
            double k = Math.tan(Math.toRadians(warrior.angle));
            double b = 0;//warrior.currPoint.y - k * warrior.currPoint.x;
            double l = warrior.speed;
            double A = 1 + k * k;
            double B = 2 * k;
            double C = b * b - l * l;
            double sqrt = Math.sqrt(B * B - 4 * A * C);
            double sx = (-B + sqrt) / (2 * A);
            double xAbs = Math.abs(sx/* - warrior.currPoint.x*/);
            if (warrior.angle < 90 || warrior.angle > 270) {
                xSpeed = -xAbs;
            } else {
                xSpeed = xAbs;
            }
            double yAbs = Math.abs(k * sx + b);
            if (warrior.angle < 180) {
                ySpeed = -yAbs;
            } else {
                ySpeed = yAbs;
            }
            float x = warrior.currPoint.x, y = warrior.currPoint.y;
            float radius = warrior.radius;
            warriorPath.reset();
            for (int i = 0; i < warrior.speed; i++) {
                x += xSpeed;
                y += ySpeed;
//                warriorPath.addCircle(x, y, radius, Path.Direction.CW);
//                if (terrPath.op(warriorPath, Path.Op.XOR)) {
//                    if (territory.rectF.contains(x + radius, y)) {
//                        warrior.angle
//                    } else if (territory.rectF.contains(x - radius, y)) {
//
//                    } else if (territory.rectF.contains(x, y - radius)) {
//
//                    } else if (territory.rectF.contains(x, y + radius)) {
//
//                    } else {
//
//                    }
//                    break;
//                }
            }
            warrior.currPoint.x = x;
            warrior.currPoint.y = y;
        }
    }

    private void advance(List<Warrior> warriors) {
        for (Warrior warrior : warriors) {
            float xSpeed = warrior.speed;
            float ySpeed = warrior.speed;
            if (warrior.angle < 180) {
                ySpeed *= -1;
            }
            if (warrior.angle < 90 || warrior.angle > 270) {
                xSpeed *= -1;
            }
            warrior.prevPoint.x = warrior.currPoint.x;
            warrior.prevPoint.y = warrior.currPoint.y;
            warrior.currPoint.x += xSpeed;
            warrior.currPoint.y += ySpeed;
        }
    }

    @Override
    public void run() {
        while (isDraw) {
            try {
                attack();
                drawUI();
                Thread.sleep(10);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

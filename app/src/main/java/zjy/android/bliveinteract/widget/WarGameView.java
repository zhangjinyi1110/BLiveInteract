package zjy.android.bliveinteract.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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

    private final int lineCount = 50;
    private final int rowCount = 35;

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
        shuWarriorPaint.setColor(Color.RED);

        weiWarriorPaint = new Paint();
        weiWarriorPaint.setColor(Color.BLUE);

        wuWarriorPaint = new Paint();
        wuWarriorPaint.setColor(Color.GREEN);

        qunWarriorPaint = new Paint();
        qunWarriorPaint.setColor(Color.GRAY);
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
            canvas.drawColor(Color.WHITE);
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
            canvas.drawCircle(warrior.getX(), warrior.getY(), warrior.getRadius(), qunWarriorPaint);
        }
    }

    private void drawWu(Canvas canvas) {
        for (Warrior warrior : wuWarriors) {
            canvas.drawCircle(warrior.getX(), warrior.getY(), warrior.getRadius(), wuWarriorPaint);
        }
    }

    private void drawWei(Canvas canvas) {
        for (Warrior warrior : weiWarriors) {
            canvas.drawCircle(warrior.getX(), warrior.getY(), warrior.getRadius(), weiWarriorPaint);
        }
    }

    private void drawShu(Canvas canvas) {
        for (Warrior warrior : shuWarriors) {
            canvas.drawCircle(warrior.getX(), warrior.getY(), warrior.getRadius(), shuWarriorPaint);
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
        float size = getHeight() * 1f / rowCount;
        int l = lineCount / 4;
        int r = rowCount  / 4;
        int ll = l * 3;
        int rl = r * 3;
        for (int i = 0; i < lineCount; i++) {
            float left = i * size;
            float right = left + size;
            for (int j = 0; j < rowCount; j++) {
                float top = j * size;
                float bottom = top + size;
                RectF rectF = new RectF(left, top, right, bottom);
                Territory info = new Territory();
                info.rectF = rectF;
                if (i == l) {
                    if (j == r) {
                        weiCapital = info;
                        info.nation = 1;
                    } else if (j == rl) {
                        shuCapital = info;
                        info.nation = 2;
                    }
                } else if (i == ll) {
                    if (j == r) {
                        qunCapital = info;
                        info.nation = 4;
                    } else if (j == rl) {
                        wuCapital = info;
                        info.nation = 3;
                    }
                }
                mapTerrs.add(info);
            }
        }
    }

    private void initWarrior() {
        float size = getHeight() / (rowCount * 2f);

        Warrior weiWarrior = new Warrior(1, 60, size, 1, new PointF(weiCapital.rectF.centerX(), weiCapital.rectF.centerY()));
        weiWarriors.add(weiWarrior);

        Warrior shuWarrior = new Warrior(1, 50, size, 2, new PointF(shuCapital.rectF.centerX(), shuCapital.rectF.centerY()));
        shuWarriors.add(shuWarrior);

        Warrior wuWarrior = new Warrior(1, 70, size, 3, new PointF(wuCapital.rectF.centerX(), wuCapital.rectF.centerY()));
        wuWarriors.add(wuWarrior);

        Warrior qunWarrior = new Warrior(1, 80, size, 4, new PointF(qunCapital.rectF.centerX(), qunCapital.rectF.centerY()));
        qunWarriors.add(qunWarrior);
    }

    private void calculate() {
        advance(weiWarriors);
        advance(shuWarriors);
        advance(wuWarriors);
        advance(qunWarriors);

        attack(weiWarriors);
        attack(shuWarriors);
        attack(wuWarriors);
        attack(qunWarriors);
    }

    private void attack(List<Warrior> warriors) {
        for (Territory terr : mapTerrs) {
            for (Warrior warrior : warriors) {
                if (warrior.getNation() == terr.nation) break;
                if (warrior.isAttacked()) continue;
                if (checkSuccess(terr, warrior)) {
                    terr.nation = warrior.getNation();
                } else {
                    checkBorder(warrior);
                }
            }
        }
    }

    private void checkBorder(Warrior warrior) {
        float left = warrior.getX() - warrior.getRadius();
        float right = warrior.getX() + warrior.getRadius();
        float top = warrior.getY() - warrior.getRadius();
        float bottom = warrior.getY() + warrior.getRadius();
        if (top <= 0) {
            warrior.setCurrPoint(warrior.getX(), warrior.getRadius());
            warrior.updateAngle(1);
        } else if (bottom >= getHeight()) {
            warrior.setCurrPoint(warrior.getX(), getHeight() - warrior.getRadius());
            warrior.updateAngle(3);
        } else if (left <= 0) {
            warrior.setCurrPoint(warrior.getRadius(), warrior.getY());
            warrior.updateAngle(0);
        } else if (right >= getWidth()){
            warrior.setCurrPoint(getWidth() - warrior.getRadius(), warrior.getY());
            warrior.updateAngle(2);
        }
    }

    private boolean checkSuccess(Territory terr, Warrior warrior) {
        float tx = terr.rectF.centerX();
        float ty = terr.rectF.centerY();
        float wx = warrior.getX();
        float wy = warrior.getY();
        float k = (wy - ty) / (wx - tx);
        float b = ty - k * tx;
        float r = warrior.getRadius() * warrior.getRadius();
        float tSize = terr.rectF.height() / 2;

        float topY = ty - tSize;
        float topX = (topY - b) / k;
        if (topX >= tx - tSize && topX <= tx + tSize) {
            float topL = (wx - topX) * (wx - topX) + (wy - topY) * (wy - topY);
            if (topL <= r) {
                float leftX = tx - tSize;
                float leftY = leftX * k + b;
                if (leftY >= ty - tSize && leftY <= ty + tSize) {
                    float leftL = (wx - leftX) * (wx - leftX) + (wy - leftY) * (wy - leftY);
                    if (leftL <= r) {
                        warrior.updateAngle(-1);
                        return true;
                    }
                }

                float rightX = tx + tSize;
                float rightY = rightX * k + b;
                if (rightY >= ty - tSize && rightY <= ty + tSize) {
                    float rightL = (wx - rightX) * (wx - rightX) + (wy - rightY) * (wy - rightY);
                    if (rightL <= r) {
                        warrior.updateAngle(-1);
                        return true;
                    }
                }

                warrior.updateAngle(1);
                return true;
            }
        }

        float bottomY = ty + tSize;
        float bottomX = (bottomY - b) / k;
        if (bottomX >= tx - tSize && bottomX <= tx + tSize) {
            float bottomL = (wx - bottomX) * (wx - bottomX) + (wy - bottomY) * (wy - bottomY);
            if (bottomL <= r) {
                float leftX = tx - tSize;
                float leftY = leftX * k + b;
                if (leftY >= ty - tSize && leftY <= ty + tSize) {
                    float leftL = (wx - leftX) * (wx - leftX) + (wy - leftY) * (wy - leftY);
                    if (leftL <= r) {
                        warrior.updateAngle(-1);
                        return true;
                    }
                }

                float rightX = tx + tSize;
                float rightY = rightX * k + b;
                if (rightY >= ty - tSize && rightY <= ty + tSize) {
                    float rightL = (wx - rightX) * (wx - rightX) + (wy - rightY) * (wy - rightY);
                    if (rightL <= r) {
                        warrior.updateAngle(-1);
                        return true;
                    }
                }

                warrior.updateAngle(3);
                return true;
            }
        }

        float leftX = tx - tSize;
        float leftY = leftX * k + b;
        if (leftY >= ty - tSize && leftY <= ty + tSize) {
            float leftL = (wx - leftX) * (wx - leftX) + (wy - leftY) * (wy - leftY);
            if (leftL <= r) {
                warrior.updateAngle(0);
                return true;
            }
        }

        float rightX = tx + tSize;
        float rightY = rightX * k + b;
        if (rightY >= ty - tSize && rightY <= ty + tSize) {
            float rightL = (wx - rightX) * (wx - rightX) + (wy - rightY) * (wy - rightY);
            if (rightL <= r) {
                warrior.updateAngle(2);
                return true;
            }
        }

        return false;
    }

    private void advance(List<Warrior> warriors) {
        for (Warrior warrior : warriors) {
            double xSpeed = warrior.getXSpeed();
            double ySpeed = warrior.getYSpeed();
            double wx = warrior.getX() + xSpeed;
            double wy = warrior.getY() + ySpeed;
            warrior.setCurrPoint((float) wx, (float) wy);
        }
    }

    @Override
    public void run() {
        while (isDraw) {
            try {
                calculate();
                drawUI();
                Thread.sleep(10);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = MeasureSpec.getSize(heightMeasureSpec);
        float size = height * 1f / rowCount;
        setMeasuredDimension((int) (size * lineCount), (int) (size * rowCount));
    }

    public void addWarrior(int nation) {
        float size = getHeight() / (rowCount * 2f);
        float x, y, angle;
        switch (nation) {
            case 1:
                x = weiCapital.rectF.centerX();
                y = weiCapital.rectF.centerY();
                break;
            case 2:
                x = shuCapital.rectF.centerX();
                y = shuCapital.rectF.centerY();
                break;
            case 3:
                x = wuCapital.rectF.centerX();
                y = wuCapital.rectF.centerY();
                break;
            default:
                x = qunCapital.rectF.centerX();
                y = qunCapital.rectF.centerY();
                break;
        }
        Random random = new Random();
        do {
            angle = random.nextInt(360);
        } while (angle % 90 == 0);
        Warrior warrior = new Warrior(1, angle, size, nation, new PointF(x, y));
        switch (nation) {
            case 1:
                weiWarriors.add(warrior);
                break;
            case 2:
                shuWarriors.add(warrior);
                break;
            case 3:
                wuWarriors.add(warrior);
                break;
            default:
                qunWarriors.add(warrior);
                break;
        }
    }

    public void addSpeed(int nation) {
        Random random = new Random();
        float speed = random.nextFloat();
        switch (nation) {
            case 1:
                for (Warrior w : weiWarriors) {
                    w.setSpeed(w.getSpeed() + speed);
                }
                break;
            case 2:
                for (Warrior w : shuWarriors) {
                    w.setSpeed(w.getSpeed() + speed);
                }
                break;
            case 3:
                for (Warrior w : wuWarriors) {
                    w.setSpeed(w.getSpeed() + speed);
                }
                break;
            default:
                for (Warrior w : qunWarriors) {
                    w.setSpeed(w.getSpeed() + speed);
                }
                break;
        }
    }
}

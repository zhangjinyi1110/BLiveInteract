package zjy.android.bliveinteract.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import zjy.android.bliveinteract.R;
import zjy.android.bliveinteract.model.Territory;
import zjy.android.bliveinteract.model.UserDanMu;
import zjy.android.bliveinteract.model.Warrior;

public class GameView extends SurfaceView implements Runnable, SurfaceHolder.Callback {

    private static final String TAG = "GameViewTag";

    private static final int lineCount = 42;
    private static final int rowCount = 42;

    public static final String[] groupNames = new String[]{"红", "黄", "蓝", "绿"};
    public static final int[] groupImageIds = new int[]{R.drawable.icon_hongfa,
            R.drawable.icon_lufei, R.drawable.icon_baji, R.drawable.icon_heihuzi};

    private boolean isDrawing = false;
    private SurfaceHolder holder;

    private Paint mapPaint, rolePaint, groupNamePaint, hpPaint;
    private final List<Warrior> warriors = new ArrayList<>();
    private final Territory[][] territories = new Territory[lineCount][rowCount];
    private final Territory[] capitals = new Territory[groupNames.length];
    private float terrSize;
    private final Bitmap[] groupImages = new Bitmap[groupImageIds.length];
    private final Rect groupImageSrc = new Rect();
    private final RectF groupImageDst = new RectF();
    private float groupNameHalfWidth, groupNameHalfHeight;
    private float hpHalfWidth, hpHalfHeight;
    private final int[][] capitalPoint = new int[][]{{12, 5}, {30, 5}, {12, 37}, {30, 37}, {21,
            21}, {4, 21}, {38, 21}};

    private final long UPDATE_TIME = 30;

    public GameView(Context context) {
        super(context);
        init();
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GameView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        holder = getHolder();
        holder.addCallback(this);

        mapPaint = new Paint();

        rolePaint = new Paint();

        groupNamePaint = new Paint();
        groupNamePaint.setColor(Color.parseColor("#64999999"));
        groupNamePaint.setTextSize(120);
        groupNameHalfWidth = groupNamePaint.measureText(groupNames[0]) / 2;
        Paint.FontMetrics fontMetrics = groupNamePaint.getFontMetrics();
        groupNameHalfHeight = (fontMetrics.bottom - fontMetrics.ascent) / 2 - fontMetrics.bottom;

        hpPaint = new Paint();
        hpPaint.setColor(Color.WHITE);
        hpPaint.setTextSize(40);
        hpHalfWidth = hpPaint.measureText("0") / 2;
        Paint.FontMetrics metrics = hpPaint.getFontMetrics();
        hpHalfHeight = (metrics.bottom - metrics.ascent) / 2 - metrics.bottom;

        for (int i = 0; i < groupImageIds.length; i++) {
            groupImages[i] = BitmapFactory.decodeResource(getResources(), groupImageIds[i]);
        }
    }

    private void initTerritory() {
        float size = getHeight() * 1f / rowCount;
        for (int i = 0; i < lineCount; i++) {
            float left = i * size;
            float right = left + size;
            for (int j = 0; j < rowCount; j++) {
                float top = j * size;
                float bottom = top + size;
                RectF rectF = new RectF(left, top, right, bottom);
                Territory info = new Territory();
                info.rectF = rectF;
                info.nation = -1;
                territories[i][j] = info;
            }
        }

        for (int i = 0; i < groupNames.length; i++) {
            int[] point = capitalPoint[i];
            Territory weiCapital = territories[point[0]][point[1]];
            weiCapital.nation = i;
            weiCapital.isCapital = true;
            weiCapital.hp = 5;
            territories[point[0] + 1][point[1] + 1].nation = i;
            territories[point[0] + 1][point[1] - 1].nation = i;
            territories[point[0] + 1][point[1]].nation = i;
            territories[point[0] - 1][point[1] + 1].nation = i;
            territories[point[0] - 1][point[1] - 1].nation = i;
            territories[point[0] - 1][point[1]].nation = i;
            territories[point[0]][point[1] + 1].nation = i;
            territories[point[0]][point[1] - 1].nation = i;
            capitals[i] = weiCapital;
        }
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
        isDrawing = true;
        initTerritory();
        new Thread(this).start();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {
        isDrawing = false;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = MeasureSpec.getSize(heightMeasureSpec);
        terrSize = height * 1f / rowCount;
        setMeasuredDimension((int) (terrSize * lineCount + 0.5f),
                (int) (terrSize * rowCount + 0.5f));
    }

    @Override
    public void run() {
        while (isDrawing) {
            Canvas canvas = holder.lockCanvas();
            try {
                long start = System.currentTimeMillis();
                calculate();
                long c = System.currentTimeMillis() - start;
                drawMap(canvas);
                long dm = System.currentTimeMillis() - start - c;
                drawRoles(canvas);
                long dr = System.currentTimeMillis() - start - c - dm;
                long now = System.currentTimeMillis();
                long time = now - start;
                Log.e(TAG, "run: time = " + time + "/c = " + c + "/dm = " + dm + "/dr = " + dr);
                if (time < UPDATE_TIME) {
                    Thread.sleep(UPDATE_TIME - time);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "run: " + e);
            } finally {
                holder.unlockCanvasAndPost(canvas);
            }
        }
    }

    private void calculate() {
        for (Warrior w : warriors) {
            advance(w);
        }

        attack(warriors);
    }

    private void attack(List<Warrior> warriors) {
        for (Territory[] lineTerr : territories) {
            for (Territory terr : lineTerr) {
                terr.attacked = false;
                for (Warrior warrior : warriors) {
                    if (warrior.getNation() == terr.nation) continue;
//                    if (warrior.isAttacked()) continue;
                    if (checkSuccess(terr, warrior, true)) {
                        if (checkWarrior(terr, warrior.getNation())) {
                            advance(warrior);
                            continue;
                        }
//                        if (terr.attacked) continue;
                        if (terr.isCapital) {
                            if (--terr.hp == 0) {
                                terr.isCapital = false;
                                int oldNation = terr.nation;
                                int newNation = warrior.getNation();
                                terr.nation = newNation;
                                for (Warrior w : warriors) {
                                    if (w.getNation() == oldNation) {
                                        w.setNation(newNation);
                                    }
                                }
                            }
                        } else {
                            terr.nation = warrior.getNation();
                            warrior.capture();
                        }
                    } else {
                        checkBorder(warrior);
                    }
                }
            }
        }
    }

    private boolean checkWarrior(Territory terr, int nation) {
        for (Warrior entry : warriors) {
            if (entry.getNation() == nation) continue;
            if (checkSuccess(terr, entry, false)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检测是否到达边界
     *
     * @param warrior 战士
     */
    private void checkBorder(Warrior warrior) {
        float left = warrior.getX() - warrior.getRadius();
        float right = warrior.getX() + warrior.getRadius();
        float top = warrior.getY() - warrior.getRadius();
        float bottom = warrior.getY() + warrior.getRadius();
        if (top <= 0) {
            warrior.setCurrPoint(warrior.getX(), warrior.getRadius() + 1);
            warrior.updateAngle(1);
        } else if (bottom >= getHeight()) {
            warrior.setCurrPoint(warrior.getX(), getHeight() - warrior.getRadius() - 1);
            warrior.updateAngle(3);
        } else if (left <= 0) {
            warrior.setCurrPoint(warrior.getRadius() + 1, warrior.getY());
            warrior.updateAngle(0);
        } else if (right >= getWidth()) {
            warrior.setCurrPoint(getWidth() - warrior.getRadius() - 1, warrior.getY());
            warrior.updateAngle(2);
        }
    }


    /**
     * 检测是否碰撞
     *
     * @param terr    领土块
     * @param warrior 战士
     * @return true or false
     */
    private boolean checkSuccess(Territory terr, Warrior warrior, boolean update) {
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
                if (update) {
                    float wby = wy + warrior.getRadius();
                    if (wx <= tx + tSize && wx >= tx - tSize && wby <= ty + tSize && wby >= ty - tSize) {
//                        Log.e("TAG", "checkSuccess: in square bottom");
                        warrior.updateAngle(3);
                    } else {
//                        Log.e("TAG", "checkSuccess: not in square bottom");
                        if (tx > wx) {
                            warrior.updateAngle(0, 90);
                        } else {
                            warrior.updateAngle(90, 180);
                        }
                    }
                }
                return true;
            }
        }

        float bottomY = ty + tSize;
        float bottomX = (bottomY - b) / k;
        if (bottomX >= tx - tSize && bottomX <= tx + tSize) {
            float bottomL = (wx - bottomX) * (wx - bottomX) + (wy - bottomY) * (wy - bottomY);
            if (bottomL <= r) {
                if (update) {
                    float wby = wy - warrior.getRadius();
                    if (wx <= tx + tSize && wx >= tx - tSize && wby <= ty + tSize && wby >= ty - tSize) {
//                        Log.e("TAG", "checkSuccess: in square top");
                        warrior.updateAngle(1);
                    } else {
//                        Log.e("TAG", "checkSuccess: not in square top");
                        if (tx > wx) {
                            warrior.updateAngle(270, 360);
                        } else {
                            warrior.updateAngle(180, 270);
                        }
                    }
                }
                return true;
            }
        }

        float leftX = tx - tSize;
        float leftY = leftX * k + b;
        if (leftY >= ty - tSize && leftY <= ty + tSize) {
            float leftL = (wx - leftX) * (wx - leftX) + (wy - leftY) * (wy - leftY);
            if (leftL <= r) {
                if (update) {
                    float wbx = wx + warrior.getRadius();
                    if (wbx <= tx + tSize && wbx >= tx - tSize && wy <= ty + tSize && wy >= ty - tSize) {
//                        Log.e("TAG", "checkSuccess: in square right");
                        warrior.updateAngle(2);
                    } else {
//                        Log.e("TAG", "checkSuccess: not in square right");
                        if (ty > wy) {
                            warrior.updateAngle(0, 90);
                        } else {
                            warrior.updateAngle(270, 360);
                        }
                    }
                }
                return true;
            }
        }

        float rightX = tx + tSize;
        float rightY = rightX * k + b;
        if (rightY >= ty - tSize && rightY <= ty + tSize) {
            float rightL = (wx - rightX) * (wx - rightX) + (wy - rightY) * (wy - rightY);
            if (rightL <= r) {
                if (update) {
                    float wbx = wx + warrior.getRadius();
                    if (wbx <= tx + tSize && wbx >= tx - tSize && wy <= ty + tSize && wy >= ty - tSize) {
//                        Log.e("TAG", "checkSuccess: in square left");
                        warrior.updateAngle(0);
                    } else {
//                        Log.e("TAG", "checkSuccess: not in square left");
                        if (ty > wy) {
                            warrior.updateAngle(90, 180);
                        } else {
                            warrior.updateAngle(180, 270);
                        }
                    }
                }
                return true;
            }
        }

        return false;
    }

    private void advance(Warrior warrior) {
        double xSpeed = warrior.getXSpeed();
        double ySpeed = warrior.getYSpeed();
        double wx = warrior.getX() + xSpeed;
        double wy = warrior.getY() + ySpeed;
        warrior.setCurrPoint((float) wx, (float) wy);
    }

    private void drawMap(Canvas canvas) {
        canvas.drawColor(Color.WHITE);
        for (Territory[] array : territories) {
            for (Territory territory : array) {
                if (territory.nation == -1) continue;
                groupImageSrc.set((int) territory.rectF.left, (int) territory.rectF.top,
                        (int) territory.rectF.right, (int) territory.rectF.bottom);
                groupImageDst.set(territory.rectF.left, territory.rectF.top,
                        territory.rectF.right, territory.rectF.bottom);
                canvas.drawBitmap(groupImages[territory.nation], groupImageSrc, groupImageDst
                        , mapPaint);
            }
        }
        for (Territory capital : capitals) {
            if (capital.isCapital) {
                float x = capital.rectF.centerX(), y = capital.rectF.centerY();
                canvas.drawText(groupNames[capital.nation], x - groupNameHalfWidth,
                        y + groupNameHalfHeight, groupNamePaint);

                canvas.drawText(String.valueOf(capital.hp), x - hpHalfWidth, y + hpHalfHeight,
                        hpPaint);
            }
        }
    }

    private void drawRoles(Canvas canvas) throws ExecutionException, InterruptedException {
        for (Warrior warrior : warriors) {
            int size = (int) (warrior.getRadius() * 2);
            float l = warrior.getX() - warrior.getRadius();
            float t = warrior.getY() - warrior.getRadius();
            Bitmap bitmap = Glide.with(this)
                    .asBitmap()
                    .load(warrior.getUserDanMu().img)
                    .apply(RequestOptions.circleCropTransform())
                    .submit(size, size)
                    .get();
            canvas.drawBitmap(bitmap, l, t, rolePaint);
        }
    }

    public void addWarrior(int nation, UserDanMu userDanMu) {
        Territory territory = capitals[nation];
        if (territory != null && territory.isCapital) {
//            List<Warrior> list = warriorMap.get(nation);
//            if (list == null) {
//                list = new ArrayList<>();
//                warriorMap.put(nation, list);
//            }
            for (Warrior w : warriors) {
                if (w.getUserDanMu().userid == userDanMu.userid) {
                    return;
                }
            }
            warriors.add(new Warrior(territory, userDanMu));
        }
    }

    public void changeNation(int nation, UserDanMu userDanMu) {
        List<Warrior> list = new ArrayList<>();
        for (Warrior w : warriors) {
            if (w.getUserDanMu().userid == userDanMu.userid) {
                list.add(w);
            }
        }
        if (list.isEmpty()) {
            addWarrior(nation, userDanMu);
            return;
        }
        Territory capital = capitals[nation];
        for (Warrior w : list) {
            w.setCurrPoint(capital.rectF.centerX(), capital.rectF.centerY());
            w.setNation(nation);
        }
    }

    public void goCapital(UserDanMu userDanMu) {
        for (Warrior entry : warriors) {
            if (entry.getUserDanMu().userid == userDanMu.userid) {
                Territory capital = capitals[entry.getNation()];
                float x = capital.rectF.centerX(), y = capital.rectF.centerY();
                entry.setCurrPoint(x, y);
            }
        }
    }

    public void randomBuff(UserDanMu userDanMu) {
        boolean helper = System.currentTimeMillis() % 10 == 0;
        for (Warrior w : warriors) {
            if (w.getUserDanMu().userid == userDanMu.userid) {
                if (helper) {
                    addHelper(w);
                    return;
                } else {
                    addSpeed(w, 0.5f);
                }
            }
        }
    }

    public void addSpeed(UserDanMu userDanMu, float speed) {
        for (Warrior w : warriors) {
            if (w.getUserDanMu().userid == userDanMu.userid) {
                addSpeed(w, speed);
            }
        }
    }

    public void addSpeed(Warrior warrior, float speed) {
        warrior.setSpeed(warrior.getSpeed() + speed);
    }

    public void addHelper(Warrior warrior) {
        Territory capital = capitals[warrior.getNation()];
        Warrior helper = new Warrior(capital, warrior.getUserDanMu());
        helper.setSpeed(warrior.getSpeed());
        warriors.add(helper);
    }

    public void addHelper(UserDanMu userDanMu) {
        for (Warrior w : warriors) {
            if (w.getUserDanMu().userid == userDanMu.userid) {
                addHelper(w);
                return;
            }
        }
    }
}

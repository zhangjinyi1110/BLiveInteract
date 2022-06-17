package zjy.android.bliveinteract.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import zjy.android.bliveinteract.model.GameInfo;
import zjy.android.bliveinteract.model.Territory;
import zjy.android.bliveinteract.model.Warrior;

public class WarGameView extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    private boolean isDraw;
    private SurfaceHolder holder;

    private Territory[][] mapTerrs;
    private Map<Integer, List<Warrior>> warriorMap;
    private Map<Integer, Paint> warriorPatin;
    private Map<Integer, Paint> terrPaint;
    private Map<Integer, Territory> capitalMap;
    private Paint nationNamePaint;

    private final int lineCount = 50;
    private final int rowCount = 35;

    private final String[] warriorsColorStr = new String[]{"#FFFFFF", "#1678FF", "#FF3715", "#FFDA17", "#00CFCF", "#44D75C",
            "#8E20FF", "#804904"};
    private final String[] terrColorStr = new String[]{"#FFFFFF", "#641678FF", "#64FF3715", "#64FFDA17", "#6400CFCF", "#6444D75C",
            "#648E20FF", "#64804904"};
    public static final String[] nationName = new String[]{"", "秦", "楚", "燕", "韩", "赵", "魏", "齐"};

    private final int[][] capitalPoint = new int[][]{{12, 5}, {38, 5}, {12, 30}, {38, 30}, {25, 18}, {6, 18}, {44, 18}};

    private final int nationCount = 7;

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

        // 地图块
        mapTerrs = new Territory[lineCount][rowCount];

        // 战士列表
        warriorMap = new HashMap<>();
        // 首都
        capitalMap = new HashMap<>();

        nationNamePaint = new Paint();
        nationNamePaint.setColor(Color.parseColor("#64999999"));
        nationNamePaint.setTextSize(120);
        nationNamePaint.setFakeBoldText(true);

        // 地图paint
        terrPaint = new HashMap<>();
        int tcSize = terrColorStr.length;
        for (int i = 0; i < tcSize; i++) {
            Paint paint = new Paint();
            paint.setColor(Color.parseColor(terrColorStr[i]));
            paint.setTextSize(40);
            paint.setFakeBoldText(true);
            terrPaint.put(i, paint);
        }

        // 战士paint
        warriorPatin = new HashMap<>();
        int wcSize = warriorsColorStr.length;
        for (int i = 0; i < wcSize; i++) {
            Paint paint = new Paint();
            paint.setColor(Color.parseColor(warriorsColorStr[i]));
            warriorPatin.put(i, paint);
        }
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
        initTerritory();
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
            drawWarrior(canvas);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            holder.unlockCanvasAndPost(canvas);
        }
    }

    private void drawWarrior(Canvas canvas) {
        for (Map.Entry<Integer, List<Warrior>> item : warriorMap.entrySet()) {
            Paint paint = warriorPatin.get(item.getKey());
            for (Warrior warrior : item.getValue()) {
                canvas.drawCircle(warrior.getX(), warrior.getY(), warrior.getRadius(), paint);
            }
        }
    }

    /**
     * 绘制地图
     *
     * @param canvas 画布
     */
    private void drawMap(Canvas canvas) {
        for (Territory[] lineTerr : mapTerrs) {
            for (Territory info : lineTerr) {
                Paint paint = terrPaint.get(info.nation);
                canvas.drawRect(info.rectF, paint);
            }
        }
        for (Territory capital : capitalMap.values()) {
            if (capital.isCapital) {
                String name = nationName[capital.nation];
                float nx = nationNamePaint.measureText(name) / 2;
                Paint.FontMetrics nfm = nationNamePaint.getFontMetrics();
                float ny = (nfm.bottom - nfm.ascent) / 2 - nfm.bottom;
                canvas.drawText(name, capital.rectF.centerX() - nx, capital.rectF.centerY() + ny, nationNamePaint);

                Paint mapPaint = terrPaint.get(0);
                String hp = capital.hp + "";
                assert mapPaint != null;
                float tx = mapPaint.measureText(hp) / 2;
                Paint.FontMetrics fontMetrics = mapPaint.getFontMetrics();
                float ty = (fontMetrics.bottom - fontMetrics.ascent) / 2 - fontMetrics.bottom;
                canvas.drawText(hp, capital.rectF.centerX() - tx, capital.rectF.centerY() + ty, mapPaint);
            }
        }
    }

    /**
     * 初始化领土
     */
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
                mapTerrs[i][j] = info;
            }
        }

        for (int i = 1; i <= nationCount; i++) {
            int[] point = capitalPoint[i - 1];
            Territory weiCapital = mapTerrs[point[0]][point[1]];
            weiCapital.nation = i;
            weiCapital.isCapital = true;
            weiCapital.hp = 5;
            mapTerrs[point[0] + 1][point[1] + 1].nation = i;
            mapTerrs[point[0] + 1][point[1] - 1].nation = i;
            mapTerrs[point[0] + 1][point[1]].nation = i;
            mapTerrs[point[0] - 1][point[1] + 1].nation = i;
            mapTerrs[point[0] - 1][point[1] - 1].nation = i;
            mapTerrs[point[0] - 1][point[1]].nation = i;
            mapTerrs[point[0]][point[1] + 1].nation = i;
            mapTerrs[point[0]][point[1] - 1].nation = i;
            capitalMap.put(i, weiCapital);
        }
    }

    /**
     * 计算
     */
    private void calculate() {
        for (List<Warrior> warriors : warriorMap.values()) {
            advance(warriors);
        }

        for (List<Warrior> warriors : warriorMap.values()) {
            attack(warriors);
        }
    }

    /**
     * 进攻
     *
     * @param warriors 战士列表
     */
    private void attack(List<Warrior> warriors) {
        for (Territory[] lineTerr : mapTerrs) {
            for (Territory terr : lineTerr) {
                terr.attacked = false;
                for (Warrior warrior : warriors) {
                    if (warrior.getNation() == terr.nation) break;
                    if (warrior.isAttacked()) continue;
                    if (checkSuccess(terr, warrior)) {
                        if (terr.attacked) continue;
                        if (terr.isCapital) {
                            if (--terr.hp == 0) {
                                terr.isCapital = false;
                                int oldNation = terr.nation;
                                int newNation = warrior.getNation();
                                terr.nation = newNation;
                                List<Warrior> oldWarriors = warriorMap.remove(oldNation);
                                if (oldWarriors != null) {
                                    for (Warrior w : oldWarriors) {
                                        w.setNation(newNation);
                                    }
                                    List<Warrior> warriorList = warriorMap.get(newNation);
                                    if (warriorList != null) {
                                        warriorList.addAll(oldWarriors);
                                    }
                                }
                            }
                        } else {
                            terr.nation = warrior.getNation();
                        }
                    } else {
                        checkBorder(warrior);
                    }
                }
            }
        }
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
            warrior.setCurrPoint(warrior.getX(), warrior.getRadius());
            warrior.updateAngle(3);
        } else if (bottom >= getHeight()) {
            warrior.setCurrPoint(warrior.getX(), getHeight() - warrior.getRadius());
            warrior.updateAngle(1);
        } else if (left <= 0) {
            warrior.setCurrPoint(warrior.getRadius(), warrior.getY());
            warrior.updateAngle(2);
        } else if (right >= getWidth()) {
            warrior.setCurrPoint(getWidth() - warrior.getRadius(), warrior.getY());
            warrior.updateAngle(0);
        }
    }

    /**
     * 检测是否碰撞
     *
     * @param terr    领土块
     * @param warrior 战士
     * @return true or false
     */
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
                warrior.updateAngle(3);
                return true;
            }
        }

        float bottomY = ty + tSize;
        float bottomX = (bottomY - b) / k;
        if (bottomX >= tx - tSize && bottomX <= tx + tSize) {
            float bottomL = (wx - bottomX) * (wx - bottomX) + (wy - bottomY) * (wy - bottomY);
            if (bottomL <= r) {
                warrior.updateAngle(1);
                return true;
            }
        }

        float leftX = tx - tSize;
        float leftY = leftX * k + b;
        if (leftY >= ty - tSize && leftY <= ty + tSize) {
            float leftL = (wx - leftX) * (wx - leftX) + (wy - leftY) * (wy - leftY);
            if (leftL <= r) {
                warrior.updateAngle(2);
                return true;
            }
        }

        float rightX = tx + tSize;
        float rightY = rightX * k + b;
        if (rightY >= ty - tSize && rightY <= ty + tSize) {
            float rightL = (wx - rightX) * (wx - rightX) + (wy - rightY) * (wy - rightY);
            if (rightL <= r) {
                warrior.updateAngle(0);
                return true;
            }
        }

        return false;
    }

    /**
     * 前进-更新每个战士前进后的坐标
     *
     * @param warriors 战士列表
     */
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
                updateData();
                drawUI();
                Thread.sleep(10);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void updateData() {
        Flowable.just(0)
                .subscribeOn(Schedulers.io())
                .doOnComplete(() -> {
                    if (onUpdateGameInfoListener != null) {
                        List<GameInfo> gameInfos = new ArrayList<>();
                        for (int i = 1; i <= nationCount; i++) {
                            GameInfo info = new GameInfo();
                            info.nation = i;
                            List<Warrior> warriors = warriorMap.get(i);
                            if (warriors == null) {
                                info.warriorNum = 0;
                            } else {
                                info.warriorNum = warriors.size();
                            }
                            info.capitalNum = capitalMap.get(i).hp;
                            int terrNum = 0;
                            for (Territory[] item : mapTerrs) {
                                for (Territory t : item) {
                                    if (i == t.nation) terrNum++;
                                }
                            }
                            info.terrNum = terrNum;
                            gameInfos.add(info);
                        }
                        onUpdateGameInfoListener.onUpdate(gameInfos);
                    }
                })
                .subscribe();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = MeasureSpec.getSize(heightMeasureSpec);
        float size = height * 1f / rowCount;
        setMeasuredDimension((int) (size * lineCount), (int) (size * rowCount));
    }

    public void addWarrior(int nation) {
        Territory territory = capitalMap.get(nation);
        if (territory != null && territory.isCapital) {
            List<Warrior> list = warriorMap.get(nation);
            if (list == null) {
                list = new ArrayList<>();
                warriorMap.put(nation, list);
            }
            list.add(new Warrior(territory));
        }
    }

    public void addSpeed(int nation) {
        List<Warrior> list = warriorMap.get(nation);
        if (list == null) {
            list = new ArrayList<>();
            warriorMap.put(nation, list);
        }
        for (Warrior w : list) {
            w.setSpeed(w.getSpeed() + 1);
        }
    }

    private OnUpdateGameInfoListener onUpdateGameInfoListener;

    public void setOnUpdateGameInfoListener(OnUpdateGameInfoListener onUpdateGameInfoListener) {
        this.onUpdateGameInfoListener = onUpdateGameInfoListener;
    }

    public interface OnUpdateGameInfoListener {
        void onUpdate(List<GameInfo> gameInfos);
    }
}

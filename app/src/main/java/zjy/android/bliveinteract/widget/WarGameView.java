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

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import zjy.android.bliveinteract.R;
import zjy.android.bliveinteract.model.CaptureInfo;
import zjy.android.bliveinteract.model.GameInfo;
import zjy.android.bliveinteract.model.Territory;
import zjy.android.bliveinteract.model.UserDanMu;
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

    private Bitmap[] bitmaps = new Bitmap[5];

    private final Map<String, Bitmap> bitmapMap = new HashMap<>();

    private final int lineCount = 42;
    private final int rowCount = 42;

    private final String[] warriorsColorStr = new String[]{"#FFFFFF", "#1678FF", "#FF3715",
            "#FFDA17", "#00CFCF", "#44D75C",
            "#8E20FF", "#804904"};
    public static final String[] terrColorStr = new String[]{"#FFFFFF", "#641678FF", "#64FF3715",
            "#64FFDA17", "#6400CFCF", "#6444D75C",
            "#648E20FF", "#64804904"};
    //    public static final String[] nationName = new String[]{"", "秦", "楚", "燕", "韩", "赵",
    //    "魏", "齐"};
    public static final String[] nationName = new String[]{"", "蓝", "绿", "红", "黄"};

    private final int[][] capitalPoint = new int[][]{{12, 5}, {30, 5}, {12, 37}, {30, 37}, {21,
            21}, {4, 21}, {38, 21}};

    private final int nationCount = nationName.length - 1;

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

        int[] a = new int[]{0, R.drawable.icon_baji, R.drawable.icon_heihuzi,
                R.drawable.icon_hongfa, R.drawable.icon_lufei};
        for (int i = 1; i < 5; i++) {
            bitmaps[i] = BitmapFactory.decodeResource(getResources(), a[i]);
        }
    }

    public void reset() {
        isDraw = false;
        init();
        initTerritory();
        isDraw = true;
        new Thread(this).start();
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
                int size = (int) (warrior.getRadius() * 2);
                float l = warrior.getX() - warrior.getRadius();
                float t = warrior.getY() - warrior.getRadius();
                canvas.drawBitmap(getImg(warrior.getUserDanMu().img, size), l, t, paint);
            }
        }
    }

    private Bitmap getImg(String path, int size) {
        Bitmap bitmap = bitmapMap.get(path);
        if (bitmap != null) return bitmap;
        try {
            bitmap = Glide.with(this)
                    .asBitmap()
                    .load(path)
                    .apply(RequestOptions.circleCropTransform())
                    .submit(size, size)
                    .get();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 50, stream);
            byte[] bytes = stream.toByteArray();
            bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            bitmapMap.put(path, bitmap);
            return bitmap;
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            Log.e("TAG", "getImg: " + e);
        }
        return null;
    }

    /**
     * 绘制地图
     *
     * @param canvas 画布
     */
    private void drawMap(Canvas canvas) {
        for (Territory[] lineTerr : mapTerrs) {
            for (Territory info : lineTerr) {
                if (info.nation == 0) {
                    Paint paint = terrPaint.get(info.nation);
                    canvas.drawRect(info.rectF, paint);
                } else {
                    Paint paint = warriorPatin.get(info.nation);
                    Rect src = new Rect();
                    src.set((int) info.rectF.left, (int) info.rectF.top, (int) info.rectF.right, (int) info.rectF.bottom);
                    RectF dst = new RectF();
                    dst.set(info.rectF);
                    canvas.drawBitmap(bitmaps[info.nation], src, dst, paint);
                }
            }
        }
        for (Territory capital : capitalMap.values()) {
            if (capital.isCapital) {
                String name = nationName[capital.nation];
                float nx = nationNamePaint.measureText(name) / 2;
                Paint.FontMetrics nfm = nationNamePaint.getFontMetrics();
                float ny = (nfm.bottom - nfm.ascent) / 2 - nfm.bottom;
                canvas.drawText(name, capital.rectF.centerX() - nx, capital.rectF.centerY() + ny,
                        nationNamePaint);

                Paint mapPaint = terrPaint.get(0);
                String hp = capital.hp + "";
                assert mapPaint != null;
                float tx = mapPaint.measureText(hp) / 2;
                Paint.FontMetrics fontMetrics = mapPaint.getFontMetrics();
                float ty = (fontMetrics.bottom - fontMetrics.ascent) / 2 - fontMetrics.bottom;
                canvas.drawText(hp, capital.rectF.centerX() - tx, capital.rectF.centerY() + ty,
                        mapPaint);
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
        for (Map.Entry<Integer, List<Warrior>> entry : warriorMap.entrySet()) {
            if (entry.getKey() == nation) continue;
            for (Warrior w : entry.getValue()) {
                if (checkSuccess(terr, w, false)) {
                    return true;
                }
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

    /**
     * 前进-更新每个战士前进后的坐标
     *
     * @param warriors 战士列表
     */
    private void advance(List<Warrior> warriors) {
        for (Warrior warrior : warriors) {
            advance(warrior);
        }
    }

    private void advance(Warrior warrior) {
        double xSpeed = warrior.getXSpeed();
        double ySpeed = warrior.getYSpeed();
        double wx = warrior.getX() + xSpeed;
        double wy = warrior.getY() + ySpeed;
        warrior.setCurrPoint((float) wx, (float) wy);
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
                        Map<Long, CaptureInfo> captureInfoMap = new HashMap<>();
                        for (int i = 1; i <= nationCount; i++) {
                            GameInfo info = new GameInfo();
                            info.nation = i;
                            List<Warrior> warriors = warriorMap.get(i);
                            if (warriors == null) {
                                info.warriorNum = 0;
                                info.userNum = 0;
                            } else {
                                info.warriorNum = warriors.size();
                                Set<Long> set = new HashSet<>();
                                for (Warrior w : warriors) {
                                    set.add(w.getUserDanMu().userid);
                                    CaptureInfo captureInfo =
                                            captureInfoMap.get(w.getUserDanMu().userid);
                                    if (captureInfo == null) {
                                        captureInfo = new CaptureInfo();
                                        captureInfo.userDanMu = w.getUserDanMu();
                                        captureInfoMap.put(w.getUserDanMu().userid, captureInfo);
                                    }
                                    captureInfo.captureCount += w.getCaptureCount();
                                }
                                info.userNum = set.size();
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
                        onUpdateGameInfoListener.onUpdate(gameInfos, captureInfoMap);
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

    public void addWarrior(int nation, UserDanMu userDanMu) {
        Territory territory = capitalMap.get(nation);
        if (territory != null && territory.isCapital) {
            List<Warrior> list = warriorMap.get(nation);
            if (list == null) {
                list = new ArrayList<>();
                warriorMap.put(nation, list);
            }
            for (Warrior w : list) {
                if (w.getUserDanMu().userid == userDanMu.userid) {
                    return;
                }
            }
            list.add(new Warrior(territory, userDanMu));
        }
    }

    public void changeNation(int nation, UserDanMu userDanMu) {
        List<Warrior> list = new ArrayList<>();
        for (Map.Entry<Integer, List<Warrior>> entry : warriorMap.entrySet()) {
            for (Warrior w : entry.getValue()) {
                if (w.getUserDanMu().userid == userDanMu.userid) {
                    list.add(w);
                }
            }
            if (!list.isEmpty()) {
                break;
            }
        }
        if (list.isEmpty()) {
            addWarrior(nation, userDanMu);
            return;
        }
        List<Warrior> warriors = warriorMap.get(list.get(0).getNation());
        if (warriors == null) {
            addWarrior(nation, userDanMu);
        } else {
            Territory capital = capitalMap.get(nation);
            List<Warrior> newWarriors = warriorMap.get(nation);
            if (newWarriors == null) {
                addWarrior(nation, userDanMu);
                return;
            }
            assert capital != null;
            float x = capital.rectF.centerX(), y = capital.rectF.centerY();
            warriors.removeAll(list);
            for (Warrior w : list) {
                w.setCurrPoint(x, y);
                w.setNation(nation);
                newWarriors.add(w);
            }
        }

    }

    public void goCapital(UserDanMu userDanMu) {
        boolean flag = false;
        for (Map.Entry<Integer, List<Warrior>> entry : warriorMap.entrySet()) {
            Territory capital = capitalMap.get(entry.getKey());
            float x = capital.rectF.centerX(), y = capital.rectF.centerY();
            for (Warrior w : entry.getValue()) {
                if (w.getUserDanMu().userid == userDanMu.userid) {
                    flag = true;
                    w.setCurrPoint(x, y);
                }
            }
            if (flag) return;
        }
    }

    public void randomBuff(UserDanMu userDanMu) {
        boolean flag = false;
        for (Map.Entry<Integer, List<Warrior>> entry : warriorMap.entrySet()) {
            for (Warrior w : entry.getValue()) {
                if (w.getUserDanMu().userid == userDanMu.userid) {
                    flag = true;
                    if (System.currentTimeMillis() % 10 == 0) {
                        addHelper(w);
                        return;
                    } else {
                        addSpeed(w, 1f);
                    }
                }
            }
            if (flag) return;
        }
    }

    public void addSpeed(UserDanMu userDanMu, float speed) {
        boolean flag = false;
        for (Map.Entry<Integer, List<Warrior>> entry : warriorMap.entrySet()) {
            for (Warrior w : entry.getValue()) {
                if (w.getUserDanMu().userid == userDanMu.userid) {
                    flag = true;
                    addSpeed(w, speed);
                }
            }
            if (flag) return;
        }
    }

    public void addSpeed(Warrior warrior, float speed) {
        warrior.setSpeed(warrior.getSpeed() + speed);
    }

    public void addHelper(Warrior warrior) {
        Territory capital = capitalMap.get(warrior.getNation());
        Warrior helper = new Warrior(capital, warrior.getUserDanMu());
        helper.setSpeed(warrior.getSpeed());
        List<Warrior> list = warriorMap.get(warrior.getNation());
        list.add(helper);
    }

    public void addHelper(UserDanMu userDanMu) {
        for (Map.Entry<Integer, List<Warrior>> entry : warriorMap.entrySet()) {
            for (Warrior w : entry.getValue()) {
                if (w.getUserDanMu().userid == userDanMu.userid) {
                    addHelper(w);
                    return;
                }
            }
        }
    }

    private OnUpdateGameInfoListener onUpdateGameInfoListener;

    public void setOnUpdateGameInfoListener(OnUpdateGameInfoListener onUpdateGameInfoListener) {
        this.onUpdateGameInfoListener = onUpdateGameInfoListener;
    }

    public interface OnUpdateGameInfoListener {
        void onUpdate(List<GameInfo> gameInfos, Map<Long, CaptureInfo> captureInfoMap);
    }
}

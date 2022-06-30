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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import zjy.android.bliveinteract.R;
import zjy.android.bliveinteract.manager.BitmapManager;
import zjy.android.bliveinteract.model.CaptureInfo;
import zjy.android.bliveinteract.model.GameMessage;
import zjy.android.bliveinteract.model.Territory;
import zjy.android.bliveinteract.model.UserDanMu;
import zjy.android.bliveinteract.model.Warrior;
import zjy.android.bliveinteract.skill.AddSpeedPercentSkill;
import zjy.android.bliveinteract.skill.BiggerSkill;
import zjy.android.bliveinteract.skill.InvalidAttackSkill;
import zjy.android.bliveinteract.skill.RandomBuffSkill;
import zjy.android.bliveinteract.skill.RandomTimeTask;
import zjy.android.bliveinteract.skill.ReduceSpeedSkill;
import zjy.android.bliveinteract.skill.Skill;
import zjy.android.bliveinteract.utils.CollisionUtils;

public class GameView extends SurfaceView implements Runnable, SurfaceHolder.Callback {

    private static final String TAG = "GameViewTag";

    private static final int lineCount = 42;
    private static final int rowCount = 42;

    public static final String[] groupNames = new String[]{"绿", "红", "黄", "蓝"};
    public static final int[] groupImageIds = new int[]{R.drawable.icon_heihuzi,
            R.drawable.icon_hongfa,
            R.drawable.icon_lufei, R.drawable.icon_baji};

    private boolean isDrawing = false;
    private SurfaceHolder holder;

    private final List<GameMessage> gameMessages = new ArrayList<>();

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
    private final int[][] capitalPoint = new int[][]{{7, 7}, {35, 7}, {7, 35}, {35, 35}};

    private static final long UPDATE_TIME = 1000 / 30;

    private final Random random = new Random();

    private int invalidAttackNation = -1;

    private final List<RandomTimeTask> randomTimeTasks = new ArrayList<>();

    private float initSpeed = 0;

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
        mapPaint.setStyle(Paint.Style.STROKE);
        mapPaint.setColor(Color.BLACK);

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
            weiCapital.hp = 8;
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
        initSkill();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {
        isDrawing = false;
        overSkill();
    }

    private void initSkill() {
        randomTimeTasks.add(new RandomTimeTask(Collections.singletonList(new InvalidAttackSkill(this, 0))));
        randomTimeTasks.add(new RandomTimeTask(Collections.singletonList(new ReduceSpeedSkill(this, 1))));
        List<Skill> skills = new ArrayList<>();
        skills.add(new AddSpeedPercentSkill(this, 2));
        skills.add(new BiggerSkill(this, 2));
        randomTimeTasks.add(new RandomTimeTask(skills));
        randomTimeTasks.add(new RandomTimeTask(Collections.singletonList(new RandomBuffSkill(this
                , 3))));
        for (RandomTimeTask task : randomTimeTasks) {
            task.start();
        }
    }

    public void overSkill() {
        for (RandomTimeTask task : randomTimeTasks) {
            task.dispose();
        }
        randomTimeTasks.clear();
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
            Canvas canvas = holder.lockHardwareCanvas();
            try {
                long start = System.currentTimeMillis();
                handleGameMessage();
                calculate();
                long c = System.currentTimeMillis() - start;
                drawMap(canvas);
                long dm = System.currentTimeMillis() - start - c;
                drawRoles(canvas);
                long dr = System.currentTimeMillis() - start - c - dm;
                updateGameInfo();
                long now = System.currentTimeMillis();
                long time = now - start;
                Log.e(TAG, "run: time = " + time + "/c = " + c + "/dm = " + dm + "/dr = " + dr);
                if (time < UPDATE_TIME) {
                    Thread.sleep(UPDATE_TIME - time);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                Log.e(TAG, "run: " + e);
            } finally {
                holder.unlockCanvasAndPost(canvas);
            }
        }
    }

    private void updateGameInfo() {
        Flowable.just(warriors)
                .subscribeOn(Schedulers.computation())
                .filter(data -> onUpdateGameInfoListener != null)
                .map(data -> {
                    Map<Long, CaptureInfo> captureInfoMap = new HashMap<>();
                    for (Warrior w : data) {
                        CaptureInfo captureInfo = captureInfoMap.get(w.getUserDanMu().userid);
                        if (captureInfo == null) {
                            captureInfo = new CaptureInfo();
                            captureInfo.userDanMu = w.getUserDanMu();
                            captureInfoMap.put(w.getUserDanMu().userid, captureInfo);
                        }
                        captureInfo.captureCount += w.getCaptureCount();
                        captureInfo.speed = w.getSpeed();
                    }
                    return new ArrayList<>(captureInfoMap.values());
                })
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(data -> onUpdateGameInfoListener.onUpdate(data))
                .subscribe();
    }

    private void handleGameMessage() {
        List<GameMessage> list = new ArrayList<>(gameMessages);
        gameMessages.clear();
        for (GameMessage gm : list) {
            switch (gm.type) {
                case GameMessage.TYPE_JOIN_GROUP:
                    addWarrior(gm.nation, gm.userDanMu);
                    break;
                case GameMessage.TYPE_ADD_SPEED:
                    addSpeed(gm.userDanMu, gm.speed);
                    break;
                case GameMessage.TYPE_CHANGE_GROUP:
                    changeNation(gm.nation, gm.userDanMu);
                    break;
                case GameMessage.TYPE_GO_CAPITAL:
                    goCapital(gm.userDanMu);
                    break;
                case GameMessage.TYPE_ADD_HELPER:
                    addHelper(gm.userDanMu);
                    break;
                case GameMessage.TYPE_RANDOM_BUFF:
                    randomBuff(gm.userDanMu);
                    break;
                case GameMessage.TYPE_ALL_ADD_SPEED:
                    initSpeed += gm.speed;
                    for (Warrior w : warriors) {
                        w.addSpeed(gm.speed);
                    }
                    break;
                case GameMessage.TYPE_GROUP_ADD_SPEED:
                    int nation = gm.userDanMu == null ? gm.nation : -1;
                    if (nation == -1) {
                        for (Warrior w : warriors) {
                            if (w.getUserDanMu().userid == gm.userDanMu.userid) {
                                nation = w.getNation();
                                break;
                            }
                        }
                    }
                    for (Warrior w : warriors) {
                        if (w.getNation() == nation) {
                            addSpeed(w, gm.speed);
                        }
                    }
                    gm.nation = nation;
                    break;
                case GameMessage.TYPE_ADD_RADIUS:
                    for (Warrior w : warriors) {
                        if (w.getUserDanMu().userid == gm.userDanMu.userid) {
                            w.addRadius(gm.radius);
                            break;
                        }
                    }
                    break;
                case GameMessage.TYPE_REMOVE_GROUP:
//                    List<Warrior> remove = new ArrayList<>();
                    for (Warrior w : warriors) {
                        if (w.getNation() == gm.nation) {
//                            remove.add(w);
                            w.setNation(-1);
                        }
                    }
//                    warriors.removeAll(remove);
                    break;
                case GameMessage.TYPE_GROUP_RANDOM_BUFF:
                    Map<Long, Warrior> map = new HashMap<>();
                    Set<Long> set = new HashSet<>();
                    for (Warrior w : warriors) {
                        boolean helper = random.nextInt() % (capitals[gm.nation].hp * 5) == 0;
                        if (w.getNation() == gm.nation) {
                            if (helper && !map.containsKey(w.getUserDanMu().userid)) {
                                map.put(w.getUserDanMu().userid, w);
                            } else if (!set.contains(w.getUserDanMu().userid)) {
                                float speed = (random.nextInt(5) + 1) / 10f;
                                addSpeed(w.getUserDanMu(), speed);
                                set.add(w.getUserDanMu().userid);
                            }
                        }
                    }
                    for (Warrior w : map.values()) {
                        addHelper(w);
                    }
                    break;
                case GameMessage.TYPE_GROUP_INVALID_ATTACK:
                    if (gm.dispose) {
                        invalidAttackNation = -1;
                    } else if (capitals[gm.nation].isCapital) {
                        invalidAttackNation = gm.nation;
                    }
                    break;
                case GameMessage.TYPE_GROUP_REDUCE_SPEED:
                    if (gm.dispose) {
                        for (Warrior w : warriors) {
                            if (w.getNation() != gm.nation) {
                                w.setReduceSpeedPercent(0);
                            }
                        }
                    } else if (capitals[gm.nation].isCapital) {
                        for (Warrior w : warriors) {
                            if (w.getNation() != gm.nation && random.nextInt() % 3 == 0) {
//                                w.setReduceSpeedPercent(0.9f);
                                w.addSpeed(-0.5f);
                            }
                        }
                    }
                    break;
                case GameMessage.TYPE_GROUP_BIGGER:
                    if (gm.dispose) {
                        for (Warrior w : warriors) {
                            if (w.getNation() == gm.nation) {
                                w.setRadiusPercent(0);
                            }
                        }
                    } else if (capitals[gm.nation].isCapital) {
                        for (Warrior w : warriors) {
                            if (w.getNation() == gm.nation) {
                                w.setRadiusPercent(1);
                            }
                        }
                    }
                    break;
                case GameMessage.TYPE_GROUP_ADD_SPEED_PERCENT:
                    if (gm.dispose) {
                        for (Warrior w : warriors) {
                            if (w.getNation() == gm.nation) {
                                w.setAddSpeedPercent(0);
                            }
                        }
                    } else if (capitals[gm.nation].isCapital) {
                        for (Warrior w : warriors) {
                            if (w.getNation() == gm.nation) {
                                w.setAddSpeedPercent(2);
                            }
                        }
                    }
                    break;
                case GameMessage.TYPE_CAPITAL_GUARD_TIME:
                    if (gm.dispose) {
                        capitals[gm.nation].guarding = false;
                    } else {
                        capitals[gm.nation].guarding = true;
                        Flowable.timer(3000, TimeUnit.MILLISECONDS)
                                .subscribeOn(Schedulers.computation())
                                .doOnComplete(() -> addGameMessage(GameMessage.createCapitalGuardTime(gm.nation, true)))
                                .subscribe();
                    }
                    break;
            }
        }
        if (onGameMessageListener != null) {
            onGameMessageListener.onMessage(list);
        }
    }

    private void calculate() {
        for (Warrior w : warriors) {
            if (w.getNation() == -1) continue;
            if (w.getXSpeed() > terrSize || w.getYSpeed() > terrSize) {
                int count;
                double xSpeed, ySpeed;
                if (w.getYSpeed() > w.getXSpeed()) {
                    count = (int) (w.getYSpeed() / terrSize + 0.5f);
                } else {
                    count = (int) (w.getXSpeed() / terrSize + 0.5f);
                }
                ySpeed = w.getYSpeed() / count;
                xSpeed = w.getXSpeed() / count;
                for (int i = 0; i < count; i++) {
                    advance(w, xSpeed, ySpeed);
                    if (attack(w) != 0) {
                        break;
                    }
                }
            } else {
                advance(w, w.getXSpeed(), w.getYSpeed());
                attack(w);
            }
        }
    }

    private int attack(Warrior warrior) {
        if (edgeCorrect(warrior)) {
            return -1;
        }
        float fx = warrior.getX();
        float fy = warrior.getY();
        float r = warrior.getRadius();
        int minL = Math.max((int) ((fx - r) / terrSize), 0);
        int maxL = Math.min((int) ((fx + r) / terrSize), rowCount - 1);
        int minR = Math.max((int) ((fy - r) / terrSize), 0);
        int maxR = Math.min((int) ((fy + r) / terrSize), lineCount - 1);
        int count = 0;
        boolean changeAngle = false;
        for (int i = minL; i <= maxL; i++) {
            for (int j = minR; j <= maxR; j++) {
                Territory territory = territories[i][j];
                if (territory.nation == warrior.getNation()) continue;
                int collisionType = CollisionUtils.shouldCollision(territory, warrior);
                if (collisionType > 0) {
                    if (!changeAngle) {
                        updateAngle(collisionType, warrior);
                        changeAngle = true;
                    }
                    if (invalidAttackNation != -1 && invalidAttackNation == territory.nation) {
                        for (Territory t : capitals) {
                            if (t.isCapital) {
                                addGameMessage(GameMessage.createGroupAddSpeed(0.0002f, invalidAttackNation));
                            }
                        }
                    }
                    if (territory.isCapital) {
                        if (territory.guarding) {
                            warrior.capture();
                            count++;
                        } else if (--territory.hp <= 0) {
                            addGameMessage(GameMessage.createRemoveGroup(territory.nation));
                            territory.isCapital = false;
                            territory.nation = warrior.getNation();
                            warrior.capture();
                            count++;
                            addGameMessage(GameMessage.createAllAddSpeed(5f));
                        } else {
                            warrior.capture();
                            count++;
                            addGameMessage(GameMessage.createCapitalGuardTime(territory.nation, false));
                            addGameMessage(GameMessage.createGroupAddSpeed(2, territory.nation));
                        }
                    } else {
                        boolean flag = false;
                        for (Warrior w : warriors) {
                            if (w.getNation() == warrior.getNation() || w.getNation() == -1) continue;
                            if (CollisionUtils.shouldCollision(territory, w) > 0) {
                                flag = true;
                                break;
                            }
                        }
                        if (!flag) {
                            territory.nation = warrior.getNation();
                        }
                        warrior.capture();
                        count++;
                    }
                }
            }
        }
        return count;
    }

    private void updateAngle(int collisionType, Warrior warrior) {
        switch (collisionType) {
            case 1:
                warrior.updateAngle(180, 270);
                break;
            case 2:
                warrior.updateAngle(1);
                break;
            case 3:
                warrior.updateAngle(270, 360);
                break;
            case 4:
                warrior.updateAngle(2);
                break;
            case 6:
                warrior.updateAngle(0);
                break;
            case 7:
                warrior.updateAngle(90, 180);
                break;
            case 8:
                warrior.updateAngle(3);
                break;
            case 9:
                warrior.updateAngle(0, 90);
                break;
        }
    }

    private boolean edgeCorrect(Warrior warrior) {
        float left = warrior.getX() - warrior.getRadius();
        float right = warrior.getX() + warrior.getRadius();
        float top = warrior.getY() - warrior.getRadius();
        float bottom = warrior.getY() + warrior.getRadius();
        if (top <= 0) {
            warrior.setCurrPoint(warrior.getX(), warrior.getRadius() + 1);
            warrior.updateAngle(1);
            return true;
        } else if (bottom >= getHeight()) {
            warrior.setCurrPoint(warrior.getX(), getHeight() - warrior.getRadius() - 1);
            warrior.updateAngle(3);
            return true;
        } else if (left <= 0) {
            warrior.setCurrPoint(warrior.getRadius() + 1, warrior.getY());
            warrior.updateAngle(2);
            return true;
        } else if (right >= getWidth()) {
            warrior.setCurrPoint(getWidth() - warrior.getRadius() - 1, warrior.getY());
            warrior.updateAngle(0);
            return true;
        }
        return false;
    }

    private void advance(Warrior warrior, double xSpeed, double ySpeed) {
        double wx = warrior.getX() + xSpeed;
        double wy = warrior.getY() + ySpeed;
        warrior.setCurrPoint((float) wx, (float) wy);
    }

    private void drawMap(Canvas canvas) {
        canvas.drawColor(Color.WHITE);
        for (Territory[] array : territories) {
            for (Territory territory : array) {
                if (territory.nation == -1) {
                    canvas.drawRect(territory.rectF, mapPaint);
                    continue;
                }
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

    private void drawRoles(Canvas canvas) {
        for (Warrior warrior : warriors) {
            if (warrior.getNation() == -1) continue;
            float l = warrior.getX() - warrior.getRadius();
            float t = warrior.getY() - warrior.getRadius();
            Bitmap bitmap;
            if (warrior.getRadius() == Warrior.RADIUS) {
                bitmap = BitmapManager.getBitmap(warrior.getUserDanMu().userid);
                canvas.drawBitmap(bitmap, l, t, rolePaint);
            } else {
                bitmap = BitmapManager.getBitmap(warrior.getUserDanMu().userid);
                src.set(0, 0, bitmap.getWidth(), bitmap.getHeight());
                dst.set(l, t, l + warrior.getRadius() * 2, t + warrior.getRadius() * 2);
                canvas.drawBitmap(bitmap, src, dst, rolePaint);
//                bitmap = BitmapManager.getSizeBitmap(warrior.getUserDanMu().userid,
//                        warrior.getRadius() * 2);
            }
        }
    }

    private final Rect src = new Rect();
    private final RectF dst = new RectF();

    public void addGameMessage(GameMessage gameMessage) {
        switch (gameMessage.type) {
            case GameMessage.TYPE_GROUP_RANDOM_BUFF:
            case GameMessage.TYPE_GROUP_INVALID_ATTACK:
            case GameMessage.TYPE_GROUP_REDUCE_SPEED:
            case GameMessage.TYPE_GROUP_BIGGER:
            case GameMessage.TYPE_GROUP_ADD_SPEED_PERCENT:
                if (!capitals[gameMessage.nation].isCapital && !gameMessage.dispose) {
                    return;
                }
                if (!gameMessage.dispose) {
                    boolean use = false;
                    for (Warrior w : warriors) {
                        if (w.getNation() == gameMessage.nation) {
                            use = true;
                            break;
                        }
                    }
                    if (!use) return;
                }
                break;
            case GameMessage.TYPE_JOIN_GROUP:
                if (!capitals[gameMessage.nation].isCapital) {
                    return;
                }
                if (checkUser(gameMessage.userDanMu)) {
                    return;
                }
                break;
            case GameMessage.TYPE_ADD_SPEED:
            case GameMessage.TYPE_GO_CAPITAL:
            case GameMessage.TYPE_ADD_HELPER:
            case GameMessage.TYPE_RANDOM_BUFF:
//            case GameMessage.TYPE_ALL_ADD_SPEED:
//            case GameMessage.TYPE_GROUP_ADD_SPEED:
            case GameMessage.TYPE_ADD_RADIUS:
                if (!capitals[gameMessage.nation].isCapital) {
                    return;
                }
                if (!checkUser(gameMessage.userDanMu)) {
                    return;
                }
                break;
        }
        gameMessages.add(gameMessage);
    }

    public boolean checkUser(UserDanMu userDanMu) {
        for (Warrior w : warriors) {
            if (userDanMu.userid == w.getUserDanMu().userid) {
                return true;
            }
        }
        return false;
    }

    private void addWarrior(int nation, UserDanMu userDanMu) {
        Territory territory = capitals[nation];
        if (territory != null && territory.isCapital) {
            for (Warrior w : warriors) {
                if (w.getUserDanMu().userid == userDanMu.userid) {
                    return;
                }
            }
            Warrior warrior = new Warrior(territory, userDanMu);
            warrior.addSpeed(initSpeed);
            warriors.add(warrior);
        }
    }

    private void changeNation(int nation, UserDanMu userDanMu) {
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
        if (capital.isCapital) {
            for (Warrior w : list) {
                w.setCurrPoint(capital.rectF.centerX(), capital.rectF.centerY());
                w.setNation(nation);
                w.setAddSpeedPercent(0);
                w.setRadiusPercent(0);
                w.setReduceSpeedPercent(0);
            }
        }
    }

    private void goCapital(UserDanMu userDanMu) {
        for (Warrior entry : warriors) {
            if (entry.getUserDanMu().userid == userDanMu.userid) {
                if (entry.getNation() == -1) break;
                Territory capital = capitals[entry.getNation()];
                float x = capital.rectF.centerX(), y = capital.rectF.centerY();
                entry.setCurrPoint(x, y);
            }
        }
    }

    private void randomBuff(UserDanMu userDanMu) {
        boolean helper = random.nextInt() % 10 == 0;
        for (Warrior w : warriors) {
            if (w.getUserDanMu().userid == userDanMu.userid) {
                if (helper) {
                    addHelper(w);
                    return;
                } else {
                    addSpeed(w, (random.nextInt(5) + 1) / 10f);
                }
            }
        }
    }

    private void addSpeed(UserDanMu userDanMu, float speed) {
        for (Warrior w : warriors) {
            if (w.getUserDanMu().userid == userDanMu.userid) {
                addSpeed(w, speed);
            }
        }
    }

    private void addSpeed(Warrior warrior, float speed) {
        warrior.addSpeed(speed);
    }

    private void addHelper(Warrior warrior) {
        if (warrior.getNation() == -1) return;
        Territory capital = capitals[warrior.getNation()];
        Warrior helper = new Warrior(capital, warrior);
        warriors.add(helper);
    }

    private void addHelper(UserDanMu userDanMu) {
        for (Warrior w : warriors) {
            if (w.getUserDanMu().userid == userDanMu.userid) {
                addHelper(w);
                return;
            }
        }
    }

    public void reset() {
//        init();
        warriors.clear();
        initTerritory();
        overSkill();
        initSkill();
        initSpeed = 0;
    }

    private OnUpdateGameInfoListener onUpdateGameInfoListener;

    public void setOnUpdateGameInfoListener(OnUpdateGameInfoListener onUpdateGameInfoListener) {
        this.onUpdateGameInfoListener = onUpdateGameInfoListener;
    }

    public interface OnUpdateGameInfoListener {
        void onUpdate(List<CaptureInfo> captureInfos);
    }

    private OnGameMessageListener onGameMessageListener;

    public void setOnGameMessageListener(OnGameMessageListener onGameMessageListener) {
        this.onGameMessageListener = onGameMessageListener;
    }

    public interface OnGameMessageListener {
        void onMessage(List<GameMessage> gameMessages);
    }
}

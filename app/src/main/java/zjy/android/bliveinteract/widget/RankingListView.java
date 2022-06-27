package zjy.android.bliveinteract.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import zjy.android.bliveinteract.model.GameMessage;

public class RankingListView extends SurfaceView implements Runnable, SurfaceHolder.Callback {

    private boolean isDrawing;
    private Paint normalPaint, buffPaint, skillPaint, defeatPaint;
    private SurfaceHolder holder;
    private final List<GameMessage> gameMessages = new ArrayList<>();

    private float textY, textH;

    public RankingListView(Context context) {
        super(context);
        init();
    }

    public RankingListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RankingListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        holder = getHolder();
        holder.addCallback(this);

        normalPaint = new Paint();
        normalPaint.setColor(Color.GRAY);
        normalPaint.setTextSize(38);

        skillPaint = new Paint();
        skillPaint.setColor(Color.RED);
        skillPaint.setTextSize(38);

        buffPaint = new Paint();
        buffPaint.setColor(Color.BLACK);
        buffPaint.setTextSize(38);

        defeatPaint = new Paint();
        defeatPaint.setColor(Color.BLUE);
        defeatPaint.setTextSize(38);

        Paint.FontMetrics fontMetrics = normalPaint.getFontMetrics();
        textY = (fontMetrics.bottom - fontMetrics.ascent) / 2 - fontMetrics.bottom;
        textH = fontMetrics.bottom - fontMetrics.ascent;
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
        isDrawing = true;
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
    public void run() {
        while (isDrawing) {
            Canvas canvas = holder.lockCanvas();
            try {
                canvas.drawColor(Color.WHITE);
                drawList(canvas);
                Thread.sleep(100);
            } catch (Exception e) {
                Log.e("RankingListViewTag", "run: " + e);
            } finally {
                holder.unlockCanvasAndPost(canvas);
            }
        }
    }

    private void drawList(Canvas canvas) {
        float y = textH - textY;
        int size = gameMessages.size();
        for (int i = 0; i < size; i++) {
            Paint paint = normalPaint;
            GameMessage message = gameMessages.get(i);
            String text = "";
            if (message.userDanMu != null) {
                text += message.userDanMu.username;
            }
            switch (message.type) {
                case GameMessage.TYPE_JOIN_GROUP:
                    text += "加入了" + GameView.groupNames[message.nation] + "方";
                    break;
                case GameMessage.TYPE_ADD_SPEED:
                    paint = buffPaint;
                    text += "获得了加速";
                    break;
                case GameMessage.TYPE_CHANGE_GROUP:
                    text += "反水到" + GameView.groupNames[message.nation] + "方";
                    break;
                case GameMessage.TYPE_GO_CAPITAL:
                    text += "回城";
                    break;
                case GameMessage.TYPE_ADD_HELPER:
                    paint = buffPaint;
                    text += "增加了分身";
                    break;
                case GameMessage.TYPE_RANDOM_BUFF:
                    paint = buffPaint;
                    text += "获得了随机buff";
                    break;
                case GameMessage.TYPE_ALL_ADD_SPEED:
                    paint = buffPaint;
                    text = "全体增加速度";
                    break;
                case GameMessage.TYPE_ADD_RADIUS:
                    paint = buffPaint;
                    text += "变大了";
                    break;
                case GameMessage.TYPE_GROUP_ADD_SPEED:
                    paint = buffPaint;
                    text = GameView.groupNames[message.nation] + "方阵营加速";
                    break;
                case GameMessage.TYPE_REMOVE_GROUP:
                    paint = defeatPaint;
                    text = GameView.groupNames[message.nation] + "方阵营战败";
                    break;
                case GameMessage.TYPE_GROUP_RANDOM_BUFF:
                    paint = skillPaint;
                    text = GameView.groupNames[message.nation] + "方发动被动（随机buff）";
                    break;
                case GameMessage.TYPE_GROUP_INVALID_ATTACK:
                    paint = skillPaint;
                    if (message.dispose) {
                        text = GameView.groupNames[message.nation] + "方被动技能结束";
                    } else {
                        text = GameView.groupNames[message.nation] + "方发动被动（敌方攻击无效）";
                    }
                    break;
                case GameMessage.TYPE_GROUP_REDUCE_SPEED:
                    paint = skillPaint;
                    if (message.dispose) {
                        text = GameView.groupNames[message.nation] + "方被动技能结束";
                    } else {
                        text = GameView.groupNames[message.nation] + "方发动被动（随机敌方速度减半）";
                    }
                    break;
                case GameMessage.TYPE_GROUP_BIGGER:
                    paint = skillPaint;
                    if (message.dispose) {
                        text = GameView.groupNames[message.nation] + "方被动技能结束";
                    } else {
                        text = GameView.groupNames[message.nation] + "方发动被动（己方变大）";
                    }
                    break;
                case GameMessage.TYPE_GROUP_ADD_SPEED_PERCENT:
                    paint = skillPaint;
                    if (message.dispose) {
                        text = GameView.groupNames[message.nation] + "方被动技能结束";
                    } else {
                        text = GameView.groupNames[message.nation] + "方发动被动（增加倍速）";
                    }
                    break;
            }
            canvas.drawText(text, 0, y, paint);
            y += textH;
        }
    }

    public void setGameMessages(List<GameMessage> gameMessages) {
        int size = gameMessages.size();
        if (size == 0) return;
        int oldSize = this.gameMessages.size();
        if (oldSize >= 8) {
            if (size < 8) {
                this.gameMessages.subList(0, size).clear();
            } else {
                this.gameMessages.clear();
            }
        }
        this.gameMessages.addAll(gameMessages);
    }

    public void setGameMessages(GameMessage gameMessage) {
        if (this.gameMessages.size() >= 8) {
            this.gameMessages.remove(0);
        }
        this.gameMessages.add(gameMessage);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), 800);
    }
}

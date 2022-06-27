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
    private Paint textPaint;
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

        textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(38);

        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
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
                    text += "获得了加速";
                    break;
                case GameMessage.TYPE_CHANGE_GROUP:
                    text += "反水到" + GameView.groupNames[message.nation] + "方";
                    break;
                case GameMessage.TYPE_GO_CAPITAL:
                    text += "回城";
                    break;
                case GameMessage.TYPE_ADD_HELPER:
                    text += "增加了分身";
                    break;
                case GameMessage.TYPE_RANDOM_BUFF:
                    text += "获得了随机buff";
                    break;
                case GameMessage.TYPE_ALL_ADD_SPEED:
                    text = "全体增加速度";
                    break;
                case GameMessage.TYPE_ADD_RADIUS:
                    text += "变大了";
                    break;
                case GameMessage.TYPE_GROUP_ADD_SPEED:
                    text = GameView.groupNames[message.nation] + "方阵营加速";
                    break;
                case GameMessage.TYPE_REMOVE_GROUP:
                    text = GameView.groupNames[message.nation] + "方阵营战败";
                    break;
            }
            canvas.drawText(text, 0, y, textPaint);
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

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
    private List<GameMessage> gameMessages = new ArrayList<>();

    private float textY;

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
        textPaint.setTextSize(28);

        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        textY = (fontMetrics.bottom - fontMetrics.ascent) / 2 - fontMetrics.bottom;
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
        StringBuilder builder = new StringBuilder();
        for (int i = gameMessages.size() - 1; i >= 0; i--) {
            GameMessage message = gameMessages.get(i);
            if (message.userDanMu != null) {
                builder.append(message.userDanMu.username);
            }
            switch (message.type) {
                case GameMessage.TYPE_JOIN_GROUP:
                    builder.append("加入了");
                    break;
                case GameMessage.TYPE_ADD_SPEED:
                    builder.append("获得了加速");
                    break;
                case GameMessage.TYPE_CHANGE_GROUP:
                    builder.append("反水了");
                    break;
                case GameMessage.TYPE_GO_CAPITAL:
                    builder.append("回城");
                    break;
                case GameMessage.TYPE_ADD_HELPER:
                    builder.append("增加了分身");
                    break;
                case GameMessage.TYPE_RANDOM_BUFF:
                    builder.append("获得了随机buff");
                    break;
                case GameMessage.TYPE_ALL_ADD_SPEED:
                    builder.append("全体增加速度");
                    break;
            }
            builder.append('\n');
        }
        canvas.drawText(builder.toString(), 0, getHeight() - textY, textPaint);
    }

    public void setGameMessages(List<GameMessage> gameMessages) {
        int size = gameMessages.size();
        if (size > 0 && this.gameMessages.size() >= 8) {
            this.gameMessages.subList(0, size).clear();
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

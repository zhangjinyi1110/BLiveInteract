package zjy.android.bliveinteract.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import zjy.android.bliveinteract.model.GameInfo;

public class RankingListView extends SurfaceView implements Runnable, SurfaceHolder.Callback {

    private final int size = WarGameView.nationName.length;
    private boolean isDrawing;
    private Paint nationNamePaint, textPaint, textPaint3;
    private final Paint[] bgPaint = new Paint[size];
    private SurfaceHolder holder;
    private final RectF[] rectFS = new RectF[size];
    private float itemHeight;
    private List<GameInfo> gameInfoList = new ArrayList<>();

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

        nationNamePaint = new Paint();
        nationNamePaint.setColor(Color.WHITE);
        nationNamePaint.setTextSize(68);

        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(28);

        textPaint3 = new Paint();
        textPaint3.setColor(Color.WHITE);

        for (int i = 0; i < size; i++) {
            rectFS[i] = new RectF();
            Paint paint = new Paint();
            paint.setTextSize(78);
            paint.setColor(Color.parseColor(WarGameView.terrColorStr[i]));
            bgPaint[i] = paint;
        }
        Paint.FontMetrics fontMetrics = bgPaint[0].getFontMetrics();
        itemHeight = fontMetrics.descent - fontMetrics.ascent;
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
        int width = getWidth();
        float height = 0;
        for (int i = 1; i < size; i++) {
            rectFS[i].set(0, height, width, height += itemHeight);
        }
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
        int i = 1;
        for (GameInfo info : gameInfoList) {
            int nation = info.nation;
            canvas.drawRect(rectFS[i], bgPaint[nation]);
            Paint.FontMetrics fontMetrics = nationNamePaint.getFontMetrics();
            float h = (fontMetrics.descent - fontMetrics.ascent) / 2 - fontMetrics.bottom;
            float top = (i - 1) * itemHeight;
            float y = top + itemHeight / 2 + h;
            canvas.drawText(WarGameView.nationName[nation], 0, y, nationNamePaint);

            canvas.drawText("领土：" + info.terrNum, 70, top + 35, textPaint);
            canvas.drawText("城池：" + info.capitalNum, 250, top + 35, textPaint);
            canvas.drawText("将军：" + info.userNum, 70, top + 75, textPaint);
            canvas.drawText("战士：" + info.warriorNum, 250, top + 75, textPaint);

            i++;
        }
    }

    public void setGameInfoList(List<GameInfo> gameInfoList) {
        this.gameInfoList = gameInfoList;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), (int) (itemHeight * 7 + 0.5));
    }
}

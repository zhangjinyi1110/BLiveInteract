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

import zjy.android.bliveinteract.model.CaptureInfo;

public class CaptureRankingView extends SurfaceView implements Runnable, SurfaceHolder.Callback {

    private SurfaceHolder holder;
    private boolean isDrawing;

    private final int size = WarGameView.nationName.length;
    private final  Paint[] bgPaint = new Paint[size];
    private Paint namePaint, capturePaint, imgPaint;

    private List<CaptureInfo> captureInfos = new ArrayList<>();

    public CaptureRankingView(Context context) {
        super(context);
        init();
    }

    public CaptureRankingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CaptureRankingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        holder = getHolder();
        holder.addCallback(this);

        namePaint = new Paint();
        namePaint.setTextSize(28);
        namePaint.setColor(Color.BLACK);
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
                canvas.drawColor(Color.parseColor("#f5f5f5"));
                drawCaptureRanking(canvas);
                Thread.sleep(100);
            } catch (Exception e) {
                Log.e("CaptureRankingViewTag", "run: " + e);
            } finally {
                holder.unlockCanvasAndPost(canvas);
            }
        }
    }

    private void drawCaptureRanking(Canvas canvas) {
        int size = Math.min(10, captureInfos.size());
        int height = 70;
        for (int i = 0; i < size; i++) {
            CaptureInfo captureInfo = captureInfos.get(i);
            String count = captureInfo.captureCount + "";
            float cw = namePaint.measureText(count);
            String name;
            if (captureInfo.userDanMu.username.length() < 9) {
                name = captureInfo.userDanMu.username;
            } else {
                name = captureInfo.userDanMu.username.substring(0, 9);
            }
            canvas.drawText(name, 100, i * height + 48, namePaint);
            canvas.drawText(count, getWidth() - cw - 10, i * height + 48, namePaint);
        }
    }

    public void setCaptureInfos(List<CaptureInfo> captureInfos) {
        this.captureInfos = captureInfos;
    }
}

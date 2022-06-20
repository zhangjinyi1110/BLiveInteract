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
import java.util.List;

import zjy.android.bliveinteract.R;
import zjy.android.bliveinteract.model.Territory;
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
                drawMap(canvas);
                drawRoles(canvas);
                long now = System.currentTimeMillis();
                long time = now - start;
                Log.e(TAG, "run: " + time);
                if (time < 10) {
                    Thread.sleep(10 - time);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                Log.e(TAG, "run: " + e);
            } finally {
                holder.unlockCanvasAndPost(canvas);
            }
        }
    }

    private void calculate() {
        for (Warrior warrior : warriors) {
            double fx = warrior.getX() + warrior.getXSpeed();
            double fy = warrior.getY() + warrior.getYSpeed();
            if (fx > fy) {

            } else {

            }
            int count = (int) (Math.max(Math.abs(fx - warrior.getX()),
                    Math.abs(fy - warrior.getY())) / terrSize + 0.5f);
            for (int i = 0; i < count; i++) {
                if (canAttack())
            }
        }
    }

    private void drawMap(Canvas canvas) {
        canvas.drawColor(Color.WHITE);
        for (Territory[] array : territories) {
            for (Territory territory : array) {
                if (territory.nation == 0) continue;
                groupImageSrc.set((int) territory.rectF.left, (int) territory.rectF.top,
                        (int) territory.rectF.right, (int) territory.rectF.bottom);
                groupImageDst.set(territory.rectF.left, territory.rectF.top,
                        territory.rectF.right, territory.rectF.bottom);
                canvas.drawBitmap(groupImages[territory.nation - 1], groupImageSrc, groupImageDst
                        , mapPaint);
            }
        }
        for (Territory capital : capitals) {
            if (capital.isCapital) {
                float x = capital.rectF.centerX(), y = capital.rectF.centerY();
                canvas.drawText(groupNames[capital.nation - 1], x - groupNameHalfWidth,
                        y - groupNameHalfHeight, groupNamePaint);

                canvas.drawText(String.valueOf(capital.hp), x - hpHalfWidth, y - hpHalfHeight,
                        mapPaint);
            }
        }
    }

    private void drawRoles(Canvas canvas) {
        for (Warrior warrior : warriors) {
//            canvas.
        }
    }
}

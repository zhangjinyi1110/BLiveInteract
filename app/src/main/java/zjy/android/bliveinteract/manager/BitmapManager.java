package zjy.android.bliveinteract.manager;

import android.graphics.Bitmap;
import android.graphics.Matrix;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import zjy.android.bliveinteract.App;
import zjy.android.bliveinteract.model.Warrior;

public class BitmapManager {

    private static final Map<Long, Bitmap> normalMap = new HashMap<>();
    private static final Map<String, Bitmap> sizeMap = new HashMap<>();

    public static boolean checkUser(long uid) {
        return normalMap.containsKey(uid);
    }

    public static void cacheBitmap(long uid, String imgPath) throws ExecutionException,
            InterruptedException {
        int size = (int) (Warrior.RADIUS * 2);
        Bitmap bitmap = Glide.with(App.getApp())
                .asBitmap()
                .load(imgPath)
                .apply(RequestOptions.circleCropTransform())
                .submit(size, size)
                .get();
        normalMap.put(uid, bitmap);
    }

    public static Bitmap getBitmap(long uid) {
        return normalMap.get(uid);
    }

    public static Bitmap getSizeBitmap(long uid, float size) {
        Bitmap bitmap = sizeMap.get(uid + "/" + size);
        if (bitmap != null) return bitmap;
        bitmap = getBitmap(uid);
        int oldWidth = bitmap.getWidth(), oldHeight = bitmap.getHeight();
        Matrix matrix = new Matrix();
        matrix.postScale(size / oldWidth, size / oldHeight);
        Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, oldWidth, oldHeight, matrix, true);
        sizeMap.put(uid + "/" + size, newBitmap);
        return newBitmap;
    }

}

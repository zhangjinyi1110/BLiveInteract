package zjy.android.bliveinteract.utils;

import android.app.Application;
import android.widget.Toast;

public class ToastUtils {

    private final Application application;
    private static ToastUtils utils;

    private ToastUtils(Application application) {
        this.application = application;
    }

    public static void init(Application application) {
        utils = new ToastUtils(application);
    }

    public static void showShort(String text) {
        Toast.makeText(utils.application, text, Toast.LENGTH_SHORT).show();
    }

}

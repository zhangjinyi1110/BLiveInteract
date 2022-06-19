package zjy.android.bliveinteract;

import android.app.Application;

import com.bumptech.glide.Glide;

import zjy.android.bliveinteract.network.Api;
import zjy.android.bliveinteract.utils.ToastUtils;

public class App extends Application {

    private static App app;

    @Override
    public void onCreate() {
        super.onCreate();
        ToastUtils.init(this);
        app = this;
    }

    public static App getApp() {
        return app;
    }
}

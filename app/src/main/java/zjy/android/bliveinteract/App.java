package zjy.android.bliveinteract;

import android.app.Application;

import zjy.android.bliveinteract.utils.ToastUtils;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ToastUtils.init(this);
    }
}

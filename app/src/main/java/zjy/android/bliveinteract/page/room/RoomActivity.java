package zjy.android.bliveinteract.page.room;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import zjy.android.bliveinteract.R;
import zjy.android.bliveinteract.contract.MainContract;
import zjy.android.bliveinteract.model.UserDanMu;
import zjy.android.bliveinteract.utils.ToastUtils;

public class RoomActivity extends FragmentActivity {

    private long backTime = System.currentTimeMillis();

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            if (iBinder instanceof RoomService.RoomBinder) {
                ((RoomService.RoomBinder) iBinder).setMainHandler(handler);
                ((RoomService.RoomBinder) iBinder).connectBLive(roomId);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == MainContract.WHAT_DAN_MU) {
                UserDanMu userDanMu = (UserDanMu) msg.obj;
                handleDanMu(userDanMu);
            }
        }
    };

    private long roomId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);
        roomId = getIntent().getLongExtra("roomId", 0);
        bindService(new Intent(this, RoomService.class), connection, BIND_AUTO_CREATE);
    }

    @Override
    public void onBackPressed() {
        long nowTime = System.currentTimeMillis();
        if (nowTime - backTime > 700) {
            backTime = nowTime;
            ToastUtils.showShort("再次返回可退出程序");
        } else {
            super.onBackPressed();
        }
    }

    private void handleDanMu(UserDanMu userDanMu) {

    }
}

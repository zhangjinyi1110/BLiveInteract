package zjy.android.bliveinteract.page.room;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import zjy.android.bliveinteract.R;
import zjy.android.bliveinteract.contract.MainContract;
import zjy.android.bliveinteract.model.UserDanMu;
import zjy.android.bliveinteract.utils.ToastUtils;
import zjy.android.bliveinteract.widget.WarGameView;

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

    private WarGameView warGameView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);
        roomId = getIntent().getLongExtra("roomId", 0);
        bindService(new Intent(this, RoomService.class), connection, BIND_AUTO_CREATE);

        warGameView = findViewById(R.id.war_game);
        initBtn();
    }

    private void initBtn() {
        findViewById(R.id.wei_warrior).setOnClickListener(view -> {
            warGameView.addWarrior(1);
        });
        findViewById(R.id.shu_warrior).setOnClickListener(view -> {
            warGameView.addWarrior(2);
        });
        findViewById(R.id.wu_warrior).setOnClickListener(view -> {
            warGameView.addWarrior(3);
        });
        findViewById(R.id.qun_warrior).setOnClickListener(view -> {
            warGameView.addWarrior(4);
        });
        findViewById(R.id.wei_speed).setOnClickListener(view -> {
            warGameView.addSpeed(1);
        });
        findViewById(R.id.shu_speed).setOnClickListener(view -> {
            warGameView.addSpeed(2);
        });
        findViewById(R.id.wu_speed).setOnClickListener(view -> {
            warGameView.addSpeed(3);
        });
        findViewById(R.id.qun_speed).setOnClickListener(view -> {
            warGameView.addSpeed(4);
        });
        random();
    }

    Random random = new Random();

    private void random() {
        Flowable.timer(3, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .map(aLong -> random.nextInt(8))
                .doOnNext(integer -> {
                    switch (integer) {
                        case 0:
                            findViewById(R.id.wei_warrior).performClick();
                            break;
                        case 1:
                            findViewById(R.id.shu_warrior).performClick();
                            break;
                        case 2:
                            findViewById(R.id.wu_warrior).performClick();
                            break;
                        case 3:
                            findViewById(R.id.qun_warrior).performClick();
                            break;
                        case 4:
                            findViewById(R.id.wei_speed).performClick();
                            break;
                        case 5:
                            findViewById(R.id.shu_speed).performClick();
                            break;
                        case 6:
                            findViewById(R.id.wu_speed).performClick();
                            break;
                        case 7:
                            findViewById(R.id.qun_speed).performClick();
                            break;
                    }
                })
                .doOnComplete(this::random).subscribe();
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

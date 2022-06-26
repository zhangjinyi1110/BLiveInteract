package zjy.android.bliveinteract.page.room;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import zjy.android.bliveinteract.R;
import zjy.android.bliveinteract.contract.MainContract;
import zjy.android.bliveinteract.model.GameMessage;
import zjy.android.bliveinteract.model.UserDanMu;
import zjy.android.bliveinteract.utils.ToastUtils;
import zjy.android.bliveinteract.widget.CaptureRankingView;
import zjy.android.bliveinteract.widget.GameView;
import zjy.android.bliveinteract.widget.RankingListView;

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
            } else if (msg.what == MainContract.WHAT_COMBO) {
                UserDanMu userDanMu = (UserDanMu) msg.obj;
                handleCombo(userDanMu);
            }
        }
    };

    private long roomId;

    private GameView warGameView;

    private RankingListView rankingListView;

    private CaptureRankingView captureRankingView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);
        roomId = getIntent().getLongExtra("roomId", 0);
        bindService(new Intent(this, RoomService.class), connection, BIND_AUTO_CREATE);

        warGameView = findViewById(R.id.war_game);
        rankingListView = findViewById(R.id.ranking_list);
        captureRankingView = findViewById(R.id.capture_ranking);
        initBtn();
    }

    private TextView timeView;
    private Disposable disposable;

    private void timeout() {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
        disposable = Flowable.intervalRange(0, 410, 0, 1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.computation())
                .map(aLong -> 410 - aLong)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(aLong -> timeView.setText(String.valueOf(aLong)))
                .subscribe();
    }

    private void initBtn() {
        timeView = findViewById(R.id.time);
        timeout();
//        UserDanMu userDanMu = new UserDanMu(1, "aaa", "红", "http://i1.hdslb.com/bfs/face/68937f0b8d49c4e537e0822c13fc8e4e050234a3" +
//                ".jpg");
        findViewById(R.id.reset).setOnClickListener(v -> {
            warGameView.reset();
//            warGameView.addWarrior(0, userDanMu);
            timeout();
        });
        findViewById(R.id.speed).setOnClickListener(v -> warGameView.addGameMessage(GameMessage.createAddSpeed(30, userDanMu)));
        warGameView.setOnUpdateGameInfoListener((captureInfos) -> {
            Collections.sort(captureInfos, (o1, o2) -> o2.captureCount - o1.captureCount);
            captureRankingView.setCaptureInfos(captureInfos);
        });
        warGameView.setOnGameMessageListener((gameMessages -> rankingListView.setGameMessages(gameMessages)));
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
//        if (true) return;
        for (int i = 0; i < GameView.groupNames.length; i++) {
            if (userDanMu.danMu.equals(GameView.groupNames[i])) {
                this.userDanMu = userDanMu;
//                warGameView.addWarrior(i, userDanMu);
                warGameView.addGameMessage(GameMessage.createJoinGroup(i, userDanMu));
                return;
            }
        }
        if (userDanMu.danMu.startsWith("反水") && userDanMu.danMu.length() == 3) {
            String name = userDanMu.danMu.substring(2);
            for (int i = 0; i < GameView.groupNames.length; i++) {
                if (name.equals(GameView.groupNames[i])) {
//                    warGameView.changeNation(i, userDanMu);
                    warGameView.addGameMessage(GameMessage.createChangeGroup(i, userDanMu));
                    return;
                }
            }
        }
        switch (userDanMu.danMu) {
            case "TP":
            case "B":
            case "b":
            case "Tp":
            case "tP":
            case "tp":
                warGameView.addGameMessage(GameMessage.createGoCapital(userDanMu));
//                warGameView.goCapital(userDanMu);
                return;
        }
//        warGameView.randomBuff(userDanMu);
        warGameView.addGameMessage(GameMessage.createRandomBuff(userDanMu));
    }

    private UserDanMu userDanMu;

    private void handleCombo(UserDanMu userDanMu) {
        if (userDanMu.giftId == 1) {//辣条
//            warGameView.addSpeed(userDanMu, 2f);
            warGameView.addGameMessage(GameMessage.createAddSpeed(1f, userDanMu));
        } else if (userDanMu.giftId == 31036) {//小花花
//            warGameView.addSpeed(userDanMu, 4);
            warGameView.addGameMessage(GameMessage.createAddSpeed(2f, userDanMu));
        } else if (userDanMu.giftId == 31037) {//打call
//            warGameView.addHelper(userDanMu);
            warGameView.addGameMessage(GameMessage.createAddHelper(userDanMu));
        } else if (userDanMu.giftId == 31039) {//牛哇

        } else if (userDanMu.giftId == 30971) {//这个好诶

        } else if (userDanMu.giftId == 31025) {//泡泡糖

        } else if (userDanMu.giftId == 30896) {//打榜

        } else if (userDanMu.giftId == 30426) {//能量石

        } else if (userDanMu.giftId == 20011) {//金币

        }
    }
}

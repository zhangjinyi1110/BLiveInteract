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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import zjy.android.bliveinteract.R;
import zjy.android.bliveinteract.adapter.RankingAdapter;
import zjy.android.bliveinteract.contract.MainContract;
import zjy.android.bliveinteract.model.CaptureInfo;
import zjy.android.bliveinteract.model.UserDanMu;
import zjy.android.bliveinteract.utils.ToastUtils;
import zjy.android.bliveinteract.widget.CaptureRankingView;
import zjy.android.bliveinteract.widget.GameView;
import zjy.android.bliveinteract.widget.RankingListView;
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
        findViewById(R.id.reset).setOnClickListener(v -> {
//            warGameView.reset();
            timeout();
        });
//        findViewById(R.id.speed).setOnClickListener(v -> warGameView.addSpeed(userDanMu, 30));
//        warGameView.setOnUpdateGameInfoListener((gameInfos, captureInfoMap) -> {
//            Collections.sort(gameInfos, (o1, o2) -> {
//                if (o1.terrNum > o2.terrNum) return -1;
//                else if (o1.terrNum < o2.terrNum) return 1;
//                else if (o1.warriorNum > o2.warriorNum) return -1;
//                else if (o1.warriorNum < o2.warriorNum) return 1;
//                else if (o1.capitalNum > o2.capitalNum) return -1;
//                else if (o1.capitalNum < o2.capitalNum) return 1;
//                else if (o1.nation < o2.nation) return -1;
//                else if (o1.nation > o2.nation) return 1;
//                return 0;
//            });
//            List<CaptureInfo> captureInfoList = new ArrayList<>(captureInfoMap.values());
//            Collections.sort(captureInfoList, (o1, o2) -> o2.captureCount - o1.captureCount);
//            rankingListView.setGameInfoList(gameInfos);
//            captureRankingView.setCaptureInfos(captureInfoList);
//        });
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
        for (int i = 0; i < GameView.groupNames.length; i++) {
            if (userDanMu.danMu.equals(WarGameView.nationName[i])) {
                this.userDanMu = userDanMu;
                warGameView.addWarrior(i, userDanMu);
                return;
            }
        }
        if (userDanMu.danMu.startsWith("投靠 ") && userDanMu.danMu.length() == 4) {
            String name = userDanMu.danMu.substring(3);
            for (int i = 0; i < GameView.groupNames.length; i++) {
                if (name.equals(GameView.groupNames[i])) {
                    warGameView.changeNation(i, userDanMu);
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
                warGameView.goCapital(userDanMu);
                return;
        }
        warGameView.randomBuff(userDanMu);
    }

    private UserDanMu userDanMu;

    private void handleCombo(UserDanMu userDanMu) {
        if (userDanMu.giftId == 1) {//辣条
            warGameView.addSpeed(userDanMu, 0.5f);
        } else if (userDanMu.giftId == 31036) {//小花花
            warGameView.addSpeed(userDanMu, 3);
        } else if (userDanMu.giftId == 31037) {//打call
            warGameView.addHelper(userDanMu);
        } else if (userDanMu.giftId == 31039) {//牛哇

        } else if (userDanMu.giftId == 30971) {//这个好诶

        }
    }
}

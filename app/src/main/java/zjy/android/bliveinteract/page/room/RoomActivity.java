package zjy.android.bliveinteract.page.room;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;

import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import zjy.android.bliveinteract.R;
import zjy.android.bliveinteract.contract.MainContract;
import zjy.android.bliveinteract.databinding.ActivityRoomBinding;
import zjy.android.bliveinteract.manager.BitmapManager;
import zjy.android.bliveinteract.model.GameMessage;
import zjy.android.bliveinteract.model.GroupInfo;
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

    private ActivityRoomBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_room);
        roomId = getIntent().getLongExtra("roomId", 0);
        bindService(new Intent(this, RoomService.class), connection, BIND_AUTO_CREATE);

        warGameView = findViewById(R.id.war_game);
        rankingListView = findViewById(R.id.ranking_list);
        captureRankingView = findViewById(R.id.capture_ranking);
        initBtn();
        initGroup();
    }

    private void initGroup() {
        GroupInfo red = new GroupInfo();
        red.color = Color.parseColor("#FF3715");
        red.groupName = "红发";
        red.passiveSkill = "给我个面子";
        red.passiveSkillInfo = "随机敌方降低速度，持续3秒";
        binding.groupRed.setData(red);
        GroupInfo black = new GroupInfo();
        black.color = Color.parseColor("#333333");
        black.groupName = "黑胡子";
        black.passiveSkill = "吸收反弹";
        black.passiveSkillInfo = "被攻击增加速度，持续3秒";
        binding.groupBlack.setData(black);
        GroupInfo blue = new GroupInfo();
        blue.color = Color.parseColor("#1678FF");
        blue.groupName = "巴基";
        blue.passiveSkill = "霸王色运气";
        blue.passiveSkillInfo = "己方随机buff";
        binding.groupBlue.setData(blue);
        GroupInfo yellow = new GroupInfo();
        yellow.color = Color.parseColor("#FFDA17");
        yellow.groupName = "草帽";
        yellow.passiveSkill = "二/三档";
        yellow.passiveSkillInfo = "加速/变大，持续3秒";
        binding.groupYellow.setData(yellow);
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
                .filter(aLong -> aLong / 100 == 0)
                .doOnNext(aLong -> warGameView.addGameMessage(GameMessage.createAllAddSpeed(5)))
                .subscribe();
    }

    private void initBtn() {
        timeView = findViewById(R.id.time);
        timeout();
        findViewById(R.id.reset).setOnClickListener(v -> {
            warGameView.reset();
            timeout();
        });
        new Thread(() -> {
            try {
                BitmapManager.cacheBitmap(1, "http://i1.hdslb.com/bfs/face/68937f0b8d49c4e537e0822c13fc8e4e050234a3" +
                        ".jpg");
                BitmapManager.cacheBitmap(2, "http://i1.hdslb.com/bfs/face/68937f0b8d49c4e537e0822c13fc8e4e050234a3" +
                        ".jpg");
                BitmapManager.cacheBitmap(2, "http://i1.hdslb.com/bfs/face/68937f0b8d49c4e537e0822c13fc8e4e050234a3" +
                        ".jpg");
                BitmapManager.cacheBitmap(3, "http://i1.hdslb.com/bfs/face/68937f0b8d49c4e537e0822c13fc8e4e050234a3" +
                        ".jpg");
                BitmapManager.cacheBitmap(4, "http://i1.hdslb.com/bfs/face/68937f0b8d49c4e537e0822c13fc8e4e050234a3" +
                        ".jpg");
                BitmapManager.cacheBitmap(5, "http://i1.hdslb.com/bfs/face/68937f0b8d49c4e537e0822c13fc8e4e050234a3" +
                        ".jpg");
                BitmapManager.cacheBitmap(6, "http://i1.hdslb.com/bfs/face/68937f0b8d49c4e537e0822c13fc8e4e050234a3" +
                        ".jpg");
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
        findViewById(R.id.speed).setOnClickListener(v -> {
            UserDanMu userDanMu = new UserDanMu(1, "aaa", "红");
            UserDanMu userDanMu1 = new UserDanMu(2, "aaa", "红");
            UserDanMu userDanMu2 = new UserDanMu(2, "aaa", "红");
            UserDanMu userDanMu3 = new UserDanMu(3, "aaa", "红");
            UserDanMu userDanMu4 = new UserDanMu(4, "aaa", "红");
            UserDanMu userDanMu5 = new UserDanMu(5, "aaa", "红");
            UserDanMu userDanMu6 = new UserDanMu(6, "aaa", "红");
            warGameView.addGameMessage(GameMessage.createJoinGroup(0, userDanMu));
            warGameView.addGameMessage(GameMessage.createJoinGroup(2, userDanMu1));
            warGameView.addGameMessage(GameMessage.createJoinGroup(1, userDanMu2));
            warGameView.addGameMessage(GameMessage.createJoinGroup(1, userDanMu3));
            warGameView.addGameMessage(GameMessage.createJoinGroup(0, userDanMu4));
            warGameView.addGameMessage(GameMessage.createJoinGroup(0, userDanMu5));
            warGameView.addGameMessage(GameMessage.createJoinGroup(3, userDanMu6));
            warGameView.addGameMessage(GameMessage.createAddHelper(userDanMu));
            warGameView.addGameMessage(GameMessage.createAddHelper(userDanMu));
            warGameView.addGameMessage(GameMessage.createAddHelper(userDanMu));
            warGameView.addGameMessage(GameMessage.createAddHelper(userDanMu));
            warGameView.addGameMessage(GameMessage.createAddHelper(userDanMu));
            warGameView.addGameMessage(GameMessage.createAddHelper(userDanMu));
            warGameView.addGameMessage(GameMessage.createAddHelper(userDanMu));
            warGameView.addGameMessage(GameMessage.createAddHelper(userDanMu));
            warGameView.addGameMessage(GameMessage.createAddHelper(userDanMu));
            warGameView.addGameMessage(GameMessage.createAddHelper(userDanMu));
            warGameView.addGameMessage(GameMessage.createAddHelper(userDanMu));
            warGameView.addGameMessage(GameMessage.createAddHelper(userDanMu));
            warGameView.addGameMessage(GameMessage.createAddHelper(userDanMu));
            warGameView.addGameMessage(GameMessage.createAddHelper(userDanMu));
            warGameView.addGameMessage(GameMessage.createAddHelper(userDanMu));
            warGameView.addGameMessage(GameMessage.createAddHelper(userDanMu));
            warGameView.addGameMessage(GameMessage.createAddHelper(userDanMu));
            warGameView.addGameMessage(GameMessage.createAddHelper(userDanMu));
            warGameView.addGameMessage(GameMessage.createAddHelper(userDanMu));
            warGameView.addGameMessage(GameMessage.createAddHelper(userDanMu));
            warGameView.addGameMessage(GameMessage.createAddHelper(userDanMu));
            warGameView.addGameMessage(GameMessage.createAddHelper(userDanMu));
            warGameView.addGameMessage(GameMessage.createAddHelper(userDanMu));
            warGameView.addGameMessage(GameMessage.createAddHelper(userDanMu));
            warGameView.addGameMessage(GameMessage.createAddHelper(userDanMu));
            warGameView.addGameMessage(GameMessage.createAddHelper(userDanMu));
            warGameView.addGameMessage(GameMessage.createAddSpeed(150, userDanMu));
        });
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
                warGameView.addGameMessage(GameMessage.createJoinGroup(i, userDanMu));
                return;
            }
        }
        if (userDanMu.danMu.startsWith("反水") && userDanMu.danMu.length() == 3) {
            String name = userDanMu.danMu.substring(2);
            for (int i = 0; i < GameView.groupNames.length; i++) {
                if (name.equals(GameView.groupNames[i])) {
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
                return;
        }
        warGameView.addGameMessage(GameMessage.createRandomBuff(userDanMu));
    }

    private UserDanMu userDanMu;

    private void handleCombo(UserDanMu userDanMu) {
        if (userDanMu.giftId == 1) {//辣条
            warGameView.addGameMessage(GameMessage.createAddSpeed(0.5f * userDanMu.giftNum, userDanMu));
        } else if (userDanMu.giftId == 31036) {//小花花
            warGameView.addGameMessage(GameMessage.createAddSpeed(1f * userDanMu.giftNum, userDanMu));
        } else if (userDanMu.giftId == 31037) {//打call
            for (int i = 0; i < userDanMu.giftNum; i++) {
                warGameView.addGameMessage(GameMessage.createAddHelper(userDanMu));
            }
        } else if (userDanMu.giftId == 31039) {//牛哇

        } else if (userDanMu.giftId == 30971) {//这个好诶
            warGameView.addGameMessage(GameMessage.createAddRadius(10f * userDanMu.giftNum, userDanMu));
        } else if (userDanMu.giftId == 31025) {//泡泡糖

        } else if (userDanMu.giftId == 30896) {//打榜

        } else if (userDanMu.giftId == 30426) {//能量石

        } else if (userDanMu.giftId == 20011) {//金币

        } else if (userDanMu.giftId == 31026) {//白银宝箱
            warGameView.addGameMessage(GameMessage.createGroupAddSpeed(0.5f * userDanMu.giftNum, userDanMu));
        }
    }
}

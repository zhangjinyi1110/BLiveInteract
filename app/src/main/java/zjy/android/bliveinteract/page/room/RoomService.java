package zjy.android.bliveinteract.page.room;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import okio.Buffer;
import okio.ByteString;
import zjy.android.bliveinteract.App;
import zjy.android.bliveinteract.contract.MainContract;
import zjy.android.bliveinteract.contract.RoomContract;
import zjy.android.bliveinteract.manager.BitmapManager;
import zjy.android.bliveinteract.model.UserDanMu;
import zjy.android.bliveinteract.network.RetrofitHelper;
import zjy.android.bliveinteract.utils.ZLibUtils;
import zjy.android.zwebsocket.IConnectCallback;
import zjy.android.zwebsocket.IReadCallback;
import zjy.android.zwebsocket.ZWebSocket;
import zjy.android.zwebsocket.request.Request;

public class RoomService extends Service {

    public static final String TAG = "RoomServiceTag";

    private final RoomBinder binder = new RoomBinder();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "onCreate: ");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand: ");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.e(TAG, "onUnbind: ");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy: ");
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.e(TAG, "onBind: ");
        return binder;
    }

    public static class RoomBinder extends Binder {

        private RoomHandler handler;

        private RoomBinder() {
            Flowable.just(new Object())
                    .subscribeOn(Schedulers.io())
                    .doOnNext(o -> {
                        Looper.prepare();
                        handler = new RoomHandler(Looper.myLooper());
                        Looper.loop();
                    }).subscribe();
        }

        public void connectBLive(long roomId) {
            Message message = Message.obtain();
            message.what = RoomContract.WHAT_CONNECT;
            message.obj = roomId;
            handler.sendMessage(message);
        }

        public void setMainHandler(Handler handler) {
            this.handler.setHandler(handler);
        }

    }

    private static class RoomHandler extends Handler implements IConnectCallback, IReadCallback {

        private static final String TAG = "RoomHandlerTag";

        private Handler handler;
        private long roomId;
        private ZWebSocket webSocket;
        private final Buffer PING_BUFFER = new Buffer();
        private final Buffer MESSAGE = new Buffer();

        private final Gson gson = new Gson();

        public RoomHandler(@NonNull Looper looper) {
            super(looper);
        }

        public void setHandler(Handler handler) {
            this.handler = handler;
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            try {
                if (msg.what == RoomContract.WHAT_CONNECT) {
                    this.roomId = (long) msg.obj;
                    connectWebSocket();
                }
            } catch (IOException e) {
                Log.e(TAG, "handleMessage: " + e);
            }
        }

        private void connectWebSocket() throws IOException {
            if (webSocket != null) {
                webSocket.unregisterConnectCallback(this);
                webSocket.unregisterReadCallback(this);
            }
            webSocket = new ZWebSocket();
            webSocket.registerConnectCallback(this);
            webSocket.registerReadCallback(this);
            Request request = new Request.Builder()
                    .setUrl("ws://broadcastlv.chat.bilibili.com:2244/sub")
                    .setPingIntervalMill(300000)
                    .build();
            webSocket.connect(request);
        }

        private void joinRoom() {
            try {
                int headLen = 16;
                Map<String, Object> map = new HashMap<>();
                map.put("uid", 0);
                map.put("roomid", roomId);
                map.put("protover", 1);
                map.put("platform", "android");
                map.put("clientver", "1.4.0");
                String json = new Gson().toJson(map);
                byte[] data = json.getBytes(StandardCharsets.UTF_8);
                Buffer buffer = new Buffer();
                buffer.writeInt(headLen + data.length);
                buffer.writeShort(headLen);
                buffer.writeShort(1);
                buffer.writeInt(7);
                buffer.writeInt(1);
                buffer.write(data);
                webSocket.send(buffer.readByteString());
            } catch (IOException e) {
                Log.e(TAG, "joinRoom: " + e);
            }
        }

        private void ping() {
            PING_BUFFER.writeInt(16);
            PING_BUFFER.writeShort(16);
            PING_BUFFER.writeShort(1);
            PING_BUFFER.writeInt(2);
            PING_BUFFER.writeInt(1);
            Flowable.timer(30, TimeUnit.SECONDS)
                    .filter(aLong -> webSocket == null)
                    .doOnNext((a) -> webSocket.send(PING_BUFFER.readByteString()))
                    .doOnNext((a) -> ping())
                    .subscribe();
        }

        @Override
        public void onConnecting() {
            Log.e(TAG, "onConnecting: ");
        }

        @Override
        public void onConnected() {
            Log.e(TAG, "onConnected: ");
            joinRoom();
            ping();
        }

        @Override
        public void onClosing(int code, String reason) {
            Log.e(TAG, "onClosing: " + code + "/" + reason);
            try {
                Thread.sleep(100);
                connectWebSocket();
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "onConnectFail: reconnect error: " + e);
            }
        }

        @Override
        public void onClosed(int code, String reason) {
            Log.e(TAG, "onClosed: " + code + "/" + reason);
        }

        @Override
        public void onConnectFail(int code, String reason) {
            Log.e(TAG, "onConnectFail: " + code + "/" + reason);
        }

        @Override
        public void onRead(String message) {

        }

        @Override
        public void onRead(ByteString message) {
            MESSAGE.write(message);
            try {
                int total = MESSAGE.readInt();
                int headLen = MESSAGE.readShort();
                int pVersion = MESSAGE.readShort();
                int opcode = MESSAGE.readInt();
                int sequence = MESSAGE.readInt();
                if (opcode == 5) {
                    if (pVersion == 2) {
                        resolveData(MESSAGE.readByteArray(total - headLen));
                    } else if (pVersion <= 1) {
                        MESSAGE.readByteArray(total - headLen);
                    } else {
                        MESSAGE.readByteArray(total - headLen);
                        Log.e(TAG, "onRead: opcode = 5, pVersion = " + pVersion);
                    }
                } else if (opcode == 3) {
                    MESSAGE.readByteArray(total - headLen);
//                    Log.e(TAG, "onRead: 人气值：" + MESSAGE.readInt());
                } else {
                    MESSAGE.readByteArray(total - headLen);
                }
            } catch (IOException e) {
                Log.e(TAG, "onRead: " + e);
            }
        }

        @SuppressWarnings("unchecked")
        private void resolveData(byte[] bytes) {
            String originData = new String(ZLibUtils.decompress(bytes), StandardCharsets.UTF_8);
            String regEx = "[\\x00-\\x1f]+";
            Pattern p = Pattern.compile(regEx);
            Matcher m = p.matcher(originData);
            String data = m.replaceAll("===").trim();
            String[] jsonList = data.split("===");
            for (String item : jsonList) {
                if (item.startsWith("{\"")) {
                    Map<String, Object> map = gson.fromJson(item, new TypeToken<Map<String,
                            Object>>() {
                    }.getType());
                    String cmd = (String) map.get("cmd");
                    if ("DANMU_MSG".equals(cmd)) {
                        handleDanMu((List<Object>) Objects.requireNonNull(map.get("info")));
                    } else if ("COMBO_SEND".equals(cmd)) {
                        Log.e("TAG", "onRead1: COMBO_SEND " + map);
                    } else if ("SEND_GIFT".equals(cmd)) {
//                        Map<String, Object> combo = (Map<String, Object>) map.get("data");
                        Log.e("TAG", "onRead2: COMBO_SEND " + gson.toJson(map.get("data")));
                        handleCombo((Map<String, Object>) map.get("data"));
                    } else if ("WELCOME".equals(cmd)) {
                        Log.e("TAG", "onRead2: WELCOME " + map);
                    } else if ("INTERACT_WORD".equals(cmd)) {
                        interactWord((Map<String, Object>)map.get("data"));
                    }
                }
            }

        }

        private void handleCombo(Map<String, Object> data) {
            long uid = (long) ((double) data.get("uid"));
            long giftId = (long) ((double) data.get("giftId"));
            String giftName = (String) data.get("giftName");
            if (data.get("blind_gift") != null) {
                Map<String, Object> original = (Map<String, Object>) data.get("blind_gift");

                giftId = (long) ((double) original.get("original_gift_id"));
                giftName = (String) original.get("original_gift_name");
            }
            int num = (int) ((double) data.get("num"));
            Log.e(TAG, "handleCombo: " + giftId + "/" + giftName + "/" + num);
            UserDanMu userDanMu = new UserDanMu(uid, giftName, giftId);
            if (handler != null) {
                Message message = Message.obtain();
                message.obj = userDanMu;
                message.what = MainContract.WHAT_COMBO;
                handler.sendMessage(message);
            }
        }

//        private final Map<Long, String> imgMap = new HashMap<>();

        private void interactWord(Map<String, Object> data) {
//            long uid = (long) ((double) data.get("uid"));
//            RetrofitHelper.createUserApi()
//                    .userInfo(uid)
//                    .retry()
//                    .filter(userInfo -> userInfo.code == 200)
//                    .map(userInfo -> userInfo.data)
//                    .doOnNext(dataDTO -> imgMap.put(dataDTO.uid, dataDTO.avatar))
//                    .doOnNext(dataDTO -> Glide.with(App.getApp()).load(dataDTO.avatar).submit())
//                    .subscribe();
        }

        @SuppressWarnings("unchecked")
        private void handleDanMu(List<Object> info) {
            String danMu = (String) info.get(1);
            List<Object> user = (List<Object>) info.get(2);
            long userid = (long) ((double) user.get(0));
            String username = (String) user.get(1);
            UserDanMu userDanMu = new UserDanMu(userid, username, danMu);
            if (BitmapManager.checkUser(userid)) {
                if (handler != null) {
                    Message message = Message.obtain();
                    message.obj = userDanMu;
                    message.what = MainContract.WHAT_DAN_MU;
                    handler.sendMessage(message);
                }
            } else {
                RetrofitHelper.createUserApi()
                        .userInfo(userid)
                        .subscribeOn(Schedulers.newThread())
                        .filter(userInfo -> userInfo.code == 200)
                        .map(userInfo -> userInfo.data)
//                        .doOnNext(dataDTO -> imgMap.put(dataDTO.uid, dataDTO.avatar))
                        .doOnNext(dataDTO -> BitmapManager.cacheBitmap(dataDTO.uid, dataDTO.avatar))
//                        .doOnNext(dataDTO -> userDanMu.img = dataDTO.avatar)
                        .doOnComplete(() -> {
                            if (handler != null) {
                                Message message = Message.obtain();
                                message.obj = userDanMu;
                                message.what = MainContract.WHAT_DAN_MU;
                                handler.sendMessage(message);
                            }
                        })
                        .retry()
                        .subscribe();
            }
        }
    }
}

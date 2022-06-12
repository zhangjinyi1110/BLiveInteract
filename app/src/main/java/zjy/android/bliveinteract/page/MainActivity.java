package zjy.android.bliveinteract.page;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import zjy.android.bliveinteract.R;
import zjy.android.bliveinteract.network.RetrofitHelper;
import zjy.android.bliveinteract.page.room.RoomActivity;
import zjy.android.bliveinteract.utils.ToastUtils;

public class MainActivity extends AppCompatActivity {

    EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editText = findViewById(R.id.room_id);
        editText.setOnEditorActionListener((textView, i, keyEvent) -> checkRoomId(textView.getText()));

        checkRoomId("6");
    }

    private boolean checkRoomId(CharSequence text) {
        if (text == null) {
            ToastUtils.showShort("请输入房间号");
            return false;
        }
        long roomId;
        try {
            roomId = Integer.parseInt(text.toString());
        } catch (NumberFormatException e) {
            ToastUtils.showShort("请输入正确格式的房间号");
            return false;
        }
        getRealRoomId(roomId);
        return true;
    }

    private void getRealRoomId(long roomId) {
        RetrofitHelper.createApi()
                .roomInit(roomId)
                .subscribeOn(Schedulers.io())
                .map(roomInfo -> {
                    if (roomInfo.code == 0) {
                        return roomInfo.data;
                    }
                    throw new IllegalStateException("code is not 0, code id " + roomInfo.code);
                })
                .map(dataDTO -> dataDTO.roomId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Long>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        s.request(Long.MAX_VALUE);
                    }

                    @Override
                    public void onNext(Long aLong) {
                        startActivity(new Intent(MainActivity.this, RoomActivity.class)
                                .putExtra("roomId", aLong));
                        finish();
                    }

                    @Override
                    public void onError(Throwable t) {
                        ToastUtils.showShort("获取房间信息失败：" + t.toString());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }
}
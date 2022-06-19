package zjy.android.bliveinteract.network;

import io.reactivex.Flowable;
import retrofit2.http.GET;
import retrofit2.http.Query;
import zjy.android.bliveinteract.model.RoomInfo;
import zjy.android.bliveinteract.model.UserInfo;

public interface Api {

    @GET("/room/v1/Room/room_init")
    Flowable<RoomInfo> roomInit(@Query("id") long id);

    @GET("/bilibili")
    Flowable<UserInfo> userInfo(@Query("uid") long id);

}

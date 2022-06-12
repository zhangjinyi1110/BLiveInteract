package zjy.android.bliveinteract.model;

import com.google.gson.annotations.SerializedName;

public class RoomInfo {
    @SerializedName("code")
    public int code;
    @SerializedName("msg")
    public String msg;
    @SerializedName("message")
    public String message;
    @SerializedName("data")
    public DataDTO data;

    public static class DataDTO {
        @SerializedName("room_id")
        public long roomId;
        @SerializedName("short_id")
        public long shortId;
        @SerializedName("uid")
        public long uid;
        @SerializedName("need_p2p")
        public int needP2p;
        @SerializedName("is_hidden")
        public boolean isHidden;
        @SerializedName("is_locked")
        public boolean isLocked;
        @SerializedName("is_portrait")
        public boolean isPortrait;
        @SerializedName("live_status")
        public int liveStatus;
        @SerializedName("hidden_till")
        public int hiddenTill;
        @SerializedName("lock_till")
        public int lockTill;
        @SerializedName("encrypted")
        public boolean encrypted;
        @SerializedName("pwd_verified")
        public boolean pwdVerified;
        @SerializedName("live_time")
        public int liveTime;
        @SerializedName("room_shield")
        public int roomShield;
        @SerializedName("is_sp")
        public int isSp;
        @SerializedName("special_type")
        public int specialType;
    }
}

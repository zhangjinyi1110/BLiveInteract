package zjy.android.bliveinteract.model;

import com.google.gson.annotations.SerializedName;

public class UserInfo {
    @SerializedName("code")
    public int code;
    @SerializedName("data")
    public DataDTO data;

    public static class DataDTO {
        @SerializedName("uid")
        public long uid;
        @SerializedName("name")
        public String name;
        @SerializedName("level")
        public int level;
        @SerializedName("sex")
        public String sex;
        @SerializedName("description")
        public String description;
        @SerializedName("avatar")
        public String avatar;
    }
}

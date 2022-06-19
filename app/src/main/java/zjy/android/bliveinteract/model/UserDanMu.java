package zjy.android.bliveinteract.model;

import java.io.Serializable;

public class UserDanMu implements Serializable {

    public long userid;
    public String username;
    public String danMu;
    public String img;
    public long giftId;
    public String giftName;

    public UserDanMu(long userid, String username, String danMu, String img) {
        this.userid = userid;
        this.username = username;
        this.danMu = danMu;
        this.img = img;
    }

    public UserDanMu(long userid, String giftName, long giftId) {
        this.userid = userid;
        this.giftName = giftName;
        this.giftId = giftId;
    }
}

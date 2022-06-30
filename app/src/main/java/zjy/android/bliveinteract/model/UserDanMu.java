package zjy.android.bliveinteract.model;

import java.io.Serializable;

public class UserDanMu implements Serializable {

    public long userid;
    public String username;
    public String danMu;
//    public String img;
    public long giftId;
    public String giftName;
    public int giftNum;

    public UserDanMu(long userid, String username, String danMu) {
        this.userid = userid;
        this.username = username;
        this.danMu = danMu;
    }

    public UserDanMu(long userid, String giftName, long giftId, int giftNum) {
        this.userid = userid;
        this.giftName = giftName;
        this.giftId = giftId;
        this.giftNum = giftNum;
    }
}

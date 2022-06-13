package zjy.android.bliveinteract.model;

import java.io.Serializable;

public class UserDanMu implements Serializable {

    public long userid;
    public String username;
    public String danMu;

    public UserDanMu(long userid, String username, String danMu) {
        this.userid = userid;
        this.username = username;
        this.danMu = danMu;
    }
}

package zjy.android.bliveinteract.model;

import androidx.annotation.NonNull;

import zjy.android.bliveinteract.widget.WarGameView;

public class GameInfo {

    public int terrNum;
    public int warriorNum;
    public int capitalNum;
    public int nation;

    @NonNull
    @Override
    public String toString() {
        return "国家：" + WarGameView.nationName[nation] +
                "   城池：" + capitalNum +
                "   战士：" + warriorNum +
                "   领土：" + terrNum;
    }
}

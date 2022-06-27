package zjy.android.bliveinteract.skill;

import zjy.android.bliveinteract.model.GameMessage;
import zjy.android.bliveinteract.widget.GameView;

public class RandomBuffSkill implements Skill {

    private final GameView gameView;
    private final int nation;

    public RandomBuffSkill(GameView gameView, int nation) {
        this.gameView = gameView;
        this.nation = nation;
    }

    @Override
    public void useSkill() {
        this.gameView.addGameMessage(GameMessage.createGroupRandomBuff(nation));
    }

    @Override
    public void overSkill() {

    }

    @Override
    public long skillTime() {
        return 0;
    }
}

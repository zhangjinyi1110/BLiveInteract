package zjy.android.bliveinteract.skill;

import zjy.android.bliveinteract.model.GameMessage;
import zjy.android.bliveinteract.widget.GameView;

public class ReduceSpeedSkill implements Skill {

    private final GameView gameView;
    private final int nation;

    public ReduceSpeedSkill(GameView gameView, int nation) {
        this.gameView = gameView;
        this.nation = nation;
    }

    @Override
    public void useSkill() {
        this.gameView.addGameMessage(GameMessage.createGroupReduceSpeed(nation, false));
    }

    @Override
    public void overSkill() {
        this.gameView.addGameMessage(GameMessage.createGroupReduceSpeed(nation, true));
    }

    @Override
    public long skillTime() {
        return 1000;
    }
}

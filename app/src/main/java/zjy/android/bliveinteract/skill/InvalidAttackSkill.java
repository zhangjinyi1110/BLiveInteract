package zjy.android.bliveinteract.skill;

import zjy.android.bliveinteract.model.GameMessage;
import zjy.android.bliveinteract.widget.GameView;

public class InvalidAttackSkill implements Skill{

    private final GameView gameView;
    private final int nation;

    public InvalidAttackSkill(GameView gameView, int nation) {
        this.gameView = gameView;
        this.nation = nation;
    }

    @Override
    public void useSkill() {
        this.gameView.addGameMessage(GameMessage.createGroupInvalidAttack(nation, false));
    }

    @Override
    public void overSkill() {
        this.gameView.addGameMessage(GameMessage.createGroupInvalidAttack(nation, true));
    }

    @Override
    public long skillTime() {
        return 3000;
    }
}

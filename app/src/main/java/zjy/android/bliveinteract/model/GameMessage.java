package zjy.android.bliveinteract.model;

public class GameMessage {

    public static final int TYPE_JOIN_GROUP = 0;
    public static final int TYPE_ADD_SPEED = 1;
    public static final int TYPE_CHANGE_GROUP = 2;
    public static final int TYPE_GO_CAPITAL = 3;
    public static final int TYPE_ADD_HELPER = 4;
    public static final int TYPE_RANDOM_BUFF = 5;
    public static final int TYPE_ALL_ADD_SPEED = 6;
    public static final int TYPE_GROUP_ADD_SPEED = 7;
    public static final int TYPE_ADD_RADIUS = 8;
    public static final int TYPE_REMOVE_GROUP = 9;

    public int type;
    public int nation;
    public UserDanMu userDanMu;
    public float speed;
    public float radius;

    public static GameMessage createJoinGroup(int nation, UserDanMu userDanMu) {
        GameMessage gameMessage = new GameMessage();
        gameMessage.nation = nation;
        gameMessage.type = TYPE_JOIN_GROUP;
        gameMessage.userDanMu = userDanMu;
        return gameMessage;
    }

    public static GameMessage createAddSpeed(float speed, UserDanMu userDanMu) {
        GameMessage gameMessage = new GameMessage();
        gameMessage.speed = speed;
        gameMessage.type = TYPE_ADD_SPEED;
        gameMessage.userDanMu = userDanMu;
        return gameMessage;
    }

    public static GameMessage createChangeGroup(int nation, UserDanMu userDanMu) {
        GameMessage gameMessage = new GameMessage();
        gameMessage.nation = nation;
        gameMessage.type = TYPE_CHANGE_GROUP;
        gameMessage.userDanMu = userDanMu;
        return gameMessage;
    }

    public static GameMessage createGoCapital(UserDanMu userDanMu) {
        GameMessage gameMessage = new GameMessage();
        gameMessage.type = TYPE_GO_CAPITAL;
        gameMessage.userDanMu = userDanMu;
        return gameMessage;
    }

    public static GameMessage createAddHelper(UserDanMu userDanMu) {
        GameMessage gameMessage = new GameMessage();
        gameMessage.type = TYPE_ADD_HELPER;
        gameMessage.userDanMu = userDanMu;
        return gameMessage;
    }

    public static GameMessage createRandomBuff(UserDanMu userDanMu) {
        GameMessage gameMessage = new GameMessage();
        gameMessage.type = TYPE_RANDOM_BUFF;
        gameMessage.userDanMu = userDanMu;
        return gameMessage;
    }

    public static GameMessage createAllAddSpeed(float speed) {
        GameMessage gameMessage = new GameMessage();
        gameMessage.type = TYPE_ALL_ADD_SPEED;
        gameMessage.speed = speed;
        return gameMessage;
    }

    public static GameMessage createGroupAddSpeed(float speed, UserDanMu userDanMu) {
        GameMessage gameMessage = new GameMessage();
        gameMessage.type = TYPE_GROUP_ADD_SPEED;
        gameMessage.speed = speed;
        gameMessage.userDanMu = userDanMu;
        return gameMessage;
    }

    public static GameMessage createAddRadius(float radius, UserDanMu userDanMu) {
        GameMessage gameMessage = new GameMessage();
        gameMessage.type = TYPE_GROUP_ADD_SPEED;
        gameMessage.radius = radius;
        gameMessage.userDanMu = userDanMu;
        return gameMessage;
    }

    public static GameMessage createRemoveGroup(int nation) {
        GameMessage gameMessage = new GameMessage();
        gameMessage.type = TYPE_REMOVE_GROUP;
        gameMessage.nation = nation;
        return gameMessage;
    }

}

package enums;

public enum Actions {
    ANT_ACTION_KILL,
    ANT_ACTION_NEST,
    ANT_ACTION_UP,
    ANT_ACTION_DOWN,
    ANT_ACTION_LEFT,
    ANT_ACTION_RIGHT,
    ANT_ACTION_COLLECT_FOOD,
    ANT_ACTION_COLLECT_MATERIAL,
    ANT_ACTION_DROP_FOOD,
    ANT_ACTION_DROP_MATERIAL,
    ANT_ACTION_STEAL,
    ANT_ACTION_LOGIN;

    public static Actions getOpposite(Actions action) {
        switch (action){
            case ANT_ACTION_LEFT:
                return ANT_ACTION_RIGHT;
            case ANT_ACTION_RIGHT:
                return ANT_ACTION_LEFT;
            case ANT_ACTION_UP:
                return ANT_ACTION_DOWN;
            case ANT_ACTION_DOWN:
                return ANT_ACTION_UP;
            default:
                return null;
        }
    }
}

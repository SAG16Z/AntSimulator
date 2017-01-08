package messages;

import enums.Actions;

import java.awt.*;

public class AntMessageCreator {

    private final String LOGIN;
    private final String UP;
    private final String DOWN;
    private final String LEFT;
    private final String RIGHT;
    private final String COLLECT;
    private final String DROP;

    /**
     * Creates a message creator object which will use the given color for it's
     * messages. Used by Ant agents.
     *
     * @param color
     *      Ant color
     */
    public AntMessageCreator(Color color) {
        AntMessage msg = new AntMessage();
        msg.setColor(color);

        LOGIN = MessageUtil.asJsonAntMessage(msg, Actions.ANT_ACTION_LOGIN);
        UP = MessageUtil.asJsonAntMessage(msg, Actions.ANT_ACTION_UP);
        DOWN = MessageUtil.asJsonAntMessage(msg, Actions.ANT_ACTION_DOWN);
        LEFT = MessageUtil.asJsonAntMessage(msg, Actions.ANT_ACTION_LEFT);
        RIGHT = MessageUtil.asJsonAntMessage(msg, Actions.ANT_ACTION_RIGHT);
        COLLECT = MessageUtil.asJsonAntMessage(msg, Actions.ANT_ACTION_COLLECT);
        DROP = MessageUtil.asJsonAntMessage(msg, Actions.ANT_ACTION_DROP);
    }


    public String getLOGIN() {
        return LOGIN;
    }

    public String getUP() {
        return UP;
    }

    public String getDOWN() {
        return DOWN;
    }

    public String getLEFT() {
        return LEFT;
    }

    public String getRIGHT() {
        return RIGHT;
    }

    public String getCOLLECT() {
        return COLLECT;
    }

    public String getDROP() {
        return DROP;
    }

}

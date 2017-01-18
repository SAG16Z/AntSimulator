package messages;

import enums.Actions;

import java.awt.*;

public class AntMessageCreator {

    private final String LOGIN, UP, UP_AND_PHEROMONES,
                            DOWN, DOWN_AND_PHEROMONES,
                            LEFT, LEFT_AND_PHEROMONES,
                            RIGHT, RIGHT_AND_PHEROMONES,
                            COLLECT, DROP;

    /**
     * Creates a message creator object which will use the given color for it's
     * messages. Used by Ant agents.
     *
     * @param color
     *      Ant color
     */
    public AntMessageCreator(int color) {
        AntMessage msg = new AntMessage();
        msg.setColor(color);

        LOGIN = MessageUtil.asJsonAntMessage(msg, Actions.ANT_ACTION_LOGIN);
        UP = MessageUtil.asJsonAntMessage(msg, Actions.ANT_ACTION_UP);
        UP_AND_PHEROMONES = MessageUtil.asJsonAntMessage(msg, Actions.ANT_ACTION_UP, true);
        DOWN = MessageUtil.asJsonAntMessage(msg, Actions.ANT_ACTION_DOWN);
        DOWN_AND_PHEROMONES = MessageUtil.asJsonAntMessage(msg, Actions.ANT_ACTION_DOWN, true);
        LEFT = MessageUtil.asJsonAntMessage(msg, Actions.ANT_ACTION_LEFT);
        LEFT_AND_PHEROMONES = MessageUtil.asJsonAntMessage(msg, Actions.ANT_ACTION_LEFT, true);
        RIGHT = MessageUtil.asJsonAntMessage(msg, Actions.ANT_ACTION_RIGHT);
        RIGHT_AND_PHEROMONES = MessageUtil.asJsonAntMessage(msg, Actions.ANT_ACTION_RIGHT, true);
        COLLECT = MessageUtil.asJsonAntMessage(msg, Actions.ANT_ACTION_COLLECT);
        DROP = MessageUtil.asJsonAntMessage(msg, Actions.ANT_ACTION_DROP);
    }

    public String getMessageForAction(Actions action){
        return getMessageForAction(action, false);
    }

    public String getMessageForAction(Actions action, boolean leavesPheromones){
        switch (action){
            case ANT_ACTION_LOGIN:
                return LOGIN;
            case ANT_ACTION_UP:
                return leavesPheromones ? UP_AND_PHEROMONES : UP;
            case ANT_ACTION_DOWN:
                return leavesPheromones ? DOWN_AND_PHEROMONES : DOWN;
            case ANT_ACTION_LEFT:
                return leavesPheromones ? LEFT_AND_PHEROMONES : LEFT;
            case ANT_ACTION_RIGHT:
                return leavesPheromones ? RIGHT_AND_PHEROMONES : RIGHT;
            case ANT_ACTION_COLLECT:
                return COLLECT;
            case ANT_ACTION_DROP:
                return DROP;
            default:
                return "";
        }
    }
}

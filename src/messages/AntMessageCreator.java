package messages;

import enums.Actions;

import java.awt.*;

public class AntMessageCreator {

    private final String LOGIN, UP, UP_AND_PHEROMONES,
                            DOWN, DOWN_AND_PHEROMONES,
                            LEFT, LEFT_AND_PHEROMONES,
                            RIGHT, RIGHT_AND_PHEROMONES,
                            COLLECT_FOOD, COLLECT_MATERIAL,
                            DROP_FOOD, DROP_MATERIAL,
                            NEST;

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
        COLLECT_FOOD = MessageUtil.asJsonAntMessage(msg, Actions.ANT_ACTION_COLLECT_FOOD);
        COLLECT_MATERIAL = MessageUtil.asJsonAntMessage(msg, Actions.ANT_ACTION_COLLECT_MATERIAL);
        DROP_FOOD = MessageUtil.asJsonAntMessage(msg, Actions.ANT_ACTION_DROP_FOOD);
        DROP_MATERIAL = MessageUtil.asJsonAntMessage(msg, Actions.ANT_ACTION_DROP_MATERIAL);
        NEST = MessageUtil.asJsonAntMessage(msg, Actions.ANT_ACTION_NEST);
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
            case ANT_ACTION_COLLECT_FOOD:
                return COLLECT_FOOD;
            case ANT_ACTION_COLLECT_MATERIAL:
                return COLLECT_MATERIAL;
            case ANT_ACTION_DROP_FOOD:
                return DROP_FOOD;
            case ANT_ACTION_DROP_MATERIAL:
                return DROP_MATERIAL;
            case ANT_ACTION_NEST:
                return NEST;
            default:
                return "";
        }
    }
}

package messages;

import enums.Action;
import enums.Direction;

public class ActionMessage {
    public Action action;
    public Direction direction;

    public ActionMessage() {
        this.action = Action.ANT_ACTION_NONE;
        this.direction = Direction.NONE;
    }
    public ActionMessage(Action action) {
        this.action = action;
        this.direction = Direction.NONE;
    }

    public ActionMessage(Action action, Direction direction) {
        this.action = action;
        this.direction = direction;
    }
}

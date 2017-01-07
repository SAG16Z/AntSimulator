package messages;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.JsonParseException;
import enums.Actions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.lang.reflect.Type;

public class MessageUtil {

    static final Logger LOG = LoggerFactory.getLogger(MessageUtil.class);

    private static class ColorInstanceCreator implements InstanceCreator<Color> {
        public Color createInstance(Type type) {
            return Color.WHITE;
        }
    }

    private static final Gson GSON = new GsonBuilder().registerTypeAdapter(
            Color.class, new ColorInstanceCreator()).create();

    public final String LOGIN, UP, DOWN, LEFT, RIGHT, COLLECT, DROP;

    /**
     * Creates a message utils object which will use the given color for it's
     * messages.
     *
     * @param color
     */
    public MessageUtil(Color color) {
        AntMessage msg = new AntMessage();
        msg.setColor(color);

        LOGIN = asJsonWithType(msg, Actions.ANT_ACTION_LOGIN);
        UP = asJsonWithType(msg, Actions.ANT_ACTION_UP);
        DOWN = asJsonWithType(msg, Actions.ANT_ACTION_DOWN);
        LEFT = asJsonWithType(msg, Actions.ANT_ACTION_LEFT);
        RIGHT = asJsonWithType(msg, Actions.ANT_ACTION_RIGHT);
        COLLECT = asJsonWithType(msg, Actions.ANT_ACTION_COLLECT);
        DROP = asJsonWithType(msg, Actions.ANT_ACTION_DROP);
    }


    /**
     * Creates a Json string according to the given messages.AntMessage's color and the
     * given action.
     *
     * @param msg
     * @param action
     * @return
     */
    private synchronized static String asJsonWithType(AntMessage msg, Actions action) {
        msg.setType(action.toString());
        return GSON.toJson(msg, AntMessage.class);
    }

    public synchronized static String asJsonPerception(PerceptionMessage msg){
        return GSON.toJson(msg, PerceptionMessage.class);
    }

    /**
     * Parses a perception message from a Json string.
     *
     * @param json
     * @return
     */
    public synchronized static PerceptionMessage getPerception(String json) {
        try {
            PerceptionMessage msg = GSON.fromJson(json, PerceptionMessage.class);
            return msg;
        } catch (JsonParseException e) {
            LOG.error(e.getMessage());
            return null;
        }
    }
    /**
     * Parses a perception message from a Json string.
     *
     * @param json
     * @return
     */
    public synchronized static AntMessage getAnt(String json) {
        try {
            AntMessage msg = GSON.fromJson(json, AntMessage.class);
            return msg;
        } catch (JsonParseException e) {
            LOG.error(e.getMessage());
            return null;
        }
    }
}

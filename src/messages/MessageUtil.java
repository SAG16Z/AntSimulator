package messages;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.JsonParseException;
import enums.Actions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.rmi.runtime.Log;

import java.awt.*;
import java.lang.reflect.Type;

public class MessageUtil {

    private static final Logger LOG = LoggerFactory.getLogger(MessageUtil.class);

    private static class ColorInstanceCreator implements InstanceCreator<Color> {
        public Color createInstance(Type type) {
            return Color.WHITE;
        }
    }

    private static final Gson GSON = new GsonBuilder().registerTypeAdapter(
            Color.class, new ColorInstanceCreator()).create();

    /**
     * Creates a ant message JSON string according to the given message and action type.
     * @param msg
     *      AntMessage object to serialize
     * @param action
     *      Type of ant action
     * @return
     *      JSON string representing message
     */
    public synchronized static String asJsonAntMessage(AntMessage msg, Actions action, boolean leavesPheromones) {
        msg.setType(action);
        msg.setLeavePheromones(leavesPheromones);
        return GSON.toJson(msg, AntMessage.class);
    }

    public synchronized static String asJsonAntMessage(AntMessage msg, Actions action) {
        return asJsonAntMessage(msg, action, false);
    }

    /**
     * Creates perception message JSON string according to given message
     * @param msg
     *      PerceptionMessage object to serialize
     * @return
     *      JSON string representing message
     */
    public synchronized static String asJsonPerception(PerceptionMessage msg){
        return GSON.toJson(msg, PerceptionMessage.class);
    }

    /**
     * Parses a perception message from a JSON string.
     *
     * @param json
     *      JSON to parse
     * @return
     *      PerceptionMessage object or null
     */
    public synchronized static PerceptionMessage getPerception(String json) {
        try {
            return GSON.fromJson(json, PerceptionMessage.class);
        } catch (JsonParseException e) {
            LOG.error(e.getMessage());
            return null;
        }
    }
    /**
     * Parses an ant message from a JSON string.
     *
     * @param json
     *      JSON to parse
     * @return
     *      AntMessage object or null
     */
    public synchronized static AntMessage getAnt(String json) {
        try {
            return GSON.fromJson(json, AntMessage.class);
        } catch (JsonParseException e) {
            LOG.error(e.getMessage());
            return null;
        }
    }
}


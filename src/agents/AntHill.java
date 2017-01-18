package agents;

import behaviours.ReceiveMessageBehaviour;
import com.google.gson.Gson;
import enums.Direction;
import gui.MapPanel;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import map.Point;
import map.Point2;
import messages.HillMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.function.Function;

public class AntHill extends AntAgentBase {

    public static String NICKNAME = "agents.Hill";
    public static String ANTHILL_SERVICE = "hill-service";

    private static float MAX_GRADIENT = 100;
    private static int PHEROMONE_TTL = 5000;

    private static final Logger LOG = LoggerFactory.getLogger(AntHill.class);
    private Point2 position;
    private Color color;
    private long[][] pheromoneMap = new long[MapPanel.CELL_V][MapPanel.CELL_H];

    @Override
    protected void setup(){
        Object args[] = getArguments();
        color = (Color)args[0];
        position = (Point2)args[1];

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType(ANTHILL_SERVICE);
        sd.setName("AntHil_" + color.toString());
        Property p = new Property("Color", color.toString());
        sd.addProperties(p);

        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }

        LOG.debug("{} Hello! agent is ready.", getLocalName());

        MessageTemplate mtAWRequest = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
        addBehaviour(new ReceiveMessageBehaviour(mtAWRequest, this::onAWRequest));

        MessageTemplate mtAWRInform = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
        addBehaviour(new ReceiveMessageBehaviour(mtAWRInform, this::onAWInform));

        for (int y = 0; y < pheromoneMap.length; ++y) {
            for (int x = 0; x < pheromoneMap[y].length; ++x) {
                pheromoneMap[y][x] = 0;
            }
        }
    }

    @Override
    protected void takeDown() {
        // Deregister from the yellow pages
        try {
            DFService.deregister(this);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        LOG.debug("agents.Hill agent " + getAID().getName() + " terminating.");
    }

    private void onAWRequest(ACLMessage msg) {
        String content = msg.getContent();
        LOG.debug("{}", content);
        Gson g = new Gson();
        Point2 antPosition = g.fromJson(content, Point2.class);

        HillMessage hmsg = new HillMessage();
        hmsg.gradient = getGradient(antPosition);
        hmsg.gradientDirection = getDirectionVector(antPosition, this::getGradient);
        hmsg.pheromone = getPheromone(antPosition);
        hmsg.pheromoneDirection = getDirectionVector(antPosition, this::getPheromone);

        ACLMessage reply = prepareReply(msg, ACLMessage.INFORM);
        reply.setContent(g.toJson(hmsg, HillMessage.class));
        send(reply);
    }

    private void onAWInform(ACLMessage msg) {
        String content = msg.getContent();
        //AntMessage ant = messages.MessageUtil.getAnt(content);
        Point target = new Point(1, 1);



        ACLMessage reply = prepareReply(msg, ACLMessage.INFORM);
        //reply.setContent(MessageUtil.asJsonPerception(pm));
        //Direction dir = getGradientVector()
        //float gradient = getGradient(target);

        pheromoneMap[target.y][target.x] = System.currentTimeMillis();
        //Direction dir = getDirectionVector(target, this::getGradient);

        //send(reply);
    }

    private float getGradient(Point2 p) {
        float gradient = MAX_GRADIENT - (float)Math.hypot(p.x - position.x, p.y - position.y);
        return gradient/MAX_GRADIENT;
    }

    private float getPheromone(Point2 p) {
        if(System.currentTimeMillis() - pheromoneMap[p.y][p.x] > PHEROMONE_TTL)
        {
            pheromoneMap[p.y][p.x] = 0;
            return 0.0f;
        }

        return 1.0f - ((System.currentTimeMillis() - pheromoneMap[p.y][p.x])/PHEROMONE_TTL);
    }

    private boolean isValidPosition(Point2 point) {
        return point != null && point.x >= 0 && point.x < MapPanel.CELL_H && point.y >= 0 && point.y < MapPanel.CELL_V;
    }

    public Direction getDirectionVector(Point2 p, Function<Point2, Float> getValue){
        float gradient = 0;
        Direction dir = Direction.NONE;
        if(isValidPosition(p)) {
            Point2 adjacent = p.left();
            if (isValidPosition(adjacent) && getValue.apply(adjacent) > gradient) {
                gradient = getValue.apply(adjacent);
                dir = Direction.LEFT;
            }
            adjacent = p.right();
            if (isValidPosition(adjacent) && getValue.apply(adjacent) > gradient) {
                gradient = getValue.apply(adjacent);
                dir = Direction.RIGHT;
            }
            adjacent = p.down();
            if (isValidPosition(adjacent) && getValue.apply(adjacent) > gradient) {
                gradient = getValue.apply(adjacent);
                dir = Direction.DOWN;
            }
            adjacent = p.up();
            if (isValidPosition(adjacent) && getValue.apply(adjacent) > gradient) {
                dir = Direction.UP;
            }
        }
        return dir;
    }

}

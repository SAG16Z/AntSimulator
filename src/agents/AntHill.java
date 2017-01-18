package agents;

import behaviours.ReceiveMessageBehaviour;
import enums.Direction;
import gui.MapPanel;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import map.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.Vector;
import java.util.function.Function;

public class AntHill extends Agent {

    public static String NICKNAME = "agents.Hill";
    public static String ANTHILL_SERVICE = "hill-service";

    private static float MAX_GRADIENT = 100;
    private static int PHEROMONE_TTL = 5000;

    private static final Logger LOG = LoggerFactory.getLogger(AntHill.class);
    private Point position;
    private Color color;
    private long[][] pheromoneMap = new long[MapPanel.getV()][MapPanel.getH()];

    static public int INIT_SIZE = 3;
    private Vector<Point> points;

    @Override
    protected void setup(){
        Object args[] = getArguments();
        color = (Color)args[0];
        position = (Point)args[1];

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType(ANTHILL_SERVICE);
        sd.setName("AntHill color-" + color.toString());
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }

        LOG.debug("{} Hello! Agent is ready.", getLocalName());

        MessageTemplate mtAWRequest = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
        addBehaviour(new ReceiveMessageBehaviour(mtAWRequest, this::onAWRequest));

        MessageTemplate mtAWRInform = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
        addBehaviour(new ReceiveMessageBehaviour(mtAWRInform, this::onAWInform));

        for (int y = 0; y < pheromoneMap.length; ++y) {
            for (int x = 0; x < pheromoneMap[y].length; ++x) {
                pheromoneMap[y][x] = 0;
            }
        }

        points = new Vector<Point>();
        for(int x = position.x - INIT_SIZE/2; x <= position.x + INIT_SIZE/2 ; x++) {
            for(int y = position.y - INIT_SIZE/2; y <= position.y + INIT_SIZE/2 ; y++) {
                points.add(new Point(x, y));
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

    private ACLMessage prepareReply(ACLMessage msg, int perf){
        ACLMessage reply = new ACLMessage(perf);
        reply.setSender(getAID());
        reply.setLanguage("json");
        reply.addReceiver(msg.getSender());
        return reply;
    }

    private void onAWRequest(ACLMessage msg) {
        String content = msg.getContent();
        //AntMessage ant = messages.MessageUtil.getAnt(content);
        Point target = new Point(1, 1);



        ACLMessage reply = prepareReply(msg, ACLMessage.INFORM);
        //reply.setContent(MessageUtil.asJsonPerception(pm));
        //Direction dir = getGradientVector()
        float gradient = getGradient(target);
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

    private float getGradient(Point p) {
        float gradient = MAX_GRADIENT - (float)Math.hypot(p.x - position.x, p.y - position.y);
        return gradient/MAX_GRADIENT;
    }

    private float getPheromone(Point p) {
        if(System.currentTimeMillis() - pheromoneMap[p.y][p.x] > PHEROMONE_TTL)
        {
            pheromoneMap[p.y][p.x] = 0;
            return 0.0f;
        }

        return 1.0f - ((System.currentTimeMillis() - pheromoneMap[p.y][p.x])/PHEROMONE_TTL);
    }

    private boolean isValidPosition(Point point) {
        return point != null && point.x >= 0 && point.x < MapPanel.getH() && point.y >= 0 && point.y < MapPanel.getV();
    }

    public Direction getDirectionVector(Point p, Function<Point, Float> getValue){
        float gradient = 0;
        Direction dir = null;
        if(isValidPosition(p)) {
            Point adjacent = p.left();
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

    public Point getPosition() {
        return position;
    }
}